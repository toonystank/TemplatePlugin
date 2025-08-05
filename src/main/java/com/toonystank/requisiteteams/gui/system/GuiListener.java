package com.toonystank.requisiteteams.gui.system;


import com.toonystank.requisiteteams.data.PlayerDataManager;
import com.toonystank.requisiteteams.gui.system.components.ActionRecord;
import com.toonystank.requisiteteams.gui.system.components.GuiAction;
import com.toonystank.requisiteteams.gui.system.components.GuiClickAction;
import com.toonystank.requisiteteams.gui.system.components.util.ItemNbt;
import com.toonystank.requisiteteams.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class GuiListener implements Listener {

    /**
     * Handles what happens when a player clicks on the GUI
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onGuiClick(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUISystem gui)) return;

        // Executes the outside click action
        final GuiAction<InventoryClickEvent> outsideClickAction = gui.getOutsideClickAction();
        if (outsideClickAction != null && event.getClickedInventory() == null) {
            outsideClickAction.execute(event);
            return;
        }

        if (event.getClickedInventory() == null) return;

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryClickEvent> defaultTopClick = gui.getDefaultTopClickAction();
        if (defaultTopClick != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            defaultTopClick.execute(event);
        }

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryClickEvent> playerInventoryClick = gui.getPlayerInventoryAction();
        if (playerInventoryClick != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
            playerInventoryClick.execute(event);
        }

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryClickEvent> defaultClick = gui.getDefaultClickAction();
        if (defaultClick != null) defaultClick.execute(event);

        // Slot action and checks weather or not there is a slot action and executes it
        final GuiAction<InventoryClickEvent> slotAction = gui.getSlotAction(event.getSlot());
        if (slotAction != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            slotAction.execute(event);
        }

        GuiItem guiItem;
        GuiClickAction itemAction;
        // Checks whether it's a paginated gui or not
        if (gui instanceof PaginatedGuiSystem paginatedGui) {

            // Gets the gui item from the added items or the page items
            guiItem = paginatedGui.getGuiItem(event.getSlot());
            if (guiItem == null) guiItem = paginatedGui.getPageItem(event.getSlot());

        } else {
            // The clicked GUI Item
            guiItem = gui.getGuiItem(event.getSlot());
        }

        if (!isGuiItem(event.getCurrentItem(), guiItem)) return;
        itemAction = guiItem.getAction();
        MessageUtils.debug("GuiListener: onGuiClick: " + event.getCurrentItem().getType() + " in slot " + event.getSlot() + " clicked by " + event.getWhoClicked().getName());
        // Executes the action of the item
        MessageUtils.debug("GuiListener: onGuiClick: itemAction = " + itemAction);
        ActionRecord actionRecord = new ActionRecord(event.isLeftClick(),event.getCurrentItem(), PlayerDataManager.getPlayer(event.getWhoClicked().getUniqueId()));
        if (itemAction != null) itemAction.execute(actionRecord);
    }

    /**
     * Handles what happens when a player clicks on the GUI
     *
     * @param event The InventoryClickEvent
     */
    @EventHandler
    public void onGuiDrag(final InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUISystem gui)) return;

        // Default click action and checks weather or not there is a default action and executes it
        final GuiAction<InventoryDragEvent> dragAction = gui.getDragAction();
        if (dragAction != null) dragAction.execute(event);
    }

    /**
     * Handles what happens when the GUI is closed
     *
     * @param event The InventoryCloseEvent
     */
    @EventHandler
    public void onGuiClose(final InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUISystem gui)) return;


        // The GUI action for closing
        final GuiAction<InventoryCloseEvent> closeAction = gui.getCloseGuiAction();

        // Checks if there is or not an action set and executes it
        if (closeAction != null && !gui.isUpdating() && gui.shouldRunCloseAction()) closeAction.execute(event);
        if (gui instanceof PaginatedGuiSystem paginatedGui) {
            // Clear the item actions map when the GUI is closed
            paginatedGui.getBaseGUI().getItemActions().clear();
        }
    }

    /**
     * Handles what happens when the GUI is opened
     *
     * @param event The InventoryOpenEvent
     */
    @EventHandler
    public void onGuiOpen(final InventoryOpenEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUISystem gui)) return;

        // The GUI action for opening
        final GuiAction<InventoryOpenEvent> openAction = gui.getOpenGuiAction();

        // Checks if there is or not an action set and executes it
        if (openAction != null && !gui.isUpdating()) openAction.execute(event);
    }

    /**
     * Checks if the item is or not a GUI item
     *
     * @param currentItem The current item clicked
     * @param guiItem     The GUI item in the slot
     * @return Whether it is or not a GUI item
     */
    private boolean isGuiItem(@Nullable final ItemStack currentItem, @Nullable final GuiItem guiItem) {
        if (currentItem == null || guiItem == null) return false;
        // Checks whether the Item is truly a GUI Item
        final String nbt = ItemNbt.getString(currentItem, "mf-gui");
        if (nbt == null) return false;
        return nbt.equals(guiItem.getUuid().toString());
    }

}
