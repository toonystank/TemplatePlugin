package com.toonystank.requisiteteams.team;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

/**
 * Scheduler for asynchronously saving all teams at regular intervals.
 */
public class TeamSaveScheduler {

    private final TeamManager teamManager;
    private final RequisiteTeams plugin;
    private final long saveIntervalTicks;

    /**
     * Constructs a new TeamSaveScheduler.
     *
     * @param teamManager The TeamManager instance to save teams.
     * @param plugin      The RequisiteTeams plugin instance.
     * @param saveIntervalSeconds The interval in seconds between saves.
     */
    public TeamSaveScheduler(TeamManager teamManager, RequisiteTeams plugin, long saveIntervalSeconds) {
        this.teamManager = teamManager;
        this.plugin = plugin;
        this.saveIntervalTicks = saveIntervalSeconds * 20L; // Convert seconds to ticks (20 ticks per second)
    }

    /**
     * Starts the asynchronous team save task.
     */
    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    teamManager.saveAllTeams();
                    MessageUtils.toConsole("Asynchronously saved all teams", false);
                } catch (IOException e) {
                    MessageUtils.toConsole("Failed to asynchronously save teams: " + e.getMessage(), true);
                }
            }
        }.runTaskTimerAsynchronously(plugin, saveIntervalTicks, saveIntervalTicks);
    }
}