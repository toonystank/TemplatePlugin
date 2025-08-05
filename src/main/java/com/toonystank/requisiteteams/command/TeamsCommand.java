package com.toonystank.requisiteteams.command;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.command.manager.BaseCommand;
import com.toonystank.requisiteteams.command.manager.SubCommand;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.level.Level;
import com.toonystank.requisiteteams.level.LevelData;
import com.toonystank.requisiteteams.team.Team;
import com.toonystank.requisiteteams.team.TeamLevel;
import com.toonystank.requisiteteams.team.TeamManager;
import com.toonystank.requisiteteams.team.rank.Rank;
import com.toonystank.requisiteteams.team.rank.RankPermissions;
import com.toonystank.requisiteteams.team.rank.RankRegistry;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TeamsCommand extends BaseCommand {

    public TeamsCommand(RequisiteTeams plugin) {
        super(plugin,
                "RequisiteTeams",
                false,
                true,
                "Command for RequisiteTeams",
                "/teams <subcommand>",
                "requisiteteams.use",
                List.of("rt", "team", "teams"));
        registerSubCommands();
    }

    private void registerSubCommands() {
        this.registerSubCommand("reload", new ReloadCommand());
        this.registerSubCommand("addxp", new TeamSubCommands.AddXpCommand());
        this.registerSubCommand("setxp", new TeamSubCommands.SetXpCommand());
        this.registerSubCommand("getxp", new TeamSubCommands.GetXpCommand());
        this.registerSubCommand("create", new TeamSubCommands.CreateCommand());
        this.registerSubCommand("delete", new TeamSubCommands.DeleteCommand());
        this.registerSubCommand("add", new TeamSubCommands.AddCommand());
        this.registerSubCommand("kick", new TeamSubCommands.KickCommand());
        this.registerSubCommand("promote", new TeamSubCommands.PromoteCommand());
        this.registerSubCommand("setrank", new TeamSubCommands.SetRankCommand());
        this.registerSubCommand("info", new TeamSubCommands.InfoCommand());
        this.registerSubCommand("list", new TeamSubCommands.ListCommand());
        this.registerSubCommand("leaderboard", new TeamSubCommands.LeaderboardCommand());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public void execute(ConsoleCommandSender sender, String[] args) {
        sender.sendMessage("Use /teams <subcommand> for team management.");
    }

    @Override
    public void execute(Player player, String[] args) {
        player.sendMessage("Use /teams <subcommand> for team management.");
    }
}