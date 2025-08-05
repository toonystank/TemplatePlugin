package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.gui.system.GuiItem;
import com.toonystank.requisiteteams.gui.system.SimpleBedrockGUI;
import com.toonystank.requisiteteams.gui.system.components.ActionRecord;
import net.kyori.adventure.text.Component;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public record SimpleBedrockGuiAdapter(SimpleBedrockGUI gui) implements UnifiedGUI {
    @Override
    public void setTitle(Component title) {
        gui.setTitle(title.toString());
    }

    @Override
    public void updateTitle(String title) {
        gui.setTitle(title);
    }

    @Override
    public void updateTitle(Component title) {

    }

    @Override
    public void addItem(GuiItem item, ItemSection itemSection) {
        if (!itemSection.isBedrockSupported()) return;
        String displayName = item.getItemStack().getItemMeta().getDisplayName();
        ActionRecord actionRecord = ActionRecord.BedrockAction(item);
        gui.addButton(displayName, new BukkitRunnable() {
            @Override
            public void run() {
                item.getAction().execute(actionRecord);
            }
        });
    }

    @Override
    public void setItem(List<Integer> slot, GuiItem item, ItemSection itemSection) {
        if (!itemSection.isBedrockSupported()) return;
        String displayName = item.getItemStack().getItemMeta().getDisplayName();
        ActionRecord actionRecord = ActionRecord.BedrockAction(item);
        gui.addButton(displayName, new BukkitRunnable() {
            @Override
            public void run() {
                item.getAction().execute(actionRecord);
            }
        });
    }

    @Override
    public void updateItem(List<Integer> slot, GuiItem item, ItemSection itemSection) {
        if (!itemSection.isBedrockSupported()) return;
        String displayName = item.getItemStack().getItemMeta().getDisplayName();
        ActionRecord actionRecord = ActionRecord.BedrockAction(item);
        gui.updateButton(slot.get(0), displayName, new BukkitRunnable() {
            @Override
            public void run() {
                item.getAction().execute(actionRecord);
            }
        });
    }

    @Override
    public void clearPageItems() {
        gui.clear();
    }

    @Override
    public void close(RequisitePlayer player) {

    }

    @Override
    public void open(RequisitePlayer player) {
        gui.show(player);
    }

    @Override
    public void update() {
        gui.refresh();
    }

    @Override
    public void disableAllInteractions() {
        // May not be applicable for Bedrock forms
    }

    @Override
    public void setCloseGuiAction(Runnable action) {
        gui.setCloseAction(action); // Adjust based on SimpleBedrockGUI API
    }

    @Override
    public boolean isViewer(RequisitePlayer player) {
        return gui.isOpenFor(player); // Adjust based on SimpleBedrockGUI API
    }

    @Override
    public int getPagesNum() {
        return gui.getPagesNum();
    }

    @Override
    public int getCurrentPageNum() {
        return gui.getCurrentPageNum();
    }
}