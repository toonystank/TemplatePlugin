package com.toonystank.requisiteteams.data;

import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager extends FileConfig {

    public static final Map<UUID, RequisitePlayer> playerUuidMap = new ConcurrentHashMap<>();
    private static final Map<String, UUID> playerNameMap = new ConcurrentHashMap<>();
    public static PlayerDataManager staticInstance;

    public PlayerDataManager() throws IOException {
        super("playerdata.yml", "data", false, false);
        if (staticInstance != null) {
            MessageUtils.warning("Multiple instances of PlayerDataManager detected!");
        }
        staticInstance = this;
    }

    public static boolean addPlayer(RequisitePlayer player, boolean bypassCheck) {
        if (player == null || player.getUuid() == null) {
            MessageUtils.debug("Null player or UUID in addPlayer");
            return false;
        }
        if (!bypassCheck && playerUuidMap.containsKey(player.getUuid())) {
            MessageUtils.debug("Player already exists in cache: " + player.getUuid());
            return false;
        }
        playerUuidMap.put(player.getUuid(), player);
        String name = Optional.of(player.getName())
                .orElse("someone-(" + player.getUuid() + ")");
        playerNameMap.put(name.toLowerCase(), player.getUuid());
        return true;
    }

    public static @Nullable RequisitePlayer getPlayer(String name) {
        if (name == null) {
            MessageUtils.debug("Null name in getPlayer");
            return null;
        }
        name = name.toLowerCase();
        UUID uuid = playerNameMap.get(name);
        if (uuid != null) {
            RequisitePlayer player = playerUuidMap.get(uuid);
            if (player != null && player.getUuid() != null && player.getName() != null) {
                return player;
            }
        }
        return loadPlayerData(name);
    }

    public static @Nullable RequisitePlayer getPlayer(UUID uuid) {
        if (uuid == null) {
            MessageUtils.debug("Null UUID in getPlayer");
            return null;
        }
        RequisitePlayer player = playerUuidMap.get(uuid);
        if (player != null && player.getUuid() != null && player.getName() != null) {
            return player;
        }
        return loadPlayerData(uuid);
    }

    private static @Nullable RequisitePlayer loadPlayerData(Object identifier) {
        if (staticInstance == null) {
            MessageUtils.error("PlayerDataManager instance is null");
            return null;
        }
        OfflinePlayer offlinePlayer = null;
        String name = null;
        UUID uuid = null;
        if (identifier instanceof String) {
            name = (String) identifier;
            offlinePlayer = Bukkit.getOfflinePlayer(name);
            uuid = offlinePlayer.getUniqueId();
        } else if (identifier instanceof UUID) {
            uuid = (UUID) identifier;
            offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            name = offlinePlayer.getName();
        }
        if (uuid == null || name == null) {
            MessageUtils.debug("Could not resolve UUID or name for identifier: " + identifier);
            return null;
        }
        double collectedXP = staticInstance.getDouble(uuid + ".collectedXP");
        RequisitePlayer player = new RequisitePlayer(offlinePlayer, collectedXP);
        addPlayer(player, true);
        MessageUtils.debug("Loaded player data for: " + uuid);
        return player;
    }

    public static List<RequisitePlayer> getPlayers() {
        return new ArrayList<>(playerUuidMap.values());
    }
}
