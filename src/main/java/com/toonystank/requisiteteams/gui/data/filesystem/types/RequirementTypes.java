package com.toonystank.requisiteteams.gui.data.filesystem.types;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum RequirementTypes {

    HAS_EXP("has exp", "!has exp"),
    HAS_MONEY("has money", "!has money"),
    HAS_ITEM("has item", "!has item"),
    HAS_META("has meta", "!has meta"),
    HAS_PERMISSION("has permission", "!has permission"),
    IS_NEAR("is near", "!is near"),
    JAVA_SCRIPT("javascript", null),
    STRING_CONTAINS("string contains", "!string contains"),
    STRING_EQUALS("string equals", "!string equals"),
    STRING_EQUALS_IGNORECASE("string equals ignorecase", "!string equals ignorecase");

    public final String normal;
    public final @Nullable String inverted;
    RequirementTypes(String normal, @Nullable String inverted) {
        this.normal = normal;
        this.inverted = inverted;
    }
    public static boolean equals(String type) {
        for (RequirementTypes requirementType : RequirementTypes.values()) {
            if (requirementType.normal.equalsIgnoreCase(type)) {
                return true;
            }else if (requirementType.inverted != null && requirementType.inverted.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
    public static RequirementTypes getRequirementType(String type) {
        for (RequirementTypes requirementType : RequirementTypes.values()) {
            if (requirementType.normal.equalsIgnoreCase(type)) {
                return requirementType;
            }else if (requirementType.inverted != null && requirementType.inverted.equalsIgnoreCase(type)) {
                return requirementType;
            }
        }
        return null;
    }

}
