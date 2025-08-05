package com.toonystank.requisiteteams.team;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.level.Level;
import com.toonystank.requisiteteams.level.LevelData;
import com.toonystank.requisiteteams.team.rank.Rank;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages teams, including registration, deletion, and persistence to YAML files.
 */
public class TeamManager {

    @Getter
    private static TeamManager instance;

    @Getter
    private static final ConcurrentHashMap<String, Team> teamMap = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<UUID, String> teamUUIDMap = new ConcurrentHashMap<>();

    public TeamManager() {
        instance = this;
    }

    /**
     * Registers a new team and saves it to its YAML file.
     *
     * @param team The team to register.
     * @return The registered team.
     * @throws IllegalArgumentException If a team with the same name already exists.
     * @throws IOException If saving to YAML fails.
     */
    public Team registerTeam(Team team) throws IOException {
        if (teamMap.containsKey(team.getName())) {
            Team existingTeam = teamMap.get(team.getName());
            existingTeam.setNew(false);
            return existingTeam;
        }
        // Ensure team has default values if not set
        if (team.getTeamLevel() == null) {
            Level defaultLevel = LevelData.getLevel(1);
            if (defaultLevel == null) {
                throw new IOException("Default level 1 not found for new team " + team.getName());
            }
            team.setTeamLevel(new TeamLevel(defaultLevel, 0, team));
        }
        if (team.getName() == null) {
            throw new IllegalArgumentException("Team name cannot be null");
        }
        teamMap.put(team.getName(), team);
        teamUUIDMap.put(team.getTeamUUID(), team.getName());
        Leaderboard.getInstance().addTeam(team); // Add to leaderboard
        return team;
    }

    /**
     * Saves a team's data to its YAML file.
     *
     * @param team The team to save.
     * @throws IOException If saving fails.
     */
    public void saveTeam(Team team) throws IOException {
        // Save team data using FileConfig
        team.set("name", team.getName());
        team.set("ownerUUID", team.getOwnerUUID() != null ? team.getOwnerUUID().toString() : "null");

        team.set("level", team.getTeamLevel() != null ? team.getTeamLevel().getLevel().getLevel() : 1);
        team.set("currentXP", team.getTeamLevel() != null ? team.getTeamLevel().getCurrentXp() : 0);
        team.set("balance", team.getTeamBalance());

        // Save players with their ranks
        for (UUID uuid : team.getMembers()) {
            RequisitePlayer player = PlayerDataManager.getPlayer(uuid);
            if (player == null) {
                MessageUtils.toConsole("Player with UUID " + uuid + " not found in PlayerDataManager, skipping", true);
                continue;
            }
            Rank rank = player.getRank();
            team.set("players." + uuid + ".rank", rank.getName());
        }
        team.save();
        MessageUtils.toConsole("Saved team " + team.getName() + " to " + team.getFileName(), false);
    }

    /**
     * Saves all teams in the teamMap to their respective YAML files.
     *
     * @throws IOException If saving any team fails.
     */
    public void saveAllTeams() throws IOException {
        for (Team team : teamMap.values()) {
            saveTeam(team);
        }
        MessageUtils.toConsole("Saved " + teamMap.size() + " teams to YAML files", false);
    }

    /**
     * Gets a team by its name.
     *
     * @param name The name of the team.
     * @return The Team object, or null if not found.
     */
    public static Team getTeam(String name) {
        return teamMap.get(name);
    }

    public static Team getTeam(UUID uuid) {
        String teamName = teamUUIDMap.get(uuid);
        if (teamName == null) return null;
        return teamMap.get(teamName);
    }

    public Team getTeamByPlayer(RequisitePlayer player) {
        UUID uuid = player.getTeamUUID();
        return getTeam(uuid);
    }

    /**
     * Deletes a team and removes its YAML file.
     *
     * @param name The name of the team to delete.
     * @return true if the team was deleted, false if it didn't exist.
     */
    public boolean deleteTeam(String name) {
        Team team = teamMap.get(name);
        if (team != null) {
            teamMap.remove(name);
            teamUUIDMap.remove(team.getTeamUUID());
            Leaderboard.getInstance().removeTeam(team); // Remove from leaderboard
            boolean deleted = team.deleteConfig();
            if (deleted) {
                MessageUtils.toConsole("Deleted team " + name + " and its YAML file", false);
            } else {
                MessageUtils.toConsole("Failed to delete YAML file for team " + name, true);
            }
            return deleted;
        }
        return false;
    }

    /**
     * Checks if a player is in any team.
     *
     * @param player The player to check.
     * @return true if the player is in a team, false otherwise.
     */
    public boolean isPlayerInTeam(@NotNull RequisitePlayer player) {
        for (Team team : teamMap.values()) {
            if (team.getMembers().contains(player.getUuid())) {
                return true;
            }
        }
        return false;
    }

    public boolean isTeamExists(String teamName) {
        if (teamName == null || teamName.isEmpty()) {
            MessageUtils.toConsole("Team name is null or empty", true);
            return false;
        }
        boolean exists = teamMap.containsKey(teamName);
        if (!exists) {
            MessageUtils.toConsole("Team " + teamName + " does not exist", false);
        }
        return exists;
    }

    public boolean isTeamExists(UUID teamUUID) {
        if (teamUUID == null) {
            MessageUtils.toConsole("Team uuid is null", true);
            return false;
        }
        boolean exists = teamUUIDMap.containsKey(teamUUID);
        if (!exists) {
            MessageUtils.toConsole("Team " + teamUUID + " does not exist", false);
        }
        return exists;
    }

    /**
     * Loads all teams from the teams folder into the teamMap.
     *
     * @throws IOException If loading fails for any team.
     */
    public void loadTeams() throws IOException {
        String teamsFolderPath = RequisiteTeams.getInstance().getDataFolder() + File.separator + "teams";
        teamMap.clear();
        teamUUIDMap.clear();
        File teamsFolder = new File(teamsFolderPath);
        if (!teamsFolder.exists() || !teamsFolder.isDirectory()) {
            if (teamsFolder.mkdirs()) {
                MessageUtils.toConsole("Created teams folder: " + teamsFolderPath, false);
            } else {
                MessageUtils.toConsole("Failed to create teams folder: " + teamsFolderPath, true);
            }
            return;
        }
        File[] teamFiles = teamsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (teamFiles == null || teamFiles.length == 0) {
            MessageUtils.toConsole("No team YAML files found in " + teamsFolderPath, false);
            return;
        }
        MessageUtils.debug("Loading teams from " + teamsFolderPath + " with " + teamFiles.length + " files");

        for (File teamFile : teamFiles) {
            MessageUtils.debug("Loading team file: " + teamFile.getName());
            try {
                String fileName = teamFile.getName();
                String uuidStr = fileName.replace(".yml", "");
                UUID teamUUID;
                try {
                    teamUUID = UUID.fromString(uuidStr);
                } catch (IllegalArgumentException e) {
                    MessageUtils.toConsole("Invalid UUID format in file name: " + fileName, true);
                    continue;
                }
                Team team = new Team(teamUUID); // Team.init() is called in constructor
                if (team.getName() == null) {
                    MessageUtils.toConsole("Team in file " + fileName + " has no name, skipping", true);
                    continue;
                }
                MessageUtils.debug("Initializing team: " + team.getName());
                teamUUIDMap.put(teamUUID, team.getName());
                teamMap.put(team.getName(), team);
                MessageUtils.toConsole("Loaded team " + team.getName() + " from " + fileName, false);
            } catch (IOException e) {
                MessageUtils.toConsole("Failed to load team from " + teamFile.getName() + ": " + e.getMessage(), true);
            }
        }
        Leaderboard.getInstance().refreshFromTeamMap(); // Refresh leaderboard after loading
        MessageUtils.toConsole("Loaded " + teamMap.size() + " teams from " + teamsFolderPath, false);
    }

    public void reload() {
        MessageUtils.toConsole("Reloading TeamManager...", false);
        try {
            loadTeams();
            MessageUtils.toConsole("TeamManager reloaded successfully", false);
        } catch (IOException e) {
            MessageUtils.toConsole("Failed to reload TeamManager: " + e.getMessage(), true);
        }
    }

}