package com.toonystank.requisiteteams.gui.data.filesystem;


import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.utils.FileConfig;

import java.io.IOException;
import java.util.Optional;

/**
 * Manages priority and requirements for an item section.
 */
public class ItemPrioritySection {
    private final ItemSection itemSection;
    private final Integer priority;

    public ItemPrioritySection(ItemSection itemSection) throws IOException {
        if (itemSection == null) {
            throw new IllegalArgumentException("ItemSection cannot be null");
        }
        this.itemSection = itemSection;
        this.priority = ConfigHelper.getPriority(itemSection.getMenuConfig(), itemSection.getSectionName());
    }

    public Optional<Integer> getPriority() {
        return Optional.ofNullable(priority);
    }

}

class ConfigHelper {
    public static Integer getPriority(FileConfig menuConfig, String sectionName) {
        return menuConfig.getConfig().contains("items." + sectionName + ".priority")
                ? menuConfig.getConfig().getInt("items." + sectionName + ".priority")
                : null;
    }

    public static void setPriority(FileConfig menuConfig, String sectionName, Integer priority) {
        menuConfig.getConfig().set("items." + sectionName + ".priority", priority);
    }
}