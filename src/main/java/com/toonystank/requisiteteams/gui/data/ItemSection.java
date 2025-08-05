package com.toonystank.requisiteteams.gui.data;

import com.toonystank.requisiteteams.gui.data.filesystem.ItemPrioritySection;
import com.toonystank.requisiteteams.gui.data.filesystem.SortData;
import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.util.NumberConversions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a configurable item in the GUI.
 */
public class ItemSection {
    @Getter
    private final FileConfig menuConfig;
    @Getter
    private final String sectionName;
    @Getter
    private final String displayName;
    @Getter
    private final int amount;
    private final List<String> lore;
    @Getter
    private final String material;
    private final List<Integer> slots;
    @Getter @Setter
    private Integer customModelData;
    private final List<String> leftClickCommands;
    private final List<String> rightClickCommands;
    @Getter
    private final ItemPrioritySection prioritySection;
    private final boolean isEnchanted;
    private final boolean isNewSection;
    private final boolean isClickCommand;
    private final boolean isMenuControl;
    @Getter
    private final SortData sortData;
    private final boolean isTheme;
    @Getter
    private final boolean isBedrockSupported;

    private static final List<String> PATHS_TO_SKIP = Arrays.asList("plus-", "negative-", "counter");

    public ItemSection(FileConfig configManager, String sectionID, boolean isTheme) throws IOException {
        this(configManager, sectionID, false, isTheme);
    }

    public ItemSection(FileConfig configManager, String sectionID, boolean newSection, boolean isTheme) throws IOException {
        if (configManager == null) {
            throw new IllegalArgumentException("FileConfig cannot be null");
        }
        if (sectionID == null) {
            throw new IllegalArgumentException("Section ID cannot be null");
        }
        this.menuConfig = configManager;
        this.sectionName = sectionID;
        this.isNewSection = newSection;
        this.isTheme = isTheme;
        if (!newSection) {
            ItemSectionConfig config = new ItemSectionConfig(configManager, sectionID);
            this.displayName = config.displayName;
            this.amount = config.amount;
            this.lore = config.lore;
            this.material = config.material;
            this.slots = new SlotConfig(configManager, sectionID).getSlots();
            this.customModelData = config.customModelData;
            this.isEnchanted = config.isEnchanted;
            this.leftClickCommands = config.leftClickCommands;
            this.rightClickCommands = config.rightClickCommands;
            this.isClickCommand = config.isClickCommand;
            this.isMenuControl = sectionID.equalsIgnoreCase("Next") || sectionID.equalsIgnoreCase("Previous");
            if (isTheme) {
                this.isBedrockSupported = configManager.getBoolean("items." + sectionName + ".bedrock_supported", false);

            }else this.isBedrockSupported = configManager.getBoolean("items." + sectionName + ".bedrock_supported", true);
        } else {
            // Initialize defaults for new sections
            this.displayName = "";
            this.amount = 1;
            this.lore = new ArrayList<>();
            this.material = "STONE";
            this.slots = new ArrayList<>();
            this.customModelData = null;
            this.isEnchanted = false;
            this.leftClickCommands = new ArrayList<>();
            this.rightClickCommands = new ArrayList<>();
            this.isClickCommand = false;
            this.isMenuControl = false;
            this.isBedrockSupported = true; // Default to true for new sections
        }
        this.prioritySection = new ItemPrioritySection(this);
        this.sortData = new SortData(this);
    }


    public List<Integer> getSlots() {
        return Collections.unmodifiableList(slots);
    }

    public void createSection() throws IOException {
        menuConfig.getConfig().set("items." + sectionName + ".display_name", displayName);
        menuConfig.getConfig().set("items." + sectionName + ".lore", lore);
        menuConfig.getConfig().set("items." + sectionName + ".material", material);
        menuConfig.getConfig().set("items." + sectionName + ".slots", slots);
        menuConfig.getConfig().set("items." + sectionName + ".model_data", customModelData);
        menuConfig.getConfig().set("items." + sectionName + ".glow", isEnchanted);
        menuConfig.getConfig().set("items." + sectionName + ".left_click_commands", leftClickCommands);
        menuConfig.getConfig().set("items." + sectionName + ".right_click_commands", rightClickCommands);
        menuConfig.save();
    }

    public List<String> getLore() {
        return Collections.unmodifiableList(lore);
    }

    public List<String> getLeftClickCommands() {
        return Collections.unmodifiableList(leftClickCommands);
    }

    public List<String> getRightClickCommands() {
        return Collections.unmodifiableList(rightClickCommands);
    }

    public boolean isEnchanted() {
        return isEnchanted;
    }

    public boolean isNewSection() {
        return isNewSection;
    }

    public boolean isClickCommand() {
        return isClickCommand;
    }

    public boolean isMenuControl() {
        return isMenuControl;
    }

    public boolean isTheme() {
        return isTheme;
    }
}

/**
 * Loads configuration data for an ItemSection.
 */
class ItemSectionConfig {
    public final String displayName;
    public final int amount;
    public final List<String> lore;
    public final String material;
    public final Integer customModelData;
    public final boolean isEnchanted;
    public final List<String> leftClickCommands;
    public final List<String> rightClickCommands;
    public final boolean isClickCommand;

    public ItemSectionConfig(FileConfig config, String sectionName) throws IOException {
        this.displayName = config.getString("items." + sectionName + ".display_name");
        this.amount = Math.max(1, config.getInt("items." + sectionName + ".amount"));
        this.lore = config.getStringList("items." + sectionName + ".lore");
        this.material = config.getString("items." + sectionName + ".material", "STONE");
        this.customModelData = config.getInt("items." + sectionName + ".model_data", 0);
        this.isEnchanted = config.getBoolean("items." + sectionName + ".glow");
        if (config.getConfig().contains("items." + sectionName + ".click_commands")) {
            this.leftClickCommands = config.getStringList("items." + sectionName + ".click_commands");
            this.rightClickCommands = new ArrayList<>(leftClickCommands);
            this.isClickCommand = true;
        } else {
            this.leftClickCommands = config.getStringList("items." + sectionName + ".left_click_commands");
            this.rightClickCommands = config.getStringList("items." + sectionName + ".right_click_commands");
            this.isClickCommand = false;
        }
    }
}

/**
 * Parses and validates slot configurations.
 */
class SlotConfig {
    private final List<Integer> slots = new ArrayList<>();

    public SlotConfig(FileConfig config, String sectionName) {
        String slotStr = config.getString("items." + sectionName + ".slot");
        List<String> slotList = config.getStringList("items." + sectionName + ".slots");

        if (slotStr != null) {
            if (slotStr.contains("-")) {
                try {
                    String[] range = slotStr.split("-");
                    int start = NumberConversions.toInt(range[0].trim());
                    int end = NumberConversions.toInt(range[1].trim());
                    for (int i = start; i <= end; i++) {
                        slots.add(i);
                    }
                } catch (NumberFormatException e) {
                    MessageUtils.warning("Invalid slot range in " + sectionName + ": " + slotStr);
                }
            } else {
                try {
                    slots.add(NumberConversions.toInt(slotStr));
                } catch (NumberFormatException e) {
                    MessageUtils.warning("Invalid slot in " + sectionName + ": " + slotStr);
                }
            }
        }
        slotList.forEach(slot -> {
            try {
                slots.add(NumberConversions.toInt(slot));
            } catch (NumberFormatException e) {
                MessageUtils.warning("Invalid slot in " + sectionName + ": " + slot);
            }
        });
    }

    public List<Integer> getSlots() {
        return Collections.unmodifiableList(slots);
    }
} 