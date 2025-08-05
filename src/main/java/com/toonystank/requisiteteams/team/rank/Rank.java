package com.toonystank.requisiteteams.team.rank;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class Rank {

    private final String name;
    private final String displayName;
    private final int priority;

    @Nullable private final String parent;
    private final boolean isOwnerRank;

    private final boolean isDefaultRank;

    private final List<RankPermissions> permissions;

    public Rank(String name, String displayName, int priority, @Nullable String parent, boolean isOwnerRank, boolean isDefaultRank, List<RankPermissions> permissions) {
        this.name = name;
        this.displayName = displayName;
        this.priority = priority;
        this.parent = parent;
        this.isOwnerRank = isOwnerRank;
        this.isDefaultRank = isDefaultRank;
        this.permissions = permissions;
    }


    public boolean hasPermission(RankPermissions permission) {
        return permissions.contains(permission);
    }
    public boolean hasPermission(String permission) {
        return permissions.stream().anyMatch(p -> p.name().equalsIgnoreCase(permission));
    }

    public boolean isHigherThan(Rank other) {
        return this.priority > other.priority;
    }
    public boolean isLowerThan(Rank other) {
        return this.priority < other.priority;
    }
    public boolean isEqualTo(Rank other) {
        return this.priority == other.priority;
    }
    public boolean addPermission(RankPermissions permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
            return true;
        }
        return false;
    }

}
