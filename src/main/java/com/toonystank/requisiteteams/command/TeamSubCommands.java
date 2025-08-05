package com.toonystank.requisiteteams.command;

import com.toonystank.requisiteteams.command.manager.SubCommand;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.level.Level;
import com.toonystank.requisiteteams.team.Team;
import com.toonystank.requisiteteams.team.TeamManager;
import com.toonystank.requisiteteams.team.rank.Rank;
import com.toonystank.requisiteteams.team.rank.RankPermissions;
import com.toonystank.requisiteteams.team.rank.RankRegistry;
import com.toonystank.requisiteteams.utils.Handlers;
import com.toonystank.requisiteteams.utils.MessageUtils;
import com.toonystank.requisiteteams.team.Leaderboard;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamSubCommands {

    public static class CreateCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }
            if (args.length < 1) {
                sender.sendMessage("Usage: /teams create <teamName>");
                return;
            }
            RequisitePlayer requisitePlayer = PlayerDataManager.getPlayer(player.getUniqueId());
            if (requisitePlayer == null) {
                sender.sendMessage("Player data not found.");
                return;
            }
            if (TeamManager.getInstance().isPlayerInTeam(requisitePlayer)) {
                sender.sendMessage("You are already in a team.");
                return;
            }
            String teamName = args[0];
            if (TeamManager.getTeam(teamName) != null) {
                sender.sendMessage("A team with the name " + teamName + " already exists.");
                return;
            }
            try {
                UUID teamUUID = UUID.randomUUID();
                Team team = new Team(teamUUID, teamName, null,requisitePlayer.getUuid()); // TeamLevel will be set in registerTeam
                Rank ownerRank = RankRegistry.getOwnerRank();
                if (ownerRank == null) {
                    sender.sendMessage("No owner rank defined. Contact an administrator.");
                    return;
                }
                TeamManager.getInstance().registerTeam(team); // Use the provided registerTeam method
                team.addToTeam(requisitePlayer, ownerRank);
                sender.sendMessage("Team " + teamName + " created successfully!");
            } catch (IOException e) {
                sender.sendMessage("Failed to create team: " + e.getMessage());
                MessageUtils.toConsole("Error creating team " + teamName + ": " + e.getMessage(), true);
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.create");
        }
    }

    public static class AddXpCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /teams addxp <team> <amount>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            try {
                double xp = Double.parseDouble(args[1]);
                if (xp <= 0) {
                    sender.sendMessage("XP amount must be positive.");
                    return;
                }
                Level newLevel = team.getTeamLevel().addXp(xp);
                sender.sendMessage("Added " + xp + " XP to team " + teamName + ". Current level: " + newLevel.getLevel());
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid XP amount: " + args[1]);
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.admin");
        }
    }

    public static class SetXpCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /teams setxp <team> <amount>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            try {
                double xp = Double.parseDouble(args[1]);
                if (xp < 0) {
                    sender.sendMessage("XP amount cannot be negative.");
                    return;
                }
                team.getTeamLevel().setXp(xp);
                Level newLevel = team.getTeamLevel().levelUp();
                sender.sendMessage("Set XP for team " + teamName + " to " + xp + ". Current level: " + newLevel.getLevel());
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid XP amount: " + args[1]);
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.admin");
        }
    }

    public static class GetXpCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage("Usage: /teams getxp <team>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            sender.sendMessage("Team " + teamName + " has " + team.getTeamLevel().getCurrentXp() + " XP and is at level " + team.getTeamLevel().getLevel().getLevel());
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.use");
        }
    }

    public static class DeleteCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage("Usage: /teams delete <team>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            if (sender instanceof Player) {
                RequisitePlayer player = PlayerDataManager.getPlayer(((Player) sender).getName());
                if (player == null || !player.getTeamUUID().equals(team.getTeamUUID()) || !player.getRank().isOwnerRank()) {
                    sender.sendMessage("You must be the team owner to delete the team.");
                    return;
                }
            }
            boolean deleted = TeamManager.getInstance().deleteTeam(teamName);
            if (deleted) {
                sender.sendMessage("Team " + teamName + " deleted successfully.");
            } else {
                sender.sendMessage("Failed to delete team " + teamName + ".");
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.admin") || sender instanceof Player;
        }
    }

    public static class AddCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /teams add <team> <player>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            RequisitePlayer requisitePlayer = PlayerDataManager.getPlayer(args[1]);
            if (requisitePlayer == null) {
                sender.sendMessage("Player " + args[1] + " not found.");
                return;
            }
            RequisitePlayer senderPlayer = PlayerDataManager.getPlayer(sender.getName());
            if (senderPlayer == null || !senderPlayer.getTeamUUID().equals(team.getTeamUUID()) || !senderPlayer.getRank().hasPermission(RankPermissions.ADD_MEMBER)) {
                sender.sendMessage("You don't have permission to add members to this team.");
                return;
            }
            if (TeamManager.getInstance().isPlayerInTeam(requisitePlayer)) {
                sender.sendMessage("Player " + args[1] + " is already in a team.");
                return;
            }
            Rank defaultRank = RankRegistry.getDefaultRank();
            if (defaultRank == null) {
                sender.sendMessage("No default rank defined. Contact an administrator.");
                return;
            }
            boolean added = team.addToTeam(requisitePlayer, defaultRank);
            if (added) {
                sender.sendMessage("Added " + args[1] + " to team " + teamName + ".");
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    target.sendMessage("You have been added to team " + teamName + ".");
                }
            } else {
                sender.sendMessage("Failed to add " + args[1] + " to team " + teamName + ".");
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                return Arrays.stream(Bukkit.getOfflinePlayers())
                        .map(p -> p.getName())
                        .filter(Objects::nonNull)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return sender instanceof Player;
        }
    }

    public static class KickCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /teams kick <team> <player>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            RequisitePlayer requisitePlayer = PlayerDataManager.getPlayer(args[1]);
            if (requisitePlayer == null) {
                sender.sendMessage("Player " + args[1] + " not found.");
                return;
            }
            RequisitePlayer senderPlayer = PlayerDataManager.getPlayer(sender.getName());
            if (senderPlayer == null || !senderPlayer.getTeamUUID().equals(team.getTeamUUID()) || !senderPlayer.getRank().hasPermission(RankPermissions.REMOVE_MEMBER)) {
                sender.sendMessage("You don't have permission to kick members from this team.");
                return;
            }
            if (!team.getMembers().contains(requisitePlayer.getUuid())) {
                sender.sendMessage("Player " + args[1] + " is not in team " + teamName + ".");
                return;
            }
            if (requisitePlayer.getRank().isOwnerRank()) {
                sender.sendMessage("You cannot kick the team owner.");
                return;
            }
            boolean removed = team.removeFromTeam(requisitePlayer);
            if (removed) {
                sender.sendMessage("Kicked " + args[1] + " from team " + teamName + ".");
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    target.sendMessage("You have been kicked from team " + teamName + ".");
                }
                requisitePlayer.setPlayerTeam(null, null);
            } else {
                sender.sendMessage("Failed to kick " + args[1] + " from team " + teamName + ".");
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                Team team = TeamManager.getTeam(args[0]);
                if (team != null) {
                    return team.getMembers().stream()
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .filter(Objects::nonNull)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .sorted()
                            .collect(Collectors.toList());
                }
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return sender instanceof Player;
        }
    }

    public static class PromoteCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be used by players.");
                return;
            }
            if (args.length < 2) {
                sender.sendMessage("Usage: /teams promote <team> <player>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            RequisitePlayer requisitePlayer = PlayerDataManager.getPlayer(args[1]);
            if (requisitePlayer == null) {
                sender.sendMessage("Player " + args[1] + " not found.");
                return;
            }
            RequisitePlayer senderPlayer = PlayerDataManager.getPlayer(sender.getName());
            if (senderPlayer == null || !senderPlayer.getTeamUUID().equals(team.getTeamUUID()) || !senderPlayer.getRank().hasPermission(RankPermissions.PROMOTE_MEMBER)) {
                sender.sendMessage("You don't have permission to promote members in this team.");
                return;
            }
            if (!team.getMembers().contains(requisitePlayer.getUuid())) {
                sender.sendMessage("Player " + args[1] + " is not in team " + teamName + ".");
                return;
            }
            Rank currentRank = requisitePlayer.getRank();
            Rank nextRank = RankRegistry.getRank(currentRank.getParent());
            if (nextRank == null || nextRank.isOwnerRank()) {
                sender.sendMessage("Cannot promote " + args[1] + " further.");
                return;
            }
            if (senderPlayer.getRank().isLowerThan(nextRank)) {
                sender.sendMessage("You cannot promote to a rank higher than your own.");
                return;
            }
            requisitePlayer.setPlayerTeam(team.getTeamUUID(), nextRank);
            sender.sendMessage("Promoted " + args[1] + " to " + nextRank.getDisplayName() + " in team " + teamName + ".");
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                target.sendMessage("You have been promoted to " + nextRank.getDisplayName() + " in team " + teamName + ".");
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                Team team = TeamManager.getTeam(args[0]);
                if (team != null) {
                    return team.getMembers().stream()
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .filter(Objects::nonNull)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .sorted()
                            .collect(Collectors.toList());
                }
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return sender instanceof Player;
        }
    }

    public static class SetRankCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /teams setrank <team> <player> <rank>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            RequisitePlayer requisitePlayer = PlayerDataManager.getPlayer(args[1]);
            if (requisitePlayer == null) {
                sender.sendMessage("Player " + args[1] + " not found.");
                return;
            }
            if (!team.getMembers().contains(requisitePlayer.getUuid())) {
                sender.sendMessage("Player " + args[1] + " is not in team " + teamName + ".");
                return;
            }
            Rank rank = RankRegistry.getRank(args[2]);
            if (rank == null) {
                sender.sendMessage("Rank " + args[2] + " not found.");
                return;
            }
            if (sender instanceof Player) {
                RequisitePlayer senderPlayer = PlayerDataManager.getPlayer(sender.getName());
                if (senderPlayer == null || !senderPlayer.getTeamUUID().equals(team.getTeamUUID()) || !senderPlayer.getRank().hasPermission(RankPermissions.SET_RANK)) {
                    sender.sendMessage("You don't have permission to set ranks in this team.");
                    return;
                }
                if (senderPlayer.getRank().isLowerThan(rank)) {
                    sender.sendMessage("You cannot set a rank higher than your own.");
                    return;
                }
            }
            requisitePlayer.setPlayerTeam(team.getTeamUUID(), rank);
            sender.sendMessage("Set rank of " + args[1] + " to " + rank.getDisplayName() + " in team " + teamName + ".");
            Player target = Bukkit.getPlayer(args[1]);
            if (target != null) {
                target.sendMessage("Your rank in team " + teamName + " has been set to " + rank.getDisplayName() + ".");
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            } else if (args.length == 2) {
                Team team = TeamManager.getTeam(args[0]);
                if (team != null) {
                    return team.getMembers().stream()
                            .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                            .filter(Objects::nonNull)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .sorted()
                            .collect(Collectors.toList());
                }
            } else if (args.length == 3) {
                return RankRegistry.getRanks().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.admin") || sender instanceof Player;
        }
    }

    public static class InfoCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sender.sendMessage("Usage: /teams info <team>");
                return;
            }
            String teamName = args[0];
            Team team = TeamManager.getTeam(teamName);
            if (team == null) {
                sender.sendMessage("Team " + teamName + " not found.");
                return;
            }
            sender.sendMessage("Team: " + team.getName());
            sender.sendMessage("Level: " + team.getTeamLevel().getLevel().getLevel());
            sender.sendMessage("XP: " + team.getTeamLevel().getCurrentXp());
            sender.sendMessage("Balance: " + team.getTeamBalance());
            sender.sendMessage("Members (" + team.getMembers().size() + "):");
            for (UUID uuid : team.getMembers()) {
                RequisitePlayer player = team.getTeamPlayer(uuid);
                if (player != null) {
                    sender.sendMessage("- " + Bukkit.getOfflinePlayer(uuid).getName() + " (" + player.getRank().getDisplayName() + ")");
                }
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            if (args.length == 1) {
                return TeamManager.getTeamMap().keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                        .sorted()
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.use");
        }
    }

    public static class ListCommand implements SubCommand {
        @Override
        public void execute(CommandSender sender, String[] args) {
            if (TeamManager.getTeamMap().isEmpty()) {
                sender.sendMessage("No teams exist.");
                return;
            }
            sender.sendMessage("Teams (" + TeamManager.getTeamMap().size() + "):");
            for (Team team : TeamManager.getTeamMap().values()) {
                sender.sendMessage("- " + team.getName() + " (Level: " + team.getTeamLevel().getLevel().getLevel() + ", Members: " + team.getMembers().size() + ")");
            }
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            return Collections.emptyList();
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.use");
        }
    }

    public static class LeaderboardCommand implements SubCommand {
        private static final int DEFAULT_TOP_N = 10;
        private static final int MAX_PER_PAGE = 10;

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (args.length < 1) {
                sendUsage(sender);
                return;
            }

            String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "top":
                    handleTopCommand(sender, args);
                    break;
                case "rank":
                    handleRankCommand(sender, args);
                    break;
                case "list":
                    handleListCommand(sender, args);
                    break;
                case "refresh":
                    handleRefreshCommand(sender);
                    break;
                default:
                    sendUsage(sender);
            }
        }

        private void sendUsage(CommandSender sender) {
            sender.sendMessage("§6Leaderboard Commands:");
            sender.sendMessage("§e/teams leaderboard top [n] §7- Show the top N teams (default 10)");
            sender.sendMessage("§e/teams leaderboard rank <team> §7- Show the rank of a specific team");
            sender.sendMessage("§e/teams leaderboard list [page] §7- List all teams (paginated)");
            sender.sendMessage("§e/teams leaderboard refresh §7- Force refresh leaderboard (admin only)");
        }

        private void handleTopCommand(CommandSender sender, String[] args) {
            int n = DEFAULT_TOP_N;
            if (args.length > 1) {
                try {
                    n = Integer.parseInt(args[1]);
                    if (n <= 0) {
                        sender.sendMessage("§cPlease specify a positive number.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number: " + args[1]);
                    return;
                }
            }

            List<Team> topTeams = Leaderboard.getInstance().getTopTeams(n);
            if (topTeams.isEmpty()) {
                sender.sendMessage("§cNo teams are currently ranked.");
                return;
            }

            sender.sendMessage("§6Top " + Math.min(n, topTeams.size()) + " Teams:");
            for (int i = 0; i < topTeams.size(); i++) {
                Team team = topTeams.get(i);
                sender.sendMessage(String.format("§e%d. %s §7- Level: %d, XP: %.2f",
                        i + 1, team.getName(), team.getTeamLevel().getLevel().getLevel(), team.getTeamLevel().getCurrentXp()));
            }
        }

        private void handleRankCommand(CommandSender sender, String[] args) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /teams leaderboard rank <team>");
                return;
            }

            String teamName = args[1];
            Team team = TeamManager.getTeam(teamName);
            if (team == null || team.getTeamLevel() == null) {
                sender.sendMessage("§cTeam " + teamName + " not found or not ranked.");
                return;
            }

            int rank = Leaderboard.getInstance().getTeamRank(team);
            if (rank == -1) {
                sender.sendMessage("§cTeam " + teamName + " is not ranked.");
                return;
            }

            sender.sendMessage(String.format("§eTeam %s is ranked #%d §7- Level: %d, XP: %.2f",
                    teamName, rank, team.getTeamLevel().getLevel().getLevel(), team.getTeamLevel().getCurrentXp()));
        }

        private void handleListCommand(CommandSender sender, String[] args) {
            int page = 1;
            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                    if (page <= 0) {
                        sender.sendMessage("§cPlease specify a positive page number.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid page number: " + args[1]);
                    return;
                }
            }

            List<Team> allTeams = Leaderboard.getInstance().getTopTeams(Integer.MAX_VALUE);
            if (allTeams.isEmpty()) {
                sender.sendMessage("§cNo teams are currently ranked.");
                return;
            }

            int totalPages = (int) Math.ceil((double) allTeams.size() / MAX_PER_PAGE);
            if (page > totalPages) {
                sender.sendMessage("§cPage " + page + " does not exist. Max pages: " + totalPages);
                return;
            }

            int start = (page - 1) * MAX_PER_PAGE;
            int end = Math.min(start + MAX_PER_PAGE, allTeams.size());

            sender.sendMessage("§6Leaderboard (Page " + page + "/" + totalPages + "):");
            for (int i = start; i < end; i++) {
                Team team = allTeams.get(i);
                sender.sendMessage(String.format("§e%d. %s §7- Level: %d, XP: %.2f",
                        i + 1, team.getName(), team.getTeamLevel().getLevel().getLevel(), team.getTeamLevel().getCurrentXp()));
            }
        }

        private void handleRefreshCommand(CommandSender sender) {
            if (!Handlers.hasPermission(sender, "requisiteteams.leaderboard.refresh")) {
                sender.sendMessage("§cYou do not have permission to refresh the leaderboard.");
                return;
            }

            Leaderboard.getInstance().forceRefresh();
            sender.sendMessage("§aLeaderboard has been refreshed.");
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, String[] args) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                completions.add("top");
                completions.add("rank");
                completions.add("list");
                if (Handlers.hasPermission(sender, "requisiteteams.leaderboard.refresh")) {
                    completions.add("refresh");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("rank")) {
                completions.addAll(TeamManager.getTeamMap().keySet());
            }
            return completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        }

        @Override
        public boolean hasBasePermission(CommandSender sender) {
            return Handlers.hasPermission(sender, "requisiteteams.use");
        }
    }
}