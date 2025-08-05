package com.toonystank.requisiteteams.team;

import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Leaderboard {

    /**
     * -- GETTER --
     *  Returns the singleton instance.
     */
    @Getter
    private static Leaderboard instance;

    private final ConcurrentHashMap<String, Team> teamCache;
    private List<Team> sortedTeams;
    private final AtomicBoolean needsSort;

    private static final Comparator<Team> TEAM_COMPARATOR = (team1, team2) -> {
        double xp1 = team1.getTeamLevel() != null ? team1.getTeamLevel().getCurrentXp() : 0;
        double xp2 = team2.getTeamLevel() != null ? team2.getTeamLevel().getCurrentXp() : 0;
        int cmp = Double.compare(xp2, xp1); // descending XP
        return cmp != 0 ? cmp : team1.getName().compareTo(team2.getName());
    };

    /**
     * Initializes the singleton instance and starts the sort scheduler.
     */
    public Leaderboard(Plugin plugin) {
        instance = this;
        this.teamCache = new ConcurrentHashMap<>();
        this.sortedTeams = new ArrayList<>();
        this.needsSort = new AtomicBoolean(true);

        refreshFromTeamMap();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (needsSort.compareAndSet(true, false)) {
                    sortedTeams = teamCache.values().stream()
                            .filter(t -> t.getTeamLevel() != null)
                            .sorted(TEAM_COMPARATOR)
                            .collect(Collectors.toList());
                    MessageUtils.debug("Leaderboard auto-sorted (" + sortedTeams.size() + " teams).");
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 40L); // 40 ticks = 2 seconds
    }

    public void updateTeam(Team team) {
        if (team.getTeamLevel() == null) {
            MessageUtils.debug("Skipping update: " + team.getName() + " has no TeamLevel.");
            return;
        }

        teamCache.put(team.getName(), team);
        needsSort.set(true);
    }

    public void addTeam(Team team) {
        if (team.getTeamLevel() == null) {
            MessageUtils.debug("Skipping add: " + team.getName() + " has no TeamLevel.");
            return;
        }

        teamCache.put(team.getName(), team);
        needsSort.set(true);
    }

    public void removeTeam(Team team) {
        teamCache.remove(team.getName());
        needsSort.set(true);
        MessageUtils.debug("Removed team: " + team.getName());
    }

    /**
     * Fully reloads the leaderboard from TeamManager.
     */
    public void refreshFromTeamMap() {
        teamCache.clear();
        TeamManager.getTeamMap().values().forEach(team -> {
            if (team.getTeamLevel() != null) {
                teamCache.put(team.getName(), team);
            }
        });
        needsSort.set(true);
        MessageUtils.debug("Leaderboard refreshed from TeamMap.");
    }

    /**
     * Immediately re-sorts the leaderboard manually.
     */
    public void forceRefresh() {
        sortedTeams = teamCache.values().stream()
                .filter(t -> t.getTeamLevel() != null)
                .sorted(TEAM_COMPARATOR)
                .collect(Collectors.toList());
        needsSort.set(false);
        MessageUtils.debug("Leaderboard forcefully refreshed manually.");
    }

    /**
     * Returns 1-based rank of a team.
     */
    public int getTeamRank(Team team) {
        List<Team> currentList = this.sortedTeams;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).equals(team)) return i + 1;
        }
        return -1;
    }

    /**
     * Returns the team at the given 1-based rank.
     */
    public Team getTeamAtRank(int rank) {
        List<Team> currentList = this.sortedTeams;
        if (rank < 1 || rank > currentList.size()) return null;
        return currentList.get(rank - 1);
    }

    /**
     * Returns the top N ranked teams.
     */
    public List<Team> getTopTeams(int n) {
        List<Team> currentList = this.sortedTeams;
        if (n <= 0) return List.of();
        return currentList.subList(0, Math.min(n, currentList.size()));
    }

    /**
     * Debug output of leaderboard.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Leaderboard:\n");
        List<Team> currentList = this.sortedTeams;
        for (int i = 0; i < currentList.size(); i++) {
            Team team = currentList.get(i);
            sb.append(String.format("%d. %s - Level: %d, XP: %.2f%n",
                    i + 1,
                    team.getName(),
                    team.getTeamLevel().getLevel().getLevel(),
                    team.getTeamLevel().getCurrentXp()));
        }
        return sb.toString();
    }
}