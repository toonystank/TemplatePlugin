package com.toonystank.requisitertp;

import com.toonystank.requisitertp.utils.MainConfig;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

public final class RequisiteRTP extends JavaPlugin {

    @Getter
    private static RequisiteRTP instance;
    @Getter
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        instance = this;
        adventure = BukkitAudiences.create(this);
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public MainConfig getMainConfig() {
        return null;
    }
}
