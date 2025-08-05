package com.toonystank.requisiteteams.gui.system.components.util;

import com.google.common.primitives.Ints;
import com.toonystank.requisiteteams.gui.system.components.exception.GuiException;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for detecting server version for legacy support :(
 */
public final class VersionHelper {

    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    // PDC and customModelData
    private static final int V1_14 = 1140;
    // PlayerProfile API
    private static final int V1_20_1 = 1201;

    private static final int CURRENT_VERSION = getCurrentVersion();
    /**
     * Checks if the version has {@link ItemMeta#setCustomModelData(Integer)}
     */
    public static final boolean IS_CUSTOM_MODEL_DATA = CURRENT_VERSION >= V1_14;
    /**
     * Checks if the version has {@link org.bukkit.profile.PlayerProfile}
     */
    public static final boolean IS_PLAYER_PROFILE_API = CURRENT_VERSION >= V1_20_1;
    public static final boolean IS_FOLIA = checkFolia();

    /**
     * Check if the server has access to the Paper API
     * Taken from <a href="https://github.com/PaperMC/PaperLib">PaperLib</a>
     *
     * @return True if on Paper server (or forks), false anything else
     */
    private static boolean checkPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Check if the server has access to the Folia API
     * Taken from <a href="https://github.com/PaperMC/Folia">Folia</a>
     *
     * @return True if on Folia server (or forks), false anything else
     */
    private static boolean checkFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Gets the current server version
     *
     * @return A protocol like number representing the version, for example, 1.16.5 -> 1165
     */
    private static int getCurrentVersion() {
        // No need to cache since will only run once
        final Matcher matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion());

        final StringBuilder stringBuilder = new StringBuilder();
        if (matcher.find()) {
            stringBuilder.append(matcher.group("version").replace(".", ""));
            final String patch = matcher.group("patch");
            if (patch == null) stringBuilder.append("0");
            else stringBuilder.append(patch.replace(".", ""));
        }

        //noinspection UnstableApiUsage
        final Integer version = Ints.tryParse(stringBuilder.toString());

        // Should never fail
        if (version == null) throw new GuiException("Could not retrieve server version!");

        return version;
    }

    public static Class<?> craftClass(@NotNull final String name) throws ClassNotFoundException {
        return Class.forName(CRAFTBUKKIT_PACKAGE + "." + name);
    }

}
