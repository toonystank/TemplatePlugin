package com.toonystank.requisiteteams.team.rank;

import java.util.List;

public enum RankPermissions {

    DELETE_TEAM,
    CHANGE_TEAM_NAME,
    SET_RANK,

    ADD_MEMBER,
    REMOVE_MEMBER,
    PROMOTE_MEMBER,
    DEMOTE_MEMBER,
    CHANGE_DESCRIPTION,

    USE_TEAM_CHAT,
    CONTRIBUTE_XP,;

    public static List<RankPermissions> getAllPermissions() {
        return List.of(RankPermissions.values());
    }
    public static List<String> getAllPermissionsString() {
        return getAllPermissions().stream()
                .map(RankPermissions::name)
                .toList();
    }
}
