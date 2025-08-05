package com.toonystank.requisiteteams.team;

import com.toonystank.requisiteteams.level.Level;
import com.toonystank.requisiteteams.level.LevelData;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;

/**
 * Represents a team's level and current XP, with automatic leveling functionality.
 */
@Getter
public class TeamLevel {

    private Level level;
    private double currentXp;
    private final Team team;

    public TeamLevel(Level level, double currentXp, Team team) {
        this.level = level;
        this.currentXp = currentXp;
        this.team = team;
    }

    /**
     * Adds XP to the team and attempts to level up if possible.
     *
     * @param xp The XP to add.
     * @return The next Level if the team leveled up, otherwise the current Level.
     */
    public Level addXp(double xp) {
        this.currentXp += xp;
        Level newLevel = levelUp();
        Leaderboard.getInstance().updateTeam(team); // Notify leaderboard of XP change
        return newLevel;
    }

    /**
     * Removes XP from the team.
     *
     * @param xp The XP to remove.
     * @return true if XP was removed, false if insufficient XP.
     */
    public boolean removeXp(double xp) {
        if (currentXp >= xp) {
            this.currentXp -= xp;
            Leaderboard.getInstance().updateTeam(team); // Notify leaderboard of XP change
            return true;
        }
        return false;
    }

    public boolean setXp(double xp) {
        if (xp < 0) {
            MessageUtils.warning("Attempted to set negative XP for team " + team.getName() + ". Ignoring.");
            return false;
        }
        this.currentXp = xp;
        Leaderboard.getInstance().updateTeam(team); // Notify leaderboard of XP change
        return true;
    }

    /**
     * Checks if the team can level up based on XP and requirements.
     *
     * @return true if the team can level up, false otherwise.
     */
    public boolean canLevelUp() {
        return currentXp >= LevelData.getNextLevelXp(level.getLevel()) && level.areRequirementsMet(team);
    }

    /**
     * Attempts to level up the team to the next level.
     *
     * @return Next Level if the team leveled up, Current Level if it cannot.
     */
    public Level levelUp() {
        while (canLevelUp()) {
            Level nextLevel = LevelData.getNextLevel(level.getLevel());
            if (nextLevel == null) {
                // No next level exists (e.g., max level reached)
                MessageUtils.debug("Team " + team.getName() + " reached max level: " + level.getLevel());
                return level;
            }

            // Check if requirements for the next level are met
            if (!nextLevel.areRequirementsMet(team)) {
                MessageUtils.debug("Team " + team.getName() + " does not meet requirements for level " + nextLevel.getLevel());
                return level;
            }

            // Level up: set new level and carry over excess XP
            this.currentXp -= LevelData.getNextLevelXp(level.getLevel());
            this.level = nextLevel;
            MessageUtils.debug("Team " + team.getName() + " leveled up to " + level.getLevel() + " with remaining XP: " + currentXp);
            Leaderboard.getInstance().updateTeam(team); // Notify leaderboard of level change
        }
        return level;
    }

    @Override
    public String toString() {
        return "TeamLevel{" +
                "level=" + level.getLevel() +
                ", currentXp=" + currentXp +
                '}';
    }
}