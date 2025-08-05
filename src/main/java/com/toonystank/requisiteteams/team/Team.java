package com.toonystank.requisiteteams.team;

import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.level.Level;
import com.toonystank.requisiteteams.level.LevelData;
import com.toonystank.requisiteteams.team.rank.Rank;
import com.toonystank.requisiteteams.team.rank.RankRegistry;
import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.*;

// under development
@Getter @Setter
public class Team extends FileConfig {

    private final UUID teamUUID;
    private String name;
    private TeamLevel teamLevel;
    private UUID ownerUUID;
    private final List<UUID> members;
    private int TeamBalance;

    // to track if the team is new or being loaded
    private boolean isNew = true;

    public Team(UUID teamUUID) throws IOException {
        super(teamUUID.toString() + ".yml", "teams", false, false);
        this.teamUUID = teamUUID;
        members = new ArrayList<>();
        init();
    }
    /**
     * Constructor for creating a new team.
     *
     * @param teamUUID   The UUID of the team.
     * @param name       The name of the team.
     * @param teamLevel  The initial level of the team.
     * @throws IOException If there is an error creating the file.
     */
    public Team(UUID teamUUID, String name, TeamLevel teamLevel,UUID ownerUUID) throws IOException {
        super(teamUUID.toString() + ".yml", "teams", true, false);
        this.teamUUID = teamUUID;
        this.name = name;
        this.teamLevel = teamLevel;
        this.ownerUUID = ownerUUID;
        members = new ArrayList<>();
        this.isNew = true;
    }

    public void init() throws IOException {
        this.name = getString("name");
        String ownerUUIDString = getString("ownerUUID");
        try {
            UUID.fromString(ownerUUIDString);
        }catch (IllegalArgumentException e) {
            throw new IOException("Invalid UUID format for player: " + ownerUUIDString, e);
        }
        this.ownerUUID = UUID.fromString(ownerUUIDString);
        int stringLevel = getInt("level", 0);
        Level level = LevelData.getLevel(stringLevel);
        if (level == null) {
            throw new IOException("Level " + stringLevel + " not found for team " + name);
        }
        double currentXp = getDouble("currentXP");
        this.teamLevel = new TeamLevel(level, currentXp,this);

        Set<String> playersUUID = getConfigurationSection("players", false, true);
        MessageUtils.debug("Found " + playersUUID.size() + " players in team " + name);
        for (String stringUUID : playersUUID) {
            MessageUtils.debug("Processing player UUID: " + stringUUID);
            try {
                UUID.fromString(stringUUID);
            }catch (IllegalArgumentException e) {
                throw new IOException("Invalid UUID format for player: " + stringUUID, e);
            }
            UUID uuid = UUID.fromString(stringUUID);
            RequisitePlayer player = PlayerDataManager.getPlayer(uuid);
            if (player == null) {
                throw new IOException("Player with UUID " + uuid + " not found in PlayerDataManager");
            }
            String rankString = getString("players." + stringUUID + ".rank");
            Rank rank = RankRegistry.getRank(rankString);
            if (rank == null) {
                throw new IOException("Rank " + rankString + " not found for player " + player.getName());
            }
            MessageUtils.debug("Adding player " + player.getName() + " with rank " + rank.getName() + " to team " + name);
            player.setPlayerTeam(teamUUID, rank);
            members.add(uuid);
        }
        this.isNew = false;
        this.TeamBalance = getInt("balance", 0);
        this.teamLevel.levelUp();

        // still not done
    }

    /**
     * Gets the team player by UUID.
     *
     * @param uuid The UUID of the player.
     * @return The RequisitePlayer object if found, null otherwise.
     */
    public RequisitePlayer getTeamPlayer(UUID uuid) {
        if (uuid == null) {
            return null; // Invalid UUID
        }
        if (!members.contains(uuid)) {
            return null; // Player not in team
        }
        RequisitePlayer player = PlayerDataManager.getPlayer(uuid);
        if (player == null) {
            return null; // Player not found
        }
        if (player.getTeamUUID().equals(teamUUID)) {
            return player;
        }
        return null;
    }

    public List<RequisitePlayer> getPlayersByRank(Rank rank) {
        if (rank == null) {
            return Collections.emptyList(); // Invalid rank
        }
        List<RequisitePlayer> teamPlayers = new ArrayList<>();
        for (UUID uuid : members) {
            RequisitePlayer player = PlayerDataManager.getPlayer(uuid);
            if (player != null && player.getTeamUUID().equals(teamUUID) && player.getRank().equals(rank)) {
                teamPlayers.add(player);
            }
        }
        return teamPlayers;
    }

    /**
     * Adds a player to the team with a specified rank.
     *
     * @param player The player to add.
     * @param rank   The rank of the player in the team.
     * @return true if the player was added successfully, false if the player is already in the team or if the player/rank is null.
     */
    public boolean addToTeam(RequisitePlayer player, Rank rank) {
        if (player == null || rank == null) {
            return false; // Invalid player or rank
        }
        if (members.contains(player.getUuid())) {
            return false; // Player already in team
        }
        player.setPlayerTeam(teamUUID,rank);
        members.add(player.getUuid());
        return true;
    }

    public boolean removeFromTeam(RequisitePlayer requisitePlayer) {
        if (members.contains(requisitePlayer.getUuid())) {
            return members.remove(requisitePlayer.getUuid());
        }
        return true;
    }

    @Override
    public void reload() throws IOException {
        MessageUtils.debug("Reloading team " + name);
        init();
    }






}
