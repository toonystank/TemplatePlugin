package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.GeyserHandler;
import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.data.DataHolder;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.gui.system.GuiItem;
import com.toonystank.requisiteteams.gui.system.builder.item.ItemBuilder;
import com.toonystank.requisiteteams.gui.system.builder.item.SkullBuilder;
import com.toonystank.requisiteteams.utils.FileConfig;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class GuiData extends FileConfig {

    private final DataHolder dataHolder;
    private GuiRegistry.GuiInfo guiInfo;
    private Component title;
    private String stringTitle;
    private final int row;
    private final List<String> commandArguments = new ArrayList<>();
    private final String bedrockColorReplacement;

    public GuiData(String fileName, String version, String defaultTheme) throws IOException {
        super(fileName, "gui", false, true);
        MessageUtils.toConsole("Loading GUI: " + fileName, false);
        this.dataHolder = new DataHolder(this, defaultTheme);
        this.row = dataHolder.getDataSection().getRows();
        this.bedrockColorReplacement = "§7"; // Default replacement, adjust as needed
        setArguments();
        if (version != null) {
            setConfigVersion(version);
            updateConfig("data.version");
            reload();
        }
        MessageUtils.debug("Successfully loaded " + fileName);
    }

    private void setArguments() {
        if (getConfig().contains("data.args")) {
            commandArguments.addAll(dataHolder.getDataSection().getArgs());
        }
    }

    public void setTitle(RequisitePlayer player, List<String> args) {
        String titleText = dataHolder.getDataSection().getMenuTitle();
        boolean tinyText = false; // Adjust based on config if needed
        this.title = formatText(player, titleText, args, null, tinyText);
        this.stringTitle = formatText(player, titleText, args, false, tinyText);
    }

    public ItemSection getMenuControls(String id) {
        return dataHolder.getItemSections().get(id);
    }

    public List<ItemSection> getMenuControls() {
        return dataHolder.getItemSections().values().stream()
                .filter(ItemSection::isMenuControl)
                .toList();
    }

    public GuiItem createItem(RequisitePlayer player, String id, List<String> args, boolean formatArgs, @Nullable BaseGUI gui) {
        ItemSection itemSection = dataHolder.getItemSections().get(id);
        if (itemSection == null) {
            MessageUtils.warning("Item section is null in " + getFile().getName() + " for item " + id);
            return null;
        }
        return createItem(player, args, itemSection, false, gui, null);
    }

    public GuiItem createItem(RequisitePlayer player, List<String> args, ItemSection itemSection, boolean formatAsIcon, BaseGUI gui, Material materialToUse) {
        Component displayName = formatText(player, itemSection.getDisplayName(), args, gui, materialToUse);
        List<Component> lore = itemSection.getLore() == null ? new ArrayList<>() :
                itemSection.getLore().stream()
                        .map(line -> formatText(player, line, args, gui, materialToUse))
                        .toList();
        String materialString = itemSection.getMaterial();

        MessageUtils.debug("Creating item with material: " + materialString);
        if (materialString.startsWith("basehead-")) {
            return createBaseheadSkull(materialString, displayName, lore, itemSection);
        } else if (materialString.startsWith("playerhead-")) {
            return createPlayerheadSkull(player, materialString, displayName, lore, itemSection);
        }
        String material = determineMaterial(player, materialString);
        return createStandardItem(player, itemSection, displayName, lore, formatAsIcon, materialToUse, material, itemSection.getCustomModelData());
    }

    public GuiItem createFilterItem(List<String> args, RequisitePlayer player, ItemSection section, @Nullable RequisitePlayer admin) {
        // Placeholder for filter item creation, to be implemented by subclasses or extended
        return createItem(player, args, section, false, null, null);
    }

    private boolean isBedrockPlayer(RequisitePlayer player) {
        try {
            return GeyserHandler.isBedrockPlayer(player.getUuid());
        } catch (Exception e) {
            MessageUtils.warning("Could not check Bedrock player status for " + player.getName());
            return false;
        }
    }

    public Component formatText(RequisitePlayer player, String text, List<String> args, @Nullable BaseGUI gui, boolean tinyText) {
        if (text == null) return Component.empty();
        text = formattedArgs(text, args);
        if (isBedrockPlayer(player)) {
            text = text.replace("&7", bedrockColorReplacement);
        }
        // Placeholder for additional text formatting, extend as needed
        return Component.text(text); // Adjust with actual placeholder parsing if available
    }

    public String formatText(RequisitePlayer player, String text, List<String> args, boolean noColor, boolean tinyText) {
        text = formattedArgs(text, args);
        if (isBedrockPlayer(player)) {
            text = text.replace("&7", bedrockColorReplacement);
        }
        // Placeholder for additional text formatting, extend as needed
        return text;
    }

    public Component formatText(RequisitePlayer player, String text, List<String> args, @Nullable BaseGUI gui, Material materialToUse) {
        text = formattedArgs(text, args);
        if (gui != null && gui.gui != null) {
            text = replaceGuiPlaceholders(text, gui);
        }
        if (materialToUse != null) {
            text = replaceMaterialPlaceholders(text, materialToUse);
        }
        if (isBedrockPlayer(player)) {
            text = text.replace("&7", bedrockColorReplacement);
        }
        // Placeholder for additional text formatting, extend as needed
        return Component.text(text);
    }

    private String formattedArgs(String text, List<String> args) {
        try {
            if (args != null && !args.isEmpty()) {
                for (int i = 0; i < args.size() && i < commandArguments.size(); i++) {
                    text = text.replace("{" + commandArguments.get(i) + "}", args.get(i));
                }
            }
        } catch (Exception e) {
            MessageUtils.warning("Error formatting arguments: " + e.getMessage());
        }
        return text;
    }

    private String replaceGuiPlaceholders(String text, BaseGUI gui) {
        if (text.contains("<page>")) {
            text = text.replace("<page>", String.valueOf(gui.gui.getCurrentPageNum()));
        }
        if (text.contains("<max_page>")) {
            int maxPages = gui.gui.getPagesNum();
            text = text.replace("<max_page>", maxPages > 100 || maxPages == Integer.MAX_VALUE ? "Click to calculate" : String.valueOf(maxPages));
        }
        return text;
    }

    private String replaceMaterialPlaceholders(String text, Material material) {
        if (text.contains("<itemname>")) {
            text = text.replace("<itemname>", material.name());
        }
        // Add category or other material-related placeholders if needed
        return text;
    }

    private String determineMaterial(RequisitePlayer player, String material) {
        if (material == null || "AIR".equals(material)) {
            return "AIR";
        }
        // Placeholder for material parsing, extend with actual placeholder system if available
        return material;
    }

    private GuiItem createBaseheadSkull(String material, Component displayName, List<Component> lore, ItemSection itemSection) {
        String texture = material.replace("basehead-", "");
        MessageUtils.debug("Creating basehead skull with texture: " + texture);
        SkullBuilder skullBuilder = ItemBuilder.skull()
                .texture(texture)
                .name(displayName)
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES);

        if (itemSection.isEnchanted()) {
            skullBuilder.glow(true);
        }
        return skullBuilder.asGuiItem();
    }

    private GuiItem createPlayerheadSkull(RequisitePlayer player, String material, Component displayName, List<Component> lore, ItemSection itemSection) {
        String playerName = material.replace("playerhead-", "");
        // Placeholder for player lookup, adjust with actual player data management
        SkullBuilder skullBuilder = ItemBuilder.skull()
                .name(displayName)
                .lore(lore)
                .flags(ItemFlag.HIDE_ATTRIBUTES);

        if (itemSection.isEnchanted()) {
            skullBuilder.glow(true);
        }
        return skullBuilder.asGuiItem();
    }

    private GuiItem createStandardItem(RequisitePlayer player, ItemSection itemSection, Component displayName, List<Component> lore, boolean formatAsIcon, Material materialToUse, String material, int modelData) {
        MessageUtils.debug("Creating standard item with material: " + material);
        material = material.toUpperCase(Locale.ROOT);
        Material resolvedMaterial = materialToUse != null ? materialToUse : Material.matchMaterial(material);
        if (resolvedMaterial == null) {
            resolvedMaterial = Material.STONE;
        }

        ItemBuilder itemBuilder = ItemBuilder.from(resolvedMaterial)
                .name(displayName)
                .lore(lore)
                .amount(itemSection.getAmount())
                .flags(ItemFlag.HIDE_ATTRIBUTES)
                .model(modelData);

        if (itemSection.isEnchanted()) {
            itemBuilder.glow(true);
        }
        return itemBuilder.asGuiItem();
    }

    public void execute(RequisitePlayer operator, RequisitePlayer target, String sectionName, List<String> args, BaseGUI baseGUI, boolean left, @Nullable RequisitePlayer admin) {
        ItemSection section = dataHolder.getItemSections().get(sectionName);
        List<String> baseCommands = left ? section.getLeftClickCommands() : section.getRightClickCommands();
        runCommand(operator, target, args, baseGUI, admin, baseCommands, section);
    }

    private void runCommand(RequisitePlayer operator, RequisitePlayer target, List<String> args, BaseGUI baseGUI, @Nullable RequisitePlayer admin, List<String> baseCommands, ItemSection itemSection) {
        if (admin != null) {
            operator = admin;
        }
        for (String command : baseCommands) {
            int delay = extractDelay(command);
            command = command.replaceAll("<delay=\\d+>", "");
            String finalCommand = formatText(target, command, args, false,false);
            executeCommand(operator, finalCommand, delay, baseGUI);
        }
    }

    private int extractDelay(String command) {
        if (command.contains("<delay=")) {
            String delayString = command.substring(command.indexOf("<delay=") + 7, command.indexOf(">"));
            try {
                return Integer.parseInt(delayString);
            } catch (NumberFormatException e) {
                MessageUtils.warning("Invalid delay format: " + delayString);
            }
        }
        return 0;
    }

    private void executeCommand(RequisitePlayer operator, String command, int delay,BaseGUI gui) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    if (command.startsWith("[sound]")) {
                        String sound = command.replace("[sound] ", "");
                        operator.getOnlinePlayer().playSound(operator.getOnlinePlayer().getLocation(), sound, 1, 1);
                    } else if (command.startsWith("[close]")) {
                        gui.getGui().close(operator);
                    } /*else if (command.startsWith("[reopen]")) {
                        gui.reopen(currentSession.args(), currentSession.admin());
                    } else if (command.startsWith("[message]")) {
                        String message = command.replace("[message] ", "");
                        MessageUtils.sendMessage(operator.getOnlinePlayer(), formatText(operator, message, currentSession.args(), baseGUI, null));
                    } else if (command.startsWith("[player]")) {
                        CommandDispatch.dispatchCommand(operator, command.replace("[player] ", ""), CommandDispatch.DispatchType.PLAYER);
                    } else if (command.startsWith("[console]")) {
                        CommandDispatch.dispatchCommand(operator, command.replace("[console] ", ""), CommandDispatch.DispatchType.CONSOLE);
                    }*/
                } catch (Exception e) {
                    MessageUtils.error("Error executing command: " + command + " - " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskLater(RequisiteTeams.getInstance(), delay); // Plugin instance needed here
    }
}