package com.toonystank.requisiteteams.gui.data.filesystem;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.gui.data.filesystem.types.*;
import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Parses ItemSections to select the highest-priority section that meets view requirements.
 * Highest priority corresponds to the lowest priority number (e.g., 1 is higher than 2).
 * Items without a set priority are assigned the lowest available priority number not used in the same slot.
 */
public class PriorityParser {

    private final FileConfig configManager;

    public PriorityParser(@NotNull FileConfig configManager) {
        this.configManager = Objects.requireNonNull(configManager, "FileConfig cannot be null");
    }

    /**
     * Parses a list of ItemSections and returns the one with the highest priority
     * (lowest priority number) whose view requirements are met for the given player.
     *
     * @param sections The list of ItemSections to evaluate.
     * @param player   The RequisitePlayer to check requirements against.
     * @param args     Optional arguments for requirement parsing.
     * @param baseGUI  The BaseGUI instance for formatting and context.
     * @return The qualifying ItemSection with the highest priority (lowest number), or null if none qualify.
     */
    @Nullable
    public ItemSection parse(@NotNull List<ItemSection> sections, @NotNull RequisitePlayer player, @Nullable List<String> args, BaseGUI baseGUI) {
        if (sections.isEmpty()) {
            MessageUtils.warning("No ItemSections provided for parsing.");
            return null;
        }

        ItemSection highestPrioritySection = null;
        int lowestPriority = Integer.MAX_VALUE;

        // Collect all used priorities to determine default for unset items
        Set<Integer> usedPriorities = new HashSet<>();
        for (ItemSection section : sections) {
            section.getPrioritySection().getPriority().ifPresent(usedPriorities::add);
        }

        for (ItemSection section : sections) {
            try {
                // Get priority, assigning default if unset
                int priority = section.getPrioritySection().getPriority().orElseGet(() -> getDefaultPriority(usedPriorities));
                // Update usedPriorities to ensure subsequent unset items get the next available priority
                usedPriorities.add(priority);

                // Check if view requirements are met
                if (meetsViewRequirements(priority,section, player, args, baseGUI)) {
                    // Update if this section has a lower priority number (higher priority)
                    if (priority < lowestPriority) {
                        lowestPriority = priority;
                        highestPrioritySection = section;
                    }
                }
            } catch (Exception e) {
                MessageUtils.error("Error parsing item section '" + section.getSectionName() + "' with priority " + section.getPrioritySection().getPriority().orElseGet(() -> getDefaultPriority(usedPriorities)) + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (highestPrioritySection == null) {
            MessageUtils.debug("Selected ItemSection '" + null + "' with priority " + lowestPriority);
        } else {
            MessageUtils.debug("No ItemSection selected for player '" + player.getPlayer().getName() + "' in slot " + (sections.isEmpty() ? "unknown" : sections.get(0).getSlots()));
        }

        return highestPrioritySection;
    }

    /**
     * Checks if the ItemSection's view requirements are met for the given player.
     *
     * @param priority The priority of the item being evaluated.
     * @param section The ItemSection to evaluate.
     * @param player  The RequisitePlayer to check requirements against.
     * @param args    Optional arguments for requirement parsing.
     * @param baseGUI The BaseGUI instance for parsing and context.
     * @return True if all requirements are met, false otherwise.
     */
    private boolean meetsViewRequirements(int priority, @NotNull ItemSection section, @NotNull RequisitePlayer player, @Nullable List<String> args, BaseGUI baseGUI) throws IOException {
        String basePath = "items." + section.getSectionName() + ".view_requirement.requirements";
        if (!configManager.getConfig().contains(basePath)) {
            MessageUtils.debug("No view requirements for section '" + section.getSectionName() + "' with priority " + priority + ", valid by default.");
            return true;
        }

        Set<String> requirementKeys = configManager.getConfig().getConfigurationSection(basePath).getKeys(false);
        for (String reqKey : requirementKeys) {
            MessageUtils.debug("Processing requirement '" + reqKey + "' for section '" + section.getSectionName() + "' with priority " + priority);
            String reqPath = basePath + "." + reqKey;
            String typeStr = configManager.getString(reqPath + ".type", "").toUpperCase().replace(" ", "_");
            boolean isInverted = typeStr.startsWith("!");

            // Handle inversion
            if (isInverted) {
                typeStr = typeStr.substring(1);
            }

            try {
                Requirements requirement = createRequirement(typeStr, reqPath, section);
                boolean result = requirement.parse(player, args, baseGUI);

                // Log requirement evaluation
                MessageUtils.debug("Evaluating requirement '" + reqKey + "' for section '" + section.getSectionName() +", result=" + result + (isInverted ? " (inverted)" : ""));

                // Apply inversion if needed
                if (isInverted) {
                    result = !result;
                }

                // If any requirement fails, the section is invalid
                if (!result) {
                    MessageUtils.debug("Requirement '" + reqKey + "' failed for section '" + section.getSectionName() + "' with priority " + priority);
                    return false;
                }
            } catch (IllegalArgumentException e) {
                MessageUtils.warning("Failed to process requirement '" + reqKey + "' for section '" + section.getSectionName() + "' with priority " + priority + ": " + e.getMessage());
                return false;
            }
        }

        MessageUtils.debug("All requirements passed for section '" + section.getSectionName() + "' with priority " + priority);
        return true;
    }

    /**
     * Creates a Requirements instance based on the type string.
     *
     * @param typeStr The requirement type string (e.g., "STRING_EQUALS").
     * @param path    The configuration path for the requirement.
     * @param section The associated ItemSection.
     * @return The Requirements instance.
     * @throws IllegalArgumentException If the type is invalid.
     */
    private Requirements createRequirement(@NotNull String typeStr, @NotNull String path, @NotNull ItemSection section) {
        // Normalize the input string to handle Turkish characters (e.g., İ -> I)
        String normalizedTypeStr = Normalizer.normalize(typeStr, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .replace("İ", "I")
                .replace("ı", "i");
        MessageUtils.debug("Processing requirement type: '" + typeStr + "' (normalized: '" + normalizedTypeStr + "') for section: " + section.getSectionName());

        try {
            RequirementTypes type = RequirementTypes.valueOf(normalizedTypeStr);
            MessageUtils.debug("Successfully parsed requirement type: " + type);
            return switch (type) {
                case STRING_EQUALS -> new StringEquals(path, section);
                case STRING_CONTAINS -> new StringContains(path, section);
                case STRING_EQUALS_IGNORECASE -> new StringEqualsIgnoreCase(path, section);
                case HAS_PERMISSION -> new HasPermission(path, section);
                case HAS_MONEY -> new HasMoney(path, section);
                default -> throw new IllegalArgumentException("Unsupported requirement type: " + normalizedTypeStr);
            };
        } catch (IllegalArgumentException e) {
            MessageUtils.error("Invalid requirement type: '" + typeStr + "' (normalized: '" + normalizedTypeStr + "') for section: " + section.getSectionName());
            throw new IllegalArgumentException("Invalid requirement type: " + typeStr, e);
        }
    }

    /**
     * Determines the default priority for an item without a set priority.
     * Returns the lowest available priority number (starting from 1) not already used.
     *
     * @param usedPriorities The set of priorities already assigned to other items in the same slot.
     * @return The default priority number.
     */
    private int getDefaultPriority(Set<Integer> usedPriorities) {
        int defaultPriority = 1;
        while (usedPriorities.contains(defaultPriority)) {
            defaultPriority++;
        }
        return defaultPriority;
    }
}