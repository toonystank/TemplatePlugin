package com.toonystank.requisiteteams.level;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.team.Team;
import com.toonystank.requisiteteams.utils.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI; // Assume PlaceholderAPI dependency
import org.bukkit.entity.Player; // For player context
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class representing a team level with extensible requirements loaded from level.yml.
 */
public class Level {
    @Getter
    private final int level;
    @Getter
    private final int xpRequired;
    private final List<Requirement> requirements;

    /**
     * Constructor for Level.
     *
     * @param level       The level number.
     * @param xpRequired  The experience points required to reach this level.
     * @param requirements List of requirements for this level.
     */
    public Level(int level, int xpRequired, List<Requirement> requirements) {
        this.level = level;
        this.xpRequired = xpRequired;
        this.requirements = new ArrayList<>(requirements); // Defensive copy
    }

    public List<Requirement> getRequirements() {
        return new ArrayList<>(requirements); // Return a copy to prevent modification
    }

    /**
     * Checks if all requirements for this level are met by the team.
     *
     * @param team The team to check requirements against.
     * @return true if all requirements are satisfied, false otherwise.
     */
    public boolean areRequirementsMet(Team team) {
        // Assume team.getLeader() returns a Player object for placeholder resolution
        RequisitePlayer player = PlayerDataManager.getPlayer(team.getOwnerUUID());
        if (player == null) {
            MessageUtils.toConsole("No leader found for team, cannot resolve placeholders", true);
            return false;
        }
        TeamContext teamContext = new TeamContext(team.getMembers().size(), team.getTeamBalance(), player);
        return requirements.stream().allMatch(req -> {
            MessageUtils.debug("Checking requirement: " + req.getType() + " with value: " + req.getValue());
            return req.isSatisfied(teamContext);
        });
    }

    /**
     * Interface for a requirement that can be checked for a team.
     */
    public interface Requirement {
        boolean isSatisfied(TeamContext teamContext);
        String getType();
        Object getValue();
    }

    /**
     * Context class to hold team-related data for requirement checking.
     */
    public static class TeamContext {
        @Getter
        private final int memberCount;
        @Getter
        private final double vaultBalance;
        @Getter
        private final RequisitePlayer player; // Added for placeholder resolution
        private final Map<String, Object> customAttributes;

        public TeamContext(int memberCount, double vaultBalance, RequisitePlayer player) {
            this.memberCount = memberCount;
            this.vaultBalance = vaultBalance;
            this.player = player;
            this.customAttributes = new HashMap<>();
        }

        public void setCustomAttribute(String key, Object value) {
            customAttributes.put(key, value);
        }

        public Object getCustomAttribute(String key) {
            return customAttributes.get(key);
        }
    }

    /**
     * Requirement for minimum number of team members.
     */
    public static class MemberCountRequirement implements Requirement {
        private final int requiredMembers;

        public MemberCountRequirement(int requiredMembers) {
            this.requiredMembers = requiredMembers;
        }

        @Override
        public boolean isSatisfied(TeamContext teamContext) {
            return teamContext.getMemberCount() >= requiredMembers;
        }

        @Override
        public String getType() {
            return "member_count";
        }

        @Override
        public Object getValue() {
            return requiredMembers;
        }
    }

    /**
     * Requirement for minimum vault balance.
     */
    public static class VaultBalanceRequirement implements Requirement {
        private final double requiredBalance;

        public VaultBalanceRequirement(double requiredBalance) {
            this.requiredBalance = requiredBalance;
        }

        @Override
        public boolean isSatisfied(TeamContext teamContext) {
            return teamContext.getVaultBalance() >= requiredBalance;
        }

        @Override
        public String getType() {
            return "vault_balance";
        }

        @Override
        public Object getValue() {
            return requiredBalance;
        }
    }

    /**
     * Requirement for a PlaceholderAPI placeholder value.
     */
    public static class PlaceholderRequirement implements Requirement {
        private final String placeholder;
        private final String requiredValue;

        public PlaceholderRequirement(String placeholder, String requiredValue) {
            this.placeholder = placeholder;
            this.requiredValue = requiredValue;
        }

        @Override
        public boolean isSatisfied(TeamContext teamContext) {
            Player player = teamContext.getPlayer().getOnlinePlayer();
            if (player == null) {
                MessageUtils.toConsole("No player context for placeholder: " + placeholder, true);
                return false;
            }
            String result = PlaceholderAPI.setPlaceholders(player, placeholder);
            if (result == null) {
                MessageUtils.toConsole("Placeholder " + placeholder + " returned null", true);
                return false;
            }
            return result.equals(requiredValue);
        }

        @Override
        public String getType() {
            return "placeholder-" + placeholder;
        }

        @Override
        public Object getValue() {
            return requiredValue;
        }
    }

    /**
     * Custom requirement for future extensions.
     */
    public static class CustomRequirement implements Requirement {
        private final String key;
        private final Object requiredValue;

        public CustomRequirement(String key, Object requiredValue) {
            this.key = key;
            this.requiredValue = requiredValue;
        }

        @Override
        public boolean isSatisfied(TeamContext teamContext) {
            Object actualValue = teamContext.getCustomAttribute(key);
            return requiredValue.equals(actualValue);
        }

        @Override
        public String getType() {
            return "custom_" + key;
        }

        @Override
        public Object getValue() {
            return requiredValue;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Level level1)) return false;
        return level == level1.level &&
                xpRequired == level1.xpRequired &&
                requirements.equals(level1.requirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, xpRequired, requirements);
    }

    /**
     * Builder for creating Level instances from YAML configuration.
     */
    public static class Builder {
        private final int level;
        private int xpRequired;
        private final List<Requirement> requirements;

        public Builder(int level) {
            this.level = level;
            this.xpRequired = 0;
            this.requirements = new ArrayList<>();
        }

        public Builder setXpRequired(int xpRequired) {
            this.xpRequired = xpRequired;
            return this;
        }

        public Builder addRequirement(Requirement requirement) {
            this.requirements.add(requirement);
            return this;
        }
        public Builder addRequirements(List<Requirement> requirements) {
            this.requirements.addAll(requirements);
            return this;
        }

        public Level build() {
            return new Level(level, xpRequired, requirements);
        }
    }
}