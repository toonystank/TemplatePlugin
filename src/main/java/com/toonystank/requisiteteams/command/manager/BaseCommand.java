package com.toonystank.requisiteteams.command.manager;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.utils.Handlers;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@SuppressWarnings("unused")
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    private final RequisiteTeams plugin;
    private final Command commandData;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    protected BaseCommand(RequisiteTeams plugin, String name, boolean playerOnlyCommand, boolean requireArgument, String description, String usage, String permission, List<String> aliases) {
        this.plugin = plugin;
        this.commandData = new Command(name.toLowerCase(), playerOnlyCommand, requireArgument, description, usage, permission, aliases);
        registerCommand(commandData);
    }

    protected BaseCommand(RequisiteTeams plugin, String name, boolean playerOnlyCommand, boolean requireArgument, String description, String usage, String permission, String... aliases) {
        this(plugin, name, playerOnlyCommand, requireArgument, description, usage, permission, Arrays.asList(aliases));
    }

    protected BaseCommand(RequisiteTeams plugin, Command commandData) {
        this.plugin = plugin;
        this.commandData = commandData;
        registerCommand(commandData);
    }

    public void registerSubCommand(String name, SubCommand subCommand) {
        subCommands.put(name.toLowerCase(), subCommand);
    }

    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    public abstract void execute(ConsoleCommandSender sender, String[] args);

    public abstract void execute(Player player, String[] args);

    private void registerCommand(Command commandData) {
        PluginCommand command = plugin.getCommand(commandData.name());
        if (command == null) {
            command = createPluginCommand(commandData);
        }
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
            // Register aliases
            List<String> lowerCaseAliases = commandData.aliases().stream()
                    .map(String::toLowerCase)
                    .filter(alias -> !alias.equalsIgnoreCase(commandData.name()))
                    .distinct()
                    .collect(Collectors.toList());
            command.setAliases(lowerCaseAliases);
            if (commandData.description() != null && !commandData.description().isEmpty()) {
                command.setDescription(commandData.description());
            }
            if (commandData.usage() != null && !commandData.usage().isEmpty()) {
                command.setUsage(commandData.usage());
            }
            if (commandData.permission() != null && !commandData.permission().isEmpty()) {
                command.setPermission(commandData.permission());
            }
            // Register aliases in CommandMap
            try {
                Field commandMapField = plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
                commandMapField.setAccessible(true);
                CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer().getPluginManager());
                for (String alias : lowerCaseAliases) {
                    commandMap.register(alias, plugin.getName().toLowerCase(), command);
                }
            } catch (Exception e) {
                MessageUtils.warning("Failed to register aliases for command: " + commandData.name() + " - " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            MessageUtils.warning("Failed to register command: " + commandData.name());
        }
    }

    private PluginCommand createPluginCommand(Command commandData) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance(commandData.name(), plugin);
            command.setExecutor(this);
            command.setTabCompleter(this);
            Field commandMapField = plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer().getPluginManager());
            if (commandMap.getCommand(commandData.name()) != null) {
                MessageUtils.warning("Command " + commandData.name() + " is already registered. Skipping registration.");
                return null;
            }
            commandMap.register(plugin.getName().toLowerCase(), command);
            return command;
        } catch (Exception e) {
            MessageUtils.warning("Error registering command " + commandData.name() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (commandData.playerOnlyCommand() && !(sender instanceof Player)) {
            sender.sendMessage(plugin.getMainConfig().getLanguageConfig().getPlayerOnly());
            return true;
        }
        if (commandData.permission() != null && !Handlers.hasPermission(sender, commandData.permission())) {
            sender.sendMessage(plugin.getMainConfig().getLanguageConfig().getNoPermission());
            return true;
        }

        // Handle subcommands
        if (args.length > 0) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand == null) {
                sender.sendMessage(plugin.getMainConfig().getLanguageConfig().getNoPermission());
                return true;
            }
            if (!subCommand.hasBasePermission(sender)) {
                sender.sendMessage(plugin.getMainConfig().getLanguageConfig().getNoPermission());
                return true;
            }
            subCommand.execute(sender, Arrays.copyOfRange(args, 1, args.length));
            return true;
        }

        // Default behavior
        if (commandData.requireArguments()) {
            sender.sendMessage(commandData.usage());
            return true;
        }

        if (sender instanceof Player) {
            execute((Player) sender, args);
        } else if (sender instanceof ConsoleCommandSender) {
            execute((ConsoleCommandSender) sender, args);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(key -> key.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.onTabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return Collections.emptyList();
    }

    public record Command(String name, boolean playerOnlyCommand, boolean requireArguments, String description,
                          String usage, String permission, List<String> aliases) {
            public Command(String name, boolean playerOnlyCommand, boolean requireArguments, String description, String usage, String permission, List<String> aliases) {
                this.name = name.toLowerCase();
                this.playerOnlyCommand = playerOnlyCommand;
                this.requireArguments = requireArguments;
                this.description = description;
                this.usage = usage;
                this.permission = permission;
                this.aliases = aliases != null ? aliases.stream().map(String::toLowerCase).collect(Collectors.toList()) : Collections.emptyList();
            }
        }
}