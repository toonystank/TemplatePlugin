package com.toonystank.requisiteteams.placeholders;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.level.LevelData;
import com.toonystank.requisiteteams.team.Leaderboard;
import com.toonystank.requisiteteams.team.Team;
import com.toonystank.requisiteteams.team.TeamManager;
import com.toonystank.requisiteteams.team.rank.Rank;
import com.toonystank.requisiteteams.team.rank.RankRegistry;
import com.toonystank.requisiteteams.utils.LanguageConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import com.toonystank.requisiteteams.utils.SmallLetterConvertor;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlaceholderRequest {

    private final RequisiteTeams plugin;
    private final LanguageConfig languageConfig;
    private final TeamManager teamManager;
    private final LevelData levelData;
    @Getter
    private static final Map<String, PlaceholderHandler> handlers = new HashMap<>();

    public PlaceholderRequest(RequisiteTeams plugin,
                              LanguageConfig languageConfig,
                              TeamManager teamManager,
                              LevelData levelData) {
        this.plugin = plugin;
        this.languageConfig = languageConfig;
        this.teamManager = teamManager;
        this.levelData = levelData;
        registerHandlers();
        if (plugin.isPapiHook()) {
            new PlaceholderRequisiteTeam().register();
        }
    }

    public static String parseRaw(RequisitePlayer player, String placeholder) {
        if (player == null || placeholder == null) return "";

        // Step 1: Resolve nested internal placeholders within {}
        String processed = resolveNestedInternalPlaceholders(player, placeholder);

        if (RequisiteTeams.getInstance().isPapiHook()) {
            processed = PlaceholderAPI.setBracketPlaceholders(player.getPlayer(), processed);
            if (processed.contains("{player")) {
                MessageUtils.toConsole("Player placeholder is not installed! Please install it by using /papi ecloud download Player", false);
            }
        }

        // Step 2: Resolve internal %requisiteteams_...% and %gpextension_...% placeholders
        Pattern pattern = Pattern.compile("%requisiteteams_([^%]+)%");
        Matcher matcher = pattern.matcher(processed);
        String result = processed;


        while (matcher.find()) {
            String fullPlaceholder = matcher.group(0); // e.g., %requisiteteams_getbyid_permission_1_7441a2b5-ebdc-40ae-95d5-87a71cea166e%
            String params = matcher.group(2); // e.g., getbyid_permission_1_7441a2b5-ebdc-40ae-95d5-87a71cea166e
            String[] args = params.split("_", 3); // Split into prefix, subcommand, and rest
            String prefix = args[0].toLowerCase();
            String suffix = args.length > 1 ? params.substring(prefix.length() + 1) : ""; // Keep full suffix after prefix


            PlaceholderHandler handler = handlers.get(prefix);
            String replacement = null;
            if (handler != null) {
                replacement = handler.handle(player, suffix, args, false);
            }
            if (replacement == null) {
                replacement = fullPlaceholder; // Keep original placeholder if no handler or null result
            }
            result = result.replace(fullPlaceholder, replacement);
        }

        // Step 4: Resolve remaining PlaceholderAPI %...% placeholders (e.g., %player_name%)
        if (RequisiteTeams.getInstance().isPapiHook()) {
            result = PlaceholderAPI.setPlaceholders(player.getPlayer(), result);
        }

        return result;
    }

    public static String parsePlaceholderString(RequisitePlayer player, String placeholder, boolean parseColor, boolean tinyText) {
        if (player == null || placeholder == null) return "";
        String result = parseRaw(player, placeholder);
        if (parseColor) {
            return MessageUtils.formatString(SmallLetterConvertor.convert(result), tinyText);
        } else {
            if (RequisiteTeams.getInstance().getMainConfig().isSmallText() && tinyText) {
                // Convert to TinyText if auto conversion is enabled
                return SmallLetterConvertor.convert(result);
            } else return result;
        }
    }

    public static Component parsePlaceholder(RequisitePlayer player, String placeholder, boolean parseColor, boolean tinyText) {
        if (player == null || placeholder == null) return Component.empty();

        String result = parseRaw(player, placeholder);
        if (parseColor) {
            return MessageUtils.format(result, tinyText);
        } else {
            if (RequisiteTeams.getInstance().getMainConfig().isSmallText() && tinyText) {
                // Convert to TinyText if auto conversion is enabled
                return MiniMessage.miniMessage().deserialize(SmallLetterConvertor.convert(result));
            }
            return MiniMessage.miniMessage().deserialize(result);
        }
    }

    private static String resolveNestedInternalPlaceholders(RequisitePlayer player, String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Limit recursion depth to prevent stack overflow
        final int MAX_RECURSION_DEPTH = 10;
        int recursionDepth = 0;

        // Updated regex to be more permissive and handle malformed placeholders
        Pattern nestedPattern = Pattern.compile("\\{(requisiteteams|gpextension)_([^}]*)(?:\\}|$)");
        Matcher matcher = nestedPattern.matcher(input);
        String result = input;

        while (matcher.find()) {
            if (recursionDepth >= MAX_RECURSION_DEPTH) {
                return result;
            }

            String fullNestedPlaceholder = matcher.group(0); // e.g., {requisiteteams_otherplayer_uuid_foirfe}
            String params = matcher.group(2); // e.g., otherplayer_uuid_foirfe
            String[] args = params.split("_", 2); // Limit split to avoid excessive splitting
            String prefix = args[0].toLowerCase();
            String suffix = args.length > 1 ? args[1] : "";


            PlaceholderHandler handler = handlers.get(prefix);
            String replacement = fullNestedPlaceholder; // Default to original placeholder

            if (handler != null) {
                try {
                    // Recursively resolve nested placeholders in the suffix
                    String resolvedSuffix = recursionDepth < MAX_RECURSION_DEPTH
                            ? resolveNestedInternalPlaceholders(player, suffix)
                            : suffix;
                    recursionDepth++;
                    replacement = handler.handle(player, resolvedSuffix, new String[]{prefix, resolvedSuffix}, false);
                    if (replacement == null) {
                        replacement = fullNestedPlaceholder; // Fallback to original if handler returns null
                    }
                } catch (Exception e) {
                    replacement = fullNestedPlaceholder; // Keep original on error
                }
            }

            result = result.replace(fullNestedPlaceholder, replacement);
            matcher = nestedPattern.matcher(result); // Reset matcher for new replacements
        }

        return result;
    }


    private void registerHandlers() {
        handlers.put("prefix", (player, suffix, args, handleFormatString) -> {
            if (handleFormatString) return MessageUtils.formatString(languageConfig.getPrefix());
            return languageConfig.getPrefix();
        });
        handlers.put("title", (player, suffix, args, handleFormatString) -> {
            if (!handleFormatString)
                return languageConfig.getTitle() == null || languageConfig.getTitle().isEmpty() ? "" : languageConfig.getTitle();
            return languageConfig.getTitle() == null || languageConfig.getTitle().isEmpty() ? "" : MessageUtils.formatString(languageConfig.getTitle());
        });
        handlers.put("otherplayer", new OtherPlayerHandler());
        handlers.put("online", new OnlineHandler());
        handlers.put("teambyuuid", new TeamsByUUIDHandler(teamManager));
        handlers.put("teambyname", new TeamsByNameHandler(teamManager));
        handlers.put("leaderboard", new LeaderboardHandler(teamManager));

    }

    interface PlaceholderHandler {
        String handle(RequisitePlayer player, String suffix, String[] args, boolean handleFormatString);
    }


    private static class OtherPlayerHandler implements PlaceholderHandler {
        @Override
        public String handle(RequisitePlayer player, String suffix, String[] args, boolean handleFormatString) {
            if (args.length < 3) return null;
            String otherPlayerName = args[2];
            RequisitePlayer otherPlayer = PlayerDataManager.getPlayer(otherPlayerName);
            if (otherPlayer == null) return null;
            return switch (args[1].toLowerCase()) {
                case "uuid" -> otherPlayer.getUuid().toString();
                case "displayname" -> otherPlayer.getName();
                default -> null;
            };
        }
    }

    private static class OnlineHandler implements PlaceholderHandler {
        @Override
        public String handle(RequisitePlayer player, String suffix, String[] args, boolean handleFormatString) {
            if (args.length < 2) return "null";
            return switch (args[1].toLowerCase()) {
                case "player" -> player.getOnlinePlayer() != null ? player.getOnlinePlayer().getName() : "null";
                case "playeruuid" -> player.getUuid().toString();
                case "online" -> String.valueOf(Bukkit.getOnlinePlayers().size());
                default -> "null";
            };
        }
    }


    public static class TeamsByNameHandler implements PlaceholderHandler {
        private final TeamManager teamManager;

        public TeamsByNameHandler(TeamManager teamManager) {
            this.teamManager = teamManager;
        }

        @Override
        public String handle(RequisitePlayer player, String suffix, String[] args, boolean handleFormatString) {
            if (args.length < 2) return "null";
            String teamName = args[0];
            if (!teamManager.isTeamExists(teamName)) return "null";
            Team team = TeamManager.getTeam(teamName);

            String[] placeholderParts = args[1].split(":");
            String mainPlaceholder = placeholderParts[0].toLowerCase();
            String subPlaceholder = placeholderParts.length > 1 ? placeholderParts[1].toLowerCase() : "";

            switch (mainPlaceholder) {
                case "name" -> {
                    return team.getName();
                }
                case "uuid" -> {
                    return team.getTeamUUID().toString();
                }
                case "members" -> {
                    return String.valueOf(team.getMembers().size());
                }
                case "level" -> {
                    return String.valueOf(team.getTeamLevel().getLevel().getLevel());
                }
                case "xp" -> {
                    return String.format("%.2f", team.getTeamLevel().getCurrentXp());
                }
                case "balance" -> {
                    return String.valueOf(team.getTeamBalance());
                }
                case "owner" -> {
                    if (!subPlaceholder.equals("name")) return "null";
                    RequisitePlayer owner = team.getPlayersByRank(RankRegistry.getOwnerRank())
                            .stream()
                            .findFirst()
                            .orElse(null);
                    return owner != null ? owner.getName() : "null";
                }
                case "player" -> {
                    if (player == null) return "null";
                    RequisitePlayer teamPlayer = team.getTeamPlayer(player.getUuid());
                    if (teamPlayer == null) return "null";
                    return switch (subPlaceholder) {
                        case "rank" -> teamPlayer.getRank() != null ? teamPlayer.getRank().getName() : "null";
                        case "xp" -> String.format("%.2f", teamPlayer.getCollectedXP());
                        default -> "null";
                    };
                }
                case "has" -> {
                    if (player == null || placeholderParts.length < 2) return "false";
                    RequisitePlayer teamPlayer = team.getTeamPlayer(player.getUuid());
                    if (teamPlayer == null || teamPlayer.getRank() == null) return "false";
                    return switch (subPlaceholder) {
                        case "permission" -> {
                            if (args.length < 3) yield "false";
                            yield String.valueOf(teamPlayer.getRank().hasPermission(args[2]));
                        }
                        case "rank" -> {
                            if (args.length < 3) yield "false";
                            yield String.valueOf(teamPlayer.getRank().getName().equalsIgnoreCase(args[2]));
                        }
                        default -> "false";
                    };
                }
                case "players" -> {
                    if (args.length < 3) return "null";
                    if (!subPlaceholder.equals("by_rank")) return "null";
                    Rank rank = RankRegistry.getRank(args[2]);
                    if (rank == null) return "null";
                    return team.getPlayersByRank(rank)
                            .stream()
                            .map(RequisitePlayer::getName)
                            .collect(Collectors.joining(", "));
                }
                case "leaderboard" -> {
                    if (!subPlaceholder.equals("position")) return "null";
                    int rank = Leaderboard.getInstance().getTeamRank(team);
                    return rank > 0 ? String.valueOf(rank) : "null";
                }
                default -> {
                    return "null";
                }
            }
        }
    }

    public static class TeamsByUUIDHandler implements PlaceholderHandler {
        private final TeamManager teamManager;

        public TeamsByUUIDHandler(TeamManager teamManager) {
            this.teamManager = teamManager;
        }

        @Override
        public String handle(RequisitePlayer player, String suffix, String[] args, boolean handleFormatString) {
            if (args.length < 2) return "null";
            try {
                UUID teamUUID = UUID.fromString(args[0]);
                if (!teamManager.isTeamExists(teamUUID)) return "null";
                Team team = TeamManager.getTeam(teamUUID);
                if (team == null) return "null team";

                String[] placeholderParts = args[1].split(":");
                String mainPlaceholder = placeholderParts[0].toLowerCase();
                String subPlaceholder = placeholderParts.length > 1 ? placeholderParts[1].toLowerCase() : "";

                switch (mainPlaceholder) {
                    case "name" -> {
                        return team.getName();
                    }
                    case "uuid" -> {
                        return team.getTeamUUID().toString();
                    }
                    case "members" -> {
                        return String.valueOf(team.getMembers().size());
                    }
                    case "level" -> {
                        return String.valueOf(team.getTeamLevel().getLevel().getLevel());
                    }
                    case "xp" -> {
                        return String.format("%.2f", team.getTeamLevel().getCurrentXp());
                    }
                    case "balance" -> {
                        return String.valueOf(team.getTeamBalance());
                    }
                    case "owner" -> {
                        if (!subPlaceholder.equals("name")) return "null";
                        RequisitePlayer owner = team.getPlayersByRank(RankRegistry.getOwnerRank())
                                .stream()
                                .findFirst()
                                .orElse(null);
                        return owner != null ? owner.getName() : "null";
                    }
                    case "player" -> {
                        if (player == null) return "null";
                        RequisitePlayer teamPlayer = team.getTeamPlayer(player.getUuid());
                        if (teamPlayer == null) return "null";
                        return switch (subPlaceholder) {
                            case "rank" -> teamPlayer.getRank() != null ? teamPlayer.getRank().getName() : "null";
                            case "xp" -> String.format("%.2f", teamPlayer.getCollectedXP());
                            default -> "null";
                        };
                    }
                    case "has" -> {
                        if (player == null || placeholderParts.length < 2) return "false";
                        RequisitePlayer teamPlayer = team.getTeamPlayer(player.getUuid());
                        if (teamPlayer == null || teamPlayer.getRank() == null) return "false";
                        return switch (subPlaceholder) {
                            case "permission" -> {
                                if (args.length < 3) yield "false";
                                yield String.valueOf(teamPlayer.getRank().hasPermission(args[2]));
                            }
                            case "rank" -> {
                                if (args.length < 3) yield "false";
                                yield String.valueOf(teamPlayer.getRank().getName().equalsIgnoreCase(args[2]));
                            }
                            default -> "false";
                        };
                    }
                    case "players" -> {
                        if (args.length < 3) return "null";
                        if (!subPlaceholder.equals("by_rank")) return "null";
                        Rank rank = RankRegistry.getRank(args[2]);
                        if (rank == null) return "null";
                        return team.getPlayersByRank(rank)
                                .stream()
                                .map(RequisitePlayer::getName)
                                .collect(Collectors.joining(", "));
                    }
                    case "leaderboard" -> {
                        if (!subPlaceholder.equals("position")) return "null";
                        int rank = Leaderboard.getInstance().getTeamRank(team);
                        return rank > 0 ? String.valueOf(rank) : "null";
                    }
                    default -> {
                        return "null";
                    }
                }
            } catch (IllegalArgumentException e) {
                return "null";
            }
        }
    }

    public static class LeaderboardHandler implements PlaceholderHandler {
        private final TeamManager teamManager;

        public LeaderboardHandler(TeamManager teamManager) {
            this.teamManager = teamManager;
        }

        @Override
        public String handle(RequisitePlayer player, String suffix, String[] args, boolean handleFormatString) {
            MessageUtils.debug("Handling leaderboard placeholder with args: " + Arrays.toString(args) +
                    ", suffix: " + suffix + ", handleFormatString: " + handleFormatString);
            if (args.length > 3) return "null";
            try {
                int position = Integer.parseInt(args[1]);
                Team team = Leaderboard.getInstance().getTeamAtRank(position);
                MessageUtils.debug("Retrieved team at position " + position + ": " + (team != null ? team.getName() : "null"));
                if (team == null) return "null";

                String[] placeholderParts = args[2].split(":");
                String mainPlaceholder = placeholderParts[0].toLowerCase();
                String subPlaceholder = placeholderParts.length > 1 ? placeholderParts[1].toLowerCase() : "";

                MessageUtils.debug("Main placeholder: " + mainPlaceholder + ", Sub placeholder: " + subPlaceholder);

                switch (mainPlaceholder) {
                    case "name" -> {
                        return team.getName();
                    }
                    case "uuid" -> {
                        return team.getTeamUUID().toString();
                    }
                    case "members" -> {
                        return String.valueOf(team.getMembers().size());
                    }
                    case "level" -> {
                        return String.valueOf(team.getTeamLevel().getLevel().getLevel());
                    }
                    case "xp" -> {
                        return String.format("%.2f", team.getTeamLevel().getCurrentXp());
                    }
                    case "balance" -> {
                        return String.valueOf(team.getTeamBalance());
                    }
                    case "owner" -> {
                        if (!subPlaceholder.equals("name")) return "null";
                        RequisitePlayer owner = team.getPlayersByRank(RankRegistry.getOwnerRank())
                                .stream()
                                .findFirst()
                                .orElse(null);
                        return owner != null ? owner.getName() : "null";
                    }
                    case "players" -> {
                        if (!subPlaceholder.equals("by_rank")) return "null";
                        Rank rank = RankRegistry.getRank(args[2]);
                        if (rank == null) return "null";
                        return team.getPlayersByRank(rank)
                                .stream()
                                .map(RequisitePlayer::getName)
                                .collect(Collectors.joining(", "));
                    }
                    default -> {
                        return "null";
                    }
                }
            } catch (NumberFormatException e) {
                return "null";
            }
        }
    }

}