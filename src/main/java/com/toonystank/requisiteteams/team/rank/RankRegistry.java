package com.toonystank.requisiteteams.team.rank;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RankRegistry {

    @Getter
    private static final ConcurrentHashMap<String, Rank> ranks = new ConcurrentHashMap<>();

    public Rank registerRank(Rank rank) {
        if (ranks.containsKey(rank.getName())) {
            throw new IllegalArgumentException("Rank with name " + rank.getName() + " already exists.");
        }
        ranks.put(rank.getName(), rank);
        return rank;
    }
    public static Rank registerRank(String name,String displayName,int priority,String parent,boolean isOwnerRank, boolean isDefaultRank, List<RankPermissions> permissions) {
        if (ranks.containsKey(name)) {
            throw new IllegalArgumentException("Rank with name " + name + " already exists.");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("Priority must be non-negative.");
        }
        if (isOwnerRank) {
            if (getOwnerRank() != null) {
                throw new IllegalArgumentException("There can only be one owner rank.");
            }
        }
        if (isDefaultRank) {
            if (getDefaultRank() != null) {
                throw new IllegalArgumentException("There can only be one default rank.");
            }
        }

        Rank rank = new Rank(name,displayName,priority,parent,isOwnerRank, isDefaultRank, permissions);
        ranks.put(name, rank);
        return rank;
    }

    @Nullable
    public static Rank getOwnerRank() {
        return ranks.values().stream()
                .filter(Rank::isOwnerRank)
                .findFirst()
                .orElseGet(() -> ranks.values().stream()
                        .filter(rank -> rank.hasPermission(RankPermissions.DELETE_TEAM))
                        .findFirst()
                        .orElse(null));
    }

    @Nullable
    public static Rank getDefaultRank() {
        return ranks.values().stream()
                .filter(Rank::isDefaultRank)
                .findFirst()
                .orElse(null);
    }

    public static @Nullable Rank getRank(String name) {
        if (name == null) {
            return getDefaultRank();
        }
        return ranks.get(name);
    }


}
