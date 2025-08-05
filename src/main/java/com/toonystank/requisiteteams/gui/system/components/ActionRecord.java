package com.toonystank.requisiteteams.gui.system.components;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.system.GuiItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record ActionRecord(Boolean isLeftClick, ItemStack currentItem, @Nullable RequisitePlayer clickedPlayer) {
    public static ActionRecord BedrockAction(GuiItem item) {
        return new ActionRecord(true,item.getItemStack(),null);
    }
}
