package com.toonystank.requisiteteams.data;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.team.Team;
import com.toonystank.requisiteteams.team.TeamManager;
import com.toonystank.requisiteteams.team.rank.Rank;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Getter
public class RequisitePlayer {

    @NotNull
    private final String name;
    @NotNull
    private final UUID uuid;
    private double collectedXP;
    private final int xpSharePercentage = 10; // Percentage of XP added to player

    private UUID teamUUID;
    private Rank rank;

    public Map<UUID, BaseGUI> guiMap = new HashMap<>();

    public RequisitePlayer(@NotNull String name, @NotNull UUID uuid, double collectedXP) {
        this.name = name;
        this.uuid = uuid;
        this.collectedXP = collectedXP;
    }

    public RequisitePlayer(OfflinePlayer offlinePlayer, double collectedXP) {
        if (offlinePlayer == null) {
            MessageUtils.warning("Attempted to create RequisitePlayer with null OfflinePlayer.");
            this.name = "Unknown";
            this.uuid = UUID.randomUUID();
            this.collectedXP = 0;
        } else {
            String name = offlinePlayer.getName();
            if (name == null) {
                name = "someone-(" + offlinePlayer.getUniqueId() + ")";
            }
            this.name = name;
            this.uuid = offlinePlayer.getUniqueId();
            this.collectedXP = collectedXP;
        }
    }

    public void setPlayerTeam(UUID teamUUID, Rank rank) {
        if (this.teamUUID != null && this.teamUUID.equals(teamUUID)) {
            MessageUtils.debug("Player " + name + " is already on team " + teamUUID);
            return;
        }
        if (this.teamUUID != null && !this.teamUUID.equals(teamUUID)) {
            Team team = TeamManager.getTeam(teamUUID);
            if (team != null) team.removeFromTeam(this);
        }
        this.teamUUID = teamUUID;
        this.rank = rank;
    }

    @NotNull
    public OfflinePlayer getPlayer() {
        OfflinePlayer offlinePlayer = RequisiteTeams.getInstance().getServer().getPlayer(uuid);
        return (offlinePlayer != null) ? offlinePlayer : RequisiteTeams.getInstance().getServer().getOfflinePlayer(uuid);
    }

    @Nullable
    public Player getOnlinePlayer() {
        return Optional.ofNullable(RequisiteTeams.getInstance().getServer().getPlayer(uuid))
                .orElseGet(() -> Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.getUniqueId().equals(uuid))
                        .findFirst()
                        .orElse(null));
    }

    public double addXP(double xpAmount) {
        // Calculate the player's portion (xpSharePercentage)
        double playerXPFraction = xpSharePercentage / 100.0;
        double xpToAdd = xpAmount * playerXPFraction;

        // Add the player's portion to collectedXP
        this.collectedXP += xpToAdd;

        // Return the amount added to the player's collectedXP
        return xpToAdd;
    }

    @Override
    public String toString() {
        return "RequisitePlayer{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                '}';
    }
}