package com.toonystank.requisite.Modules.managers;

import com.toonystank.requisite.Requisite;
import lombok.Getter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

@Getter
@SuppressWarnings("unused")
public abstract class BaseCommand implements CommandExecutor,TabCompleter{

    private final Requisite plugin;
    private final Command commandData;
    /**
     * Constructor to create a new command with specific properties.
     *
     * @param plugin            The plugin instance.
     * @param name              The name of the command.
     * @param playerOnlyCommand Whether the command can only be executed by a player.
     * @param requireArgument   Whether the command requires arguments.
     * @param description       The description of the command.
     * @param usage             The usage message for the command.
     * @param permission        The permission required to use the command.
     * @param aliases           A list of command aliases.
     */
    protected BaseCommand(Requisite plugin, String name, boolean playerOnlyCommand, boolean requireArgument, String description, String usage, String permission, List<String> aliases) {
        this.plugin = plugin;
        this.commandData = new Command(name,playerOnlyCommand,requireArgument, description, usage, permission, aliases);
        registerCommand(commandData);
    }

    /**
     * Constructor to create a command using an existing command data object.
     *
     * @param plugin      The plugin instance.
     * @param commandData The command data containing properties of the command.
     */
    protected BaseCommand(Requisite plugin, Command commandData) {
        this.plugin = plugin;
        this.commandData = commandData;
        registerCommand(commandData);
    }

    /**
     * Handles tab completion logic for the command. Must be implemented by subclasses.
     *
     * @param sender The command sender (player or console).
     * @param args   The arguments provided with the command.
     * @return A list of possible completions for the current argument.
     */
    public abstract List<String> onTabComplete(CommandSender sender, String[] args);

    /**
     * Executes the command logic. Must be implemented by subclasses.
     *
     * @param sender The command sender console.
     * @param args   The arguments provided with the command.
     */
    public abstract void execute(ConsoleCommandSender sender, String[] args);
    /**
     * Executes the command logic for a player. Must be implemented by subclasses.
     * @param player The player executing the command.
     * @param args  The arguments provided with the command.
     */
    public abstract void execute(Player player, String[] args);

    private void registerCommand(Command commandData) {
        PluginCommand command = plugin.getCommand(commandData.name());
        if (command == null) {
            command = createPluginCommand(commandData);
        }
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
            if (commandData.aliases() != null && commandData.aliases().isEmpty()) {
                command.setAliases(commandData.aliases());
            }
            if (commandData.description() != null && !commandData.description().isEmpty()) {
                command.setDescription(commandData.description());
            }
            if (commandData.usage() != null && !commandData.usage().isEmpty()) {
                command.setUsage(commandData.usage());
            }
            command.setPermission(commandData.permission());
        } else {
            plugin.getLogger().warning("Failed to register command: " + commandData.name());
        }
    }
    private PluginCommand createPluginCommand(Command commandData) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class
                    .getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            PluginCommand command = constructor.newInstance(commandData.name(), plugin);
            command.setExecutor(this);
            command.setTabCompleter(this);
            if (commandData.description() != null) {
                command.setDescription(commandData.description());
            }
            if (commandData.usage() != null) {
                command.setUsage(commandData.usage());
            }
            if (commandData.permission() != null) {
                command.setPermission(commandData.permission());
            }
            if (commandData.aliases() != null) {
                command.setAliases(commandData.aliases());
            }
            Field commandMapField = plugin.getServer().getPluginManager().getClass()
                    .getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(plugin.getServer().getPluginManager());
            commandMap.register(plugin.getName(), command);
            return command;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (commandData.playerOnlyCommand() && !(sender instanceof Player)) {
            sender.sendMessage(plugin.getMainConfig().getLanguageConfig().getPlayerOnly());
            return true;
        }
        if (commandData.permission() != null && !sender.hasPermission(commandData.permission())) {
            sender.sendMessage(plugin.getMainConfig().getLanguageConfig().getNoPermission());
            return true;
        }
        if (commandData.requireArguments() && args.length == 0 && commandData.usage() != null) {
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
    public List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        return onTabComplete(sender, args);
    }

    public record Command(String name, boolean playerOnlyCommand, boolean requireArguments, @Nullable String description, @Nullable String usage, @Nullable String permission, @Nullable List<String> aliases) {
    }

}