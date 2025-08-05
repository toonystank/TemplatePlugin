package com.toonystank.requisiteteams.placeholders;

import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.utils.MessageUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderRequisiteTeam extends PlaceholderExpansion  {

    public PlaceholderRequisiteTeam() {
    }

    @Override
    public @NotNull String getIdentifier() {
        return "requisiteteams";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Edward";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offline, @NotNull String params) {
        RequisitePlayer player = PlayerDataManager.getPlayer(offline.getUniqueId());
        if (player == null) return null;

        String placeholder = PlaceholderAPI.setBracketPlaceholders(player.getPlayer(), params);
        if (placeholder.contains("{player")) {
            MessageUtils.toConsole("Player placeholder is not installed! Please install it by using /papi ecloud download Player", false);
        }

        String[] args = placeholder.split("_");
        String prefix = args[0].toLowerCase();
        String suffix = args.length > 1 ? args[1] : "";
        MessageUtils.debug("PlaceholderRequisiteTeam " + "onRequest " + "prefix: " + prefix + ", suffix: " + suffix);
        PlaceholderRequest.PlaceholderHandler handler = PlaceholderRequest.getHandlers().get(prefix);
        MessageUtils.debug("PlaceholderRequisiteTeam " + "onRequest " + "handler: " + handler);
        if (handler == null) return null;

        return handler.handle(player, suffix, args, true);
    }
}
