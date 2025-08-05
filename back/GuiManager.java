package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MainConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class GuiManager implements Listener {

    private final RequisiteTeams plugin;
    private final MainConfig mainConfig;
    public final Map<UUID, GuiData> guiInfoGuiDataMap =  new ConcurrentHashMap<>();
    public final PlayerDataManager playerDataManager;
    public String defaultTheme;

    public GuiManager(RequisiteTeams plugin, MainConfig mainConfig, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.mainConfig = mainConfig;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.playerDataManager = playerDataManager;
        initGuiList();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        RequisitePlayer player = PlayerDataManager.getPlayer(event.getPlayer().getUniqueId());
        if (PlayerDataManager.addPlayer(player, false)) {
            MessageUtils.toConsole("&aCreated Player Data for " + player.getName() + ".",false);
        }
        initiatePlayerGUI(player);
        MessageUtils.toConsole( "&aLoaded Player Data for " + player.getName() + ".",false);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        RequisitePlayer regionsPlayer = PlayerDataManager.getPlayer(event.getPlayer().getUniqueId());
        if (regionsPlayer == null) return;
        deInitializePlayerGui(regionsPlayer);
    }

    public void initiatePlayerGUI(RequisitePlayer player) {
        MessageUtils.debug("initializing player gui for " + player);
        guiInfoGuiDataMap.values().forEach(gui -> {
            BaseGUI baseGUI = new BaseGUI(gui,player);
            MessageUtils.debug("running gui " + baseGUI + " guiinfo " + gui.guiInfo);

            player.addGui(gui.guiInfo, baseGUI);
        });
    }
    public void deInitializePlayerGui(RequisitePlayer regionsPlayer) {
        regionsPlayer.guiMap.clear();
    }

    private void initGuiList() {
        String themeString = mainConfig.getString("gui.theme");
        if (themeString == null) {
            MessageUtils.warning("No gui theme found in config.yml");
            return;
        }
        if (!(themeString.equals("nature") || themeString.equals("default") || themeString.equals("clean"))) {
            MessageUtils.warning("Invalid gui theme: " + themeString);
            return;
        }
        MessageUtils.toConsole("using theme " + themeString ,true);
        this.defaultTheme = themeString;
        try {
            for (String customGUI : mainConfig.customGUIs) {
                GuiRegistry.registerGUI(customGUI,customGUI,false);
            }
            for (GuiRegistry.GuiInfo guiInfo : GuiRegistry.guis.values()) {
                GuiData guiData = getGui(guiInfo);
                MessageUtils.debug("loaded GUI " + guiInfo.guiName());
                guiInfoGuiDataMap.put(guiInfo.uuid(),guiData);
            }
            MessageUtils.toConsole("Loaded gui theme: " + themeString + " successfully",false);
        } catch (IOException e) {
            MessageUtils.warning("Error while loading gui theme: " + themeString);
        }

        List<String> extra = mainConfig.getStringList("gui.extra");
        if (extra == null || extra.isEmpty()) return;
        extra.forEach(extraGui -> {
            try {
                GuiData menu = new GuiData(extraGui, "1.0",themeString);
                guiInfoGuiDataMap.put(menu.,menu);
                MessageUtils.toConsole("Loaded extra gui: " + extraGui,false);
            } catch (IOException e) {
                MessageUtils.warning("Error while loading extra gui: " + extraGui);
            }
        });
    }

    @NotNull
    private GuiData getGui(GuiRegistry.GuiInfo gui) throws IOException {
        String fileName = gui.fileName();
        MessageUtils.debug("Loading GUI " + fileName);
        String version =
                switch (fileName) {
                    case "ClaimList" -> "1.14";
                    case "PlayerList" -> "1.12";
                    case "WarpList" -> "1.10";
                    case "BlackList" -> "1.2";
                    case "Whitelist" -> "1.1";
                    case "ClaimMenu" -> "1.4";
                    case "ClaimBlock" -> "1.4";
                    case "ClaimDelete" -> "1.1";
                    case "ClaimDeleteAll" -> "1.1";
                    case "ClaimKick" -> "1.2";
                    case "ClaimLeave" -> "1.1";
                    case "ClaimLeaveAll" -> "1.1";
                    case "ClaimInfo" -> "1.2";
                    case "ClaimPermissions" -> "1.3";
                    case "ClaimUpgrade" -> "1.6";
                    case "TrustList" -> "1.2";
                    case "ClaimUpgradeNoEnter" -> "1.1";
                    default -> "1.0";
                };
        FileConfig manager = new FileConfig(fileName + ".yml","gui");
        if (manager.isFileExist() && manager.getConfig() != null) {
            String existCheck = manager.getString("data.menu_title");
            if (existCheck != null)
                return new GuiData(plugin, fileName + ".yml", version,defaultTheme);
        }
        return new GuiData(plugin, fileName + "_" + mainConfig.languageTag + ".yml", version, regionsManager,defaultTheme);
    }

    public void reloadGUI(CommandSender operator) {
        guiInfoGuiDataMap.clear();
        initGuiList();
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            RequisitePlayer player = PlayerDataManager.getPlayer(onlinePlayer.getUniqueId());
            if (player == null) continue;
            player.guiMap.clear();
            initiatePlayerGUI(player);
        }
    }
}
 