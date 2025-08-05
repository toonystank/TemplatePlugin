package com.toonystank.requisiteteams.gui.system;

import com.google.common.base.Preconditions;
import com.toonystank.requisiteteams.gui.system.components.GuiAction;
import com.toonystank.requisiteteams.gui.system.components.GuiClickAction;
import com.toonystank.requisiteteams.gui.system.components.util.ItemNbt;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * GuiItem represents the {@link ItemStack} on the {@link Inventory}
 */
@SuppressWarnings("unused")
public class GuiItem {

    // Random UUID to identify the item when clicking
    private final UUID uuid = UUID.randomUUID();
    // Action to do when clicking on the item
    private GuiClickAction action;
    // The ItemStack of the GuiItem
    private ItemStack itemStack;

    @Getter
    private boolean isAir = false;

    /**
     * Main constructor of the GuiItem
     *
     * @param itemStack The {@link ItemStack} to be used
     * @param action    The {@link GuiAction} to run when clicking on the Item
     */
    public GuiItem(@NotNull ItemStack itemStack, @Nullable final GuiClickAction action) {
        Preconditions.checkNotNull(itemStack, "The ItemStack for the GUI Item cannot be null!");

        this.action = action;
        if (itemStack.getType() == Material.AIR) {
            isAir = true;
            itemStack = new ItemStack(Material.STONE);
        }
        // Sets the UUID to an NBT tag to be identifiable later
        setItemStack(itemStack);
    }

    /**
     * Secondary constructor with no action
     *
     * @param itemStack The ItemStack to be used
     */
    public GuiItem(@NotNull final ItemStack itemStack) {
        this(itemStack, null);
    }

    /**
     * Alternate constructor that takes {@link Material} instead of an {@link ItemStack} but without a {@link GuiAction}
     *
     * @param material The {@link Material} to be used when invoking class
     */
    public GuiItem(@NotNull final Material material) {
        this(new ItemStack(material), null);
    }

    /**
     * Alternate constructor that takes {@link Material} instead of an {@link ItemStack}
     *
     * @param material The {@code Material} to be used when invoking class
     * @param action   The {@link GuiAction} should be passed on {@link InventoryClickEvent}
     */
    public GuiItem(@NotNull final Material material, @Nullable final GuiClickAction action) {
        this(new ItemStack(material), action);
    }

    /**
     * Gets the GuiItem's {@link ItemStack}
     *
     * @return The {@link ItemStack}
     */
    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Replaces the {@link ItemStack} of the GUI Item
     *
     * @param itemStack The new {@link ItemStack}
     */
    public void setItemStack(@NotNull final ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "The ItemStack for the GUI Item cannot be null!");
        if (itemStack.getType() != Material.AIR) {
            this.itemStack = ItemNbt.setString(itemStack.clone(), "mf-gui", uuid.toString());
        } else {
            this.itemStack = itemStack.clone();
        }
    }

    /**
     * Gets the random {@link UUID} that was generated when the GuiItem was made
     */
    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Gets the {@link GuiAction} to do when the player clicks on it
     */
    @Nullable
    public GuiClickAction getAction() {
        return action;
    }

    /**
     * Replaces the {@link GuiAction} of the current GUI Item
     *
     * @param action The new {@link GuiAction} to set
     */
    public void setAction(@Nullable final GuiClickAction action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "GuiItem{" +
                "uuid=" + uuid +
                ", action=" + action +
                ", itemStack=" + itemStack +
                '}';
    }
}
