package com.toonystank.requisiteteams.gui.data.filesystem;


import com.toonystank.requisiteteams.gui.SortMode;
import com.toonystank.requisiteteams.gui.data.ItemSection;

public class SortData {

    public ItemSection section;
    public SortMode sortMode;

    public SortData(ItemSection section) {
        this.section = section;
        if (section.getSectionName().equalsIgnoreCase("mode_selector_permission")) {
            this.sortMode = SortMode.PERMISSION;
        } else if (section.getSectionName().equalsIgnoreCase("mode_selector_parameter")) {
            this.sortMode = SortMode.PARAMETER;
        } else if (section.getSectionName().equalsIgnoreCase("mode_selector_flag")) {
            this.sortMode = SortMode.FLAG;
        }else if (section.getSectionName().equalsIgnoreCase("mode_selector_expiring")) {
            this.sortMode = SortMode.EXPIRING;
        }else if (section.getSectionName().equalsIgnoreCase("mode_selector_status")) {
            this.sortMode = SortMode.STATUS;
        }else if (section.getSectionName().equalsIgnoreCase("mode_selector_category")) {
            this.sortMode = SortMode.CATEGORY;
        }else if (section.getSectionName().equalsIgnoreCase("mode_selector_type")) {
            this.sortMode = SortMode.TYPE;
        }else if (section.getSectionName().equalsIgnoreCase("mode_selector_EorS")) {
            this.sortMode = SortMode.EorS;
        }
        else if (section.getSectionName().equalsIgnoreCase("mode_selector_EorSActives")) {
            this.sortMode = SortMode.EorS_ACTIVES;
        } else {
            this.sortMode = null;
        }
    }
}
