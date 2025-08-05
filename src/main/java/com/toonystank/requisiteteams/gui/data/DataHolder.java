package com.toonystank.requisiteteams.gui.data;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.Theme;
import com.toonystank.requisiteteams.gui.data.filesystem.PriorityParser;
import com.toonystank.requisiteteams.gui.data.filesystem.RequirementSectionTypes;
import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * Manages configuration data and item prioritization for the GUI system.
 * Responsibilities:
 * - Loads configuration data via ConfigLoader.
 * - Manages item sections and their slot assignments.
 * - Processes item clicks and requirements.
 */
public class DataHolder {
    private final FileConfig configManager;
    @Getter
    private final DataSection dataSection;
    private final Map<String, ItemSection> itemSections;
    private final Map<Integer, List<ItemSection>> prioritizedItemSections;

    public DataHolder(FileConfig configManager, String defaultTheme) {
        if (configManager == null) {
            throw new IllegalArgumentException("FileConfig cannot be null");
        }
        if (defaultTheme == null) {
            throw new IllegalArgumentException("Default theme cannot be null");
        }
        this.configManager = configManager;
        ConfigLoader loader = new ConfigLoader(configManager, defaultTheme);
        this.dataSection = loader.loadDataSection();
        this.itemSections = new HashMap<>();
        // Load theme items first
        Theme theme = dataSection.getTheme();
        if (theme != null) {
            this.itemSections.putAll(loader.loadItemSections(theme, true));
        }
        // Then load non-theme items
        this.itemSections.putAll(loader.loadItemSections(false));
        this.prioritizedItemSections = buildPrioritizedItemSections();
    }

    /**
     * Builds a map of slots to sorted item sections based on priority.
     * Assigns default priorities to unset items and sorts in ascending order.
     */
    private Map<Integer, List<ItemSection>> buildPrioritizedItemSections() {
        Map<Integer, List<ItemSection>> temp = new HashMap<>();
        // Group sections by slot
        itemSections.values().forEach(section -> {
            for (Integer slot : section.getSlots()) {
                temp.computeIfAbsent(slot, k -> new ArrayList<>()).add(section);
            }
        });

        // Assign default priorities and sort for each slot
        temp.forEach((slot, sections) -> {
            // Collect used priorities
            Set<Integer> usedPriorities = new HashSet<>();
            for (ItemSection section : sections) {
                section.getPrioritySection().getPriority().ifPresent(usedPriorities::add);
            }

            // Assign default priorities to unset items
            for (ItemSection section : sections) {
                if (section.getPrioritySection().getPriority().isEmpty()) {
                    int defaultPriority = getDefaultPriority(usedPriorities);
                    usedPriorities.add(defaultPriority);
                }
            }

            // Sort sections by priority (ascending, lowest number first)
            sections.sort(Comparator.comparingInt(s -> s.getPrioritySection().getPriority().orElse(getDefaultPriority(usedPriorities))));
        });

        return temp;
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

    /**
     * Retrieves the highest-priority item section for a given slot that meets view requirements.
     * Highest priority corresponds to the lowest priority number (e.g., 1 is higher than 2).
     *
     * @param slot            The inventory slot to check.
     * @param player          The RequisitePlayer to evaluate requirements against.
     * @param manager         The BaseGUI instance for formatting and context.
     * @param args            Optional arguments for requirement parsing.
     * @param baseRequirement Additional requirement type to check, if any.
     * @return The qualifying ItemSection with the highest priority (lowest number), or null if none qualify.
     */
    public @Nullable ItemSection getCurrentPrioritizedItemSections(Integer slot, RequisitePlayer player, BaseGUI manager,
                                                                   List<String> args, RequirementSectionTypes baseRequirement) {
        // Get the list of ItemSections for the specified slot
        List<ItemSection> sections = prioritizedItemSections.getOrDefault(slot, Collections.emptyList());
        if (sections.isEmpty()) {
            MessageUtils.toConsole("No ItemSections found for slot " + slot, false);
            return null;
        }

        // Initialize the PriorityParser
        PriorityParser parser = new PriorityParser(configManager);

        // For VIEW_REQUIREMENT, rely on PriorityParser to handle view_requirement checks
        if (baseRequirement == RequirementSectionTypes.VIEW_REQUIREMENT) {
            ItemSection selectedSection = parser.parse(sections, player, args, manager);
            if (selectedSection == null) {
                MessageUtils.toConsole("No ItemSection met view requirements for slot " + slot + " and player " + player.getPlayer().getName(), false);
            }
            return selectedSection;
        }

        // For other or null baseRequirement, skip additional checks for now
        MessageUtils.toConsole("Unsupported base requirement type: " + (baseRequirement != null ? baseRequirement.name() : "null") + " for slot " + slot, false);
        return null;
    }

    public Map<String, ItemSection> getItemSections() {
        return new HashMap<>(itemSections);
    }

    public Map<Integer, List<ItemSection>> getPrioritizedItemSections() {
        return new HashMap<>(prioritizedItemSections);
    }
}

/**
 * Loads configuration data for DataSection and ItemSections.
 */
class ConfigLoader {
    private final FileConfig configManager;
    private final String defaultTheme;

    public ConfigLoader(FileConfig configManager, String defaultTheme) {
        this.configManager = configManager;
        this.defaultTheme = defaultTheme;
    }

    public DataSection loadDataSection() {
        return new DataSection(configManager, defaultTheme);
    }

    public Map<String, ItemSection> loadItemSections(boolean isTheme) {
        Map<String, ItemSection> sections = new HashMap<>();
        try {
            configManager.getConfigurationSection("items", false, true).forEach(item -> {
                try {
                    sections.put(item, new ItemSection(configManager, item, isTheme));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            MessageUtils.error("Error loading item sections: " + e.getMessage());
        }
        return sections;
    }

    /**
     * Loads item sections from a theme's configuration.
     */
    public Map<String, ItemSection> loadItemSections(Theme theme, boolean isTheme) {
        Map<String, ItemSection> sections = new HashMap<>();
        try {
            // Use the Theme object directly as a FileConfig
            theme.getConfigurationSection("items", false, true).forEach(item -> {
                try {
                    sections.put(item, new ItemSection(theme, item, isTheme));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            MessageUtils.error("Error loading theme item sections for theme '" + theme.getFile().getName() + "': " + e.getMessage());
        }
        return sections;
    }
}