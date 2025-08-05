package com.toonystank.requisiteteams.team.rank;

import com.toonystank.requisiteteams.utils.FileConfig;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class RankData extends FileConfig {

    public RankData() throws IOException {
        super("ranks.yml",false,true);
        load();
    }

    public void load() throws IOException {
        Set<String> section = getConfigurationSection("ranks",false,true);
        for (String ranks : section) {
            String DEFAULT_PATH = "ranks.";
            String displayName = getString(DEFAULT_PATH + ranks + ".display_name");
            int priority = getInt(DEFAULT_PATH + ranks + ".priority");
            boolean isDefault = getBoolean(DEFAULT_PATH + ranks + ".is_default");
            boolean isOwnerRank = getBoolean(DEFAULT_PATH + ranks + ".is_owner", false);
            String parent = getString(DEFAULT_PATH + ranks + ".parent");
            List<String> permissions = getStringList(DEFAULT_PATH + ranks + ".permissions");
            for (String permission : permissions) {
                if (permission.equalsIgnoreCase("*")) {
                    permissions = RankPermissions.getAllPermissionsString();
                }
            }
            List<RankPermissions> rankPermissions = permissions.stream()
                    .map(RankPermissions::valueOf)
                    .toList();
            RankRegistry.registerRank(ranks, displayName, priority, parent, isOwnerRank, isDefault, rankPermissions);
        }
    }

    @Override
    public void reload() throws IOException {
        super.reload();
        RankRegistry.getRanks().clear();
        load();
    }


}
