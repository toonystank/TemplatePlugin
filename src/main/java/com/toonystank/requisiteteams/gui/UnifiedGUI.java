package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.gui.system.GuiItem;
import net.kyori.adventure.text.Component;

import java.util.List;

public interface UnifiedGUI {
        void setTitle(Component title);

        void updateTitle(String title);
        void updateTitle(Component title);

        void addItem(GuiItem item, ItemSection itemSection);

        void setItem(List<Integer> slot, GuiItem item, ItemSection itemSection);

        void updateItem(List<Integer> slot, GuiItem item, ItemSection itemSection);

        void clearPageItems();

        void open(RequisitePlayer player);
        void close(RequisitePlayer player);

        void update();

        void disableAllInteractions();

        void setCloseGuiAction(Runnable action);

        boolean isViewer(RequisitePlayer player);

        int getPagesNum();

        int getCurrentPageNum();
    }