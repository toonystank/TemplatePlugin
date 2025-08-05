package com.toonystank.requisiteteams.gui.data.filesystem;

import java.util.Arrays;
import java.util.List;

public enum RequirementSectionTypes {
        OPEN_REQUIREMENT("open_requirement", "click_commands"),
        VIEW_REQUIREMENT("view_requirement", "click_commands"),
        CLICK_REQUIREMENT("click_requirement", "click_commands"),
        LEFT_CLICK_REQUIREMENT("left_click_requirement", "left_click_commands"),
        RIGHT_CLICK_REQUIREMENT("right_click_requirement", "right_click_commands"),
        SHIFT_LEFT_CLICK_REQUIREMENT("shift_left_click_requirement", "shift_left_click_commands"),
        SHIFT_RIGHT_CLICK_REQUIREMENT("shift_right_click_requirement", "shift_right_click_commands");

        public final String requirement;
        public final String command;

        RequirementSectionTypes(String requirement, String commandSection) {
            this.requirement = requirement;
            this.command = commandSection;
        }

        public static List<RequirementSectionTypes> getClickRequirements() {
            return Arrays.asList(CLICK_REQUIREMENT, LEFT_CLICK_REQUIREMENT, RIGHT_CLICK_REQUIREMENT,
                    SHIFT_LEFT_CLICK_REQUIREMENT, SHIFT_RIGHT_CLICK_REQUIREMENT);
        }
    }