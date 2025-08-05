package com.toonystank.requisiteteams.gui;

import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

@Getter
public enum SortMode {

    PERMISSION("permission"),
    PARAMETER("parameter"),
    FLAG("flag"),
    EXPIRING("expiring"),
    STATUS("status"),
    CATEGORY("category"),
    TYPE("type"),
    EorS("EorS"),
    EorS_ACTIVES("EorSActives"),
    ;

    public final String name;
    SortMode(String s) {
        this.name = s;
    }

    public static SortMode getValue(@Nullable String s) {
        for (SortMode mode : values()) {
            if (mode.getName().equalsIgnoreCase(s)) {
                return mode;
            }
        }
        return null;
    }

    public enum Expiring {
        SOONEST("Soonest"),
        ALL("All");

        public final String name;
        Expiring(String s) {
            this.name = s;
        }
    }


    public enum Parameter {
        NEAREST("Nearest"),
        SMALLEST("Smallest"),
        LARGEST("Largest");

        public final String name;
        Parameter(String s) {
            this.name = s;
        }
        public static Parameter getParameterByString(String parma) {
            for (Parameter value : values()) {
                if (value.name.equalsIgnoreCase(parma)) {
                    return value;
                }
            }
            return null;
        }
    }
    public enum Category {
        Block("block"),
        EDIBLE("edible"),
        RECORD("record"),
        ALL("all");

        public final String name;
        Category(String name) {
            this.name = name;
        }
        public static Category getCategoryByString(String parma) {
            for (Category value : values()) {
                if (value.name.equalsIgnoreCase(parma)) {
                    return value;
                }
            }
            return null;
        }
        public static Category getCategory(Material material) {
            if (material.isBlock()) return Block;
            if (material.isEdible()) return EDIBLE;
            if (material.isRecord()) return RECORD;
            else return ALL;
        }
    }

    public enum EntitiesOrSpawnReason {
        ENTITIES("Entity"),
        SPAWN_REASON("Spawn Reason");

        public final String name;
        EntitiesOrSpawnReason(String s) {
            this.name = s;
        }

        public static EntitiesOrSpawnReason getEntitiesOrSpawnReasonByString(String parma) {
            for (EntitiesOrSpawnReason value : values()) {
                if (value.name.equalsIgnoreCase(parma)) {
                    return value;
                }
            }
            return null;
        }
    }
    public enum EorSActives {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        ALL("All");
        public final String name;
        EorSActives(String s) {
            this.name = s;
        }
        public static EorSActives getEorSActivesByString(String parma) {
            for (EorSActives value : values()) {
                if (value.name.equalsIgnoreCase(parma)) {
                    return value;
                }
            }
            return null;
        }
    }
    public enum Permission {
        OWNER("Owner"),
        PUBLIC("Public"),
        SUBDIVISION("Subdivision"),
        MANAGER("Manager"),
        TRUSTED("Trusted"),
        CONTAINER("Container"),
        ACCESS_TRUSTED("Access Trusted"),
        ALL("All");

        public final String name;

        Permission(String s) {
            this.name = s;
        }

        public static Permission getPermissionByString(String parma) {
            for (Permission value : values()) {
                if (value.name.equalsIgnoreCase(parma)) {
                    return value;
                }
            }
            return null;
        }
    }
    public enum ONLINE_STATUS {
        ONLINE("Online"),
        OFFLINE("Offline"),
        ALL("All");

        public final String name;

        ONLINE_STATUS(String s) {
            this.name = s;
        }
        public static ONLINE_STATUS getOnlineStatusByString(String parma) {
            for (ONLINE_STATUS value : values()) {
                if (value.name.equalsIgnoreCase(parma)) {
                    return value;
                }
            }
            return null;
        }
    }
    public enum Type {
        ALL("All"),
        SUB_REGION("SubRegion"),
        REGION("Region"),
        ;

        public final String name;

        Type(String s) {
            this.name = s;
        }
    }
}
