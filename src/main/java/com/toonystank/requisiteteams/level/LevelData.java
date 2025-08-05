package com.toonystank.requisiteteams.level;

import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI; // Assume PlaceholderAPI dependency
import org.bukkit.entity.Player; // For player context
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extends FileConfig to load levels from levels.yml with support for level ranges, default requirements, and placeholders.
 */
public class LevelData extends FileConfig {

    private static final Map<Integer, Level> levelMap = new HashMap<>();

    /**
     * Initializes the LevelData for levels.yml.
     *
     * @throws IOException If file operations fail.
     */
    public LevelData() throws IOException {
        super("levels.yml", false, true);
    }

    /**
     * Gets a level by its number.
     *
     * @param levelNum The level number.
     * @return The Level object, or null if not found.
     */
    public static Level getLevel(int levelNum) {
        if (levelMap.isEmpty()) {
            MessageUtils.toConsole("Level map is empty, loading levels...", false);
            try {
                new LevelData().loadLevels();
            } catch (IOException e) {
                MessageUtils.toConsole("Failed to load levels: " + e.getMessage(), true);
                return null;
            }
        }
        Level level = levelMap.get(levelNum);
        if (level == null) {
            MessageUtils.toConsole("Level " + levelNum + " not found in level map.", true);
        }
        return level;
    }

    /**
     * Gets all loaded levels.
     *
     * @return An unmodifiable map of levels.
     */
    public static Map<Integer, Level> getLevelMap() {
        return Map.copyOf(levelMap); // Return unmodifiable copy
    }

    /**
     * Loads levels from levels.yml and populates the static level map.
     * Supports level-range, default requirements, specific level overrides, and placeholders.
     *
     * @throws IOException If the file cannot be read or parsed.
     */
    public void loadLevels() throws IOException {
        levelMap.clear();

        // Parse level-range
        String range = getString("level-range", "1-1");
        Pattern rangePattern = Pattern.compile("^(\\d+)-(\\d+)$");
        Matcher matcher = rangePattern.matcher(range);
        if (!matcher.matches()) {
            throw new IOException("Invalid level-range format in " + fileName + ": " + range);
        }
        int minLevel = Integer.parseInt(matcher.group(1));
        int maxLevel = Integer.parseInt(matcher.group(2));
        if (minLevel < 1 || maxLevel < minLevel) {
            throw new IOException("Invalid level range: minLevel=" + minLevel + ", maxLevel=" + maxLevel);
        }

        // Parse default requirements
        List<Level.Requirement> defaultRequirements = parseRequirements(getConfig().getMapList("default-requirements"));

        // Parse default xpRequired pattern
        int baseXp = getInt("default-xp.base", 1000);
        int incrementXp = getInt("default-xp.increment", 500);

        // Generate levels
        for (int levelNum = minLevel; levelNum <= maxLevel; levelNum++) {
            int xpRequired = baseXp + (levelNum - 1) * incrementXp;
            List<Level.Requirement> requirements = new ArrayList<>(defaultRequirements);

            // Check for level-specific overrides
            String path = "levels." + levelNum;
            if (getConfigurationSection(path) != null) {
                xpRequired = getInt(path + ".xpRequired", xpRequired);
                List<Map<?, ?>> levelReqs = getConfig().getMapList(path + ".requirements");
                if (!levelReqs.isEmpty()) {
                    requirements = parseRequirements(levelReqs);
                }
            }

            // Build and store level
            Level level = new Level.Builder(levelNum)
                    .setXpRequired(xpRequired)
                    .addRequirements(requirements)
                    .build();
            levelMap.put(levelNum, level);
        }

        MessageUtils.toConsole("Loaded " + levelMap.size() + " levels from " + fileName, false);
    }

    /**
     * Parses a list of requirements from a YAML map list.
     *
     * @param reqs List of requirement maps from YAML.
     * @return List of Level.Requirement objects.
     * @throws IOException If requirement format is invalid.
     */
    private List<Level.Requirement> parseRequirements(List<Map<?, ?>> reqs) throws IOException {
        List<Level.Requirement> requirements = new ArrayList<>();
        if (reqs == null) {
            return requirements;
        }
        for (Map<?, ?> req : reqs) {
            String type = (String) req.get("type");
            Object value = req.get("value");
            if (type == null || value == null) {
                throw new IOException("Invalid requirement format: type=" + type + ", value=" + value);
            }
            if (type.startsWith("placeholder-")) {
                String placeholder = type.substring("placeholder-".length());
                requirements.add(new Level.PlaceholderRequirement(placeholder, String.valueOf(value)));
            } else {
                switch (type) {
                    case "member_count":
                        requirements.add(new Level.MemberCountRequirement((int) value));
                        break;
                    case "vault_balance":
                        requirements.add(new Level.VaultBalanceRequirement(((Number) value).doubleValue()));
                        break;
                    default:
                        if (type.startsWith("custom_")) {
                            requirements.add(new Level.CustomRequirement(type.substring(7), value));
                        } else {
                            throw new IOException("Unknown requirement type: " + type);
                        }
                }
            }
        }
        return requirements;
    }

    public static int getNextLevelXp(int currentLevel) {
        MessageUtils.debug("Getting next level XP for current level: " + currentLevel);
        Level level = getLevel(currentLevel);
        if (level == null) {
            MessageUtils.toConsole("Level " + currentLevel + " not found, returning 0 XP", true);
            return 0;
        }
        MessageUtils.debug("Next level XP for level " + currentLevel + ": " + level.getXpRequired());
        return level.getXpRequired();
    }

    public static Level getNextLevel(int currentLevel) {
        Level level = getLevel(currentLevel + 1);
        if (level == null) {
            MessageUtils.toConsole("Next level " + (currentLevel + 1) + " not found, returning null", true);
            return null;
        }
        return level;
    }

    /**
     * Reloads the levels.yml file and updates the static level map.
     *
     * @throws IOException If the file cannot be reloaded or parsed.
     */
    @Override
    public void reload() throws IOException {
        super.reload();
        loadLevels();
    }
}