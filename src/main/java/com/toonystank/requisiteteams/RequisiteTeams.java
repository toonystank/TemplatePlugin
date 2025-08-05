package com.toonystank.requisiteteams;

import com.toonystank.requisiteteams.command.TeamsCommand;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.level.LevelData;
import com.toonystank.requisiteteams.placeholders.PlaceholderRequest;
import com.toonystank.requisiteteams.team.Leaderboard;
import com.toonystank.requisiteteams.team.Team;
import com.toonystank.requisiteteams.team.TeamManager;
import com.toonystank.requisiteteams.team.TeamSaveScheduler;
import com.toonystank.requisiteteams.team.rank.RankData;
import com.toonystank.requisiteteams.utils.MainConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;

import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

@Getter
public final class RequisiteTeams extends JavaPlugin {

    // Change this for all the console outputs
    private final String pluginName = "RequisiteTeams";

    @Getter
    private static RequisiteTeams instance;

    private PlayerDataManager playerDataManager;

    private MainConfig mainConfig;
    private LevelData levelData;
    private RankData rankData;
    private TeamManager teamManager;
    private Leaderboard leaderboard;
    private PlaceholderRequest placeholderRequest;
    @Getter
    private Economy vault;


    @Override
    public void onEnable() {
        instance = this;
        MessageUtils.toConsole("Enabling " + pluginName + "...", false);
        try {
            this.playerDataManager = new PlayerDataManager();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.mainConfig = new MainConfig();
            MessageUtils.toConsole(pluginName + " has been enabled successfully.", false);
        } catch (Exception e) {
            MessageUtils.error("Failed to load config.yml: " + e.getMessage());
            e.printStackTrace();
        }
        this.leaderboard = new Leaderboard(this);
        try {
            this.levelData = new LevelData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.rankData = new RankData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.teamManager = new TeamManager();
        try {
            teamManager.loadTeams();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        new TeamsCommand(this);

        TeamSaveScheduler teamSaveScheduler = new TeamSaveScheduler(teamManager,this,300);
        teamSaveScheduler.start();
        this.placeholderRequest = new PlaceholderRequest(this,mainConfig.getLanguageConfig(),teamManager,levelData);

        // setupEconomy();

    }

    @Override
    public void onDisable() {
        MessageUtils.toConsole("Disabling " + pluginName + "...", false);
        // Add shutdown tasks here if needed
    }
    public double addXpToPlayer(String playerName, double xp) {
        if (playerDataManager == null) {
            MessageUtils.error("PlayerDataManager is not initialized.");
            return 0;
        }
        RequisitePlayer player = PlayerDataManager.getPlayer(playerName);
        if (player == null) {
            MessageUtils.error("Player " + playerName + " not found.");
            return 0;
        }
        if (xp < 0) {
            MessageUtils.error("Cannot add negative XP to player " + playerName + ".");
            return 0;
        }
        if (xp == 0) {
            MessageUtils.warning("No XP added to player " + playerName + " (XP is 0).");
            return 0;
        }
        MessageUtils.debug("Adding " + xp + " XP to player " + playerName + ".");
        double xpAddedToPlayer = player.addXP(xp);
        Team team = teamManager.getTeamByPlayer(player);
        team.getTeamLevel().addXp(xpAddedToPlayer);
        return xpAddedToPlayer;
    }

    public boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        vault = rsp.getProvider();
        return true;
    }

    public void reload() {
        MessageUtils.toConsole("Reloading " + pluginName + "...", false);
        long startTime = System.currentTimeMillis();

        try {
            if (mainConfig != null) {
                mainConfig.reload();
            } else {
                mainConfig = new MainConfig();
            }
            levelData.reload();
            rankData.reload();
            teamManager.reload();
        } catch (Exception e) {
            MessageUtils.error("An error occurred while reloading the plugin: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        long elapsed = System.currentTimeMillis() - startTime;
        MessageUtils.toConsole("Successfully reloaded " + pluginName + " in " + elapsed + " ms.", false);
    }

    public boolean isPapiHook() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                return true;
            } catch (ClassNotFoundException e) {
                MessageUtils.error("PlaceholderAPI is not installed or not found.");
            }
        } else {
            MessageUtils.error("PlaceholderAPI plugin is not installed.");
        }
        return false;
    }
}
