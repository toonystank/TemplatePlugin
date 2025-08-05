package com.toonystank.requisiteteams.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.toonystank.requisiteteams.RequisiteTeams;

@SuppressWarnings("unused")
public class Handlers {

    public static boolean hasPermission(CommandSender sender, String highLevelPermission) {
        return sender.hasPermission(RequisiteTeams.getInstance().getPluginName() + "." + highLevelPermission);
    }
    public static  class Legacy {
        public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();

        private Legacy() {
            throw new UnsupportedOperationException("Class should not be instantiated!");
        }
    }

    public static void runTask(Runnable runnable) {
        Bukkit.getScheduler().runTask(RequisiteTeams.getInstance(),runnable);
    }
    public static void runTaskTimer(int time,Runnable runnable) {
        Bukkit.getScheduler().runTaskTimer(RequisiteTeams.getInstance(), runnable, 0, time);
    }
    public static void runTaskTimerAsync(int time,Runnable runnable) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(RequisiteTeams.getInstance(), runnable, 0, time);
    }
    public static void runTaskLater(int time,Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(RequisiteTeams.getInstance(), runnable, time);
    } 

}
