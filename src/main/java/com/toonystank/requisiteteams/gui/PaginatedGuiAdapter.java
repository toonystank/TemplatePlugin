package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.gui.system.GuiItem;
import com.toonystank.requisiteteams.gui.system.PaginatedGuiSystem;
import com.toonystank.requisiteteams.utils.MessageUtils;
import net.kyori.adventure.text.Component;

import java.util.List;

public record PaginatedGuiAdapter(PaginatedGuiSystem gui) implements UnifiedGUI {

    @Override
    public void setTitle(Component title) {
        gui.title();
    }

    @Override
    public void updateTitle(String title) {

    }

    @Override
    public void updateTitle(Component title) {
        MessageUtils.debug(Component.text("Updating GUI title to: ").append(title));
        gui.updateTitle(title);
    }

    @Override
    public void addItem(GuiItem item, ItemSection itemSection) {
        gui.addItem(item);
    }

    @Override
    public void setItem(List<Integer> slots, GuiItem item, ItemSection itemSection) {
        gui.setItem(slots, item);
    }

    @Override
    public void updateItem(List<Integer> slots, GuiItem item, ItemSection itemSection) {
        gui.updateItem(slots, item);
    }

    @Override
    public void clearPageItems() {
        gui.clearPageItems();
    }

    @Override
    public void close(RequisitePlayer player) {
        gui.close(player.getOnlinePlayer());
    }

    @Override
    public void open(RequisitePlayer player) {
        gui.open(player.getOnlinePlayer());
    }

    @Override
    public void update() {
        gui.update();
    }

    @Override
    public void disableAllInteractions() {
        gui.disableAllInteractions();
    }

    @Override
    public void setCloseGuiAction(Runnable action) {
        gui.setCloseGuiAction(event -> {
            action.run();
        });
    }

    @Override
    public boolean isViewer(RequisitePlayer player) {
        return gui.getInventory().getViewers().contains(player.getOnlinePlayer());
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