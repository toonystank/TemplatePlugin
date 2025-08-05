package com.toonystank.requisiteteams.gui.system;

import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.system.components.InteractionModifier;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * GUI that allows you to have multiple pages
 */
@SuppressWarnings("unused")
public class PaginatedGuiSystem extends GUISystem {

    // List with all the page items
    private final List<GuiItem> pageItems = new ArrayList<>();
    // Saves the current page items and their slots
    private final Map<Integer, GuiItem> currentPage;
    // Reference to BaseGUI for action restoration
    @Getter
    private final BaseGUI baseGUI;

    private int pageSize;
    @Setter
    private int pageNum = 1;

    /**
     * Main constructor to provide a way to create PaginatedGui
     *
     * @param rows                 The amount of rows the GUI should have
     * @param pageSize             The page size
     * @param title                The GUI's title using {@link String}
     * @param interactionModifiers A set containing what {@link InteractionModifier} this GUI should have
     * @param baseGUI              The BaseGUI instance for action restoration
     */
    public PaginatedGuiSystem(final int rows, final int pageSize, @NotNull final Component title, @NotNull final Set<InteractionModifier> interactionModifiers, BaseGUI baseGUI) {
        super(rows, title, interactionModifiers);
        this.pageSize = pageSize;
        int inventorySize = rows * 9;
        this.currentPage = new LinkedHashMap<>(inventorySize);
        this.baseGUI = baseGUI;
    }

    /**
     * Old main constructor of the PaginatedGui
     *
     * @param rows     The rows the GUI should have
     * @param pageSize The pageSize
     * @param title    The GUI's title
     * @param baseGUI  The BaseGUI instance for action restoration
     */
    public PaginatedGuiSystem(final int rows, final int pageSize, @NotNull final Component title, BaseGUI baseGUI) {
        super(rows, title);
        this.pageSize = pageSize;
        int inventorySize = rows * 9;
        this.currentPage = new LinkedHashMap<>(inventorySize);
        this.baseGUI = baseGUI;
    }

    /**
     * Alternative constructor that doesn't require the {@link #pageSize} to be defined
     *
     * @param rows    The rows the GUI should have
     * @param title   The GUI's title
     * @param baseGUI The BaseGUI instance for action restoration
     */
    public PaginatedGuiSystem(final int rows, @NotNull final Component title, BaseGUI baseGUI) {
        this(rows, 0, title, baseGUI);
    }

    /**
     * Alternative constructor that only requires title
     *
     * @param title   The GUI's title
     * @param baseGUI The BaseGUI instance for action restoration
     */
    public PaginatedGuiSystem(@NotNull final Component title, BaseGUI baseGUI) {
        this(2, title, baseGUI);
    }

    public GUISystem setPageSize(final int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public void addItem(@NotNull final GuiItem item) {
        pageItems.add(item);
    }

    @Override
    public void addItem(@NotNull final GuiItem... items) {
        pageItems.addAll(Arrays.asList(items));
    }

    @Override
    public void update() {
        getInventory().clear();
        populateGui();
        updatePage();
        replaceAirItem();
    }

    public void updatePageItem(final int slot, @NotNull final ItemStack itemStack) {
        if (!currentPage.containsKey(slot)) return;
        final GuiItem guiItem = currentPage.get(slot);
        guiItem.setItemStack(itemStack);
        getInventory().setItem(slot, guiItem.getItemStack());
    }

    public void updatePageItem(final int row, final int col, @NotNull final ItemStack itemStack) {
        updatePageItem(getSlotFromRowCol(row, col), itemStack);
    }

    public void updatePageItem(final int slot, @NotNull final GuiItem item) {
        if (!currentPage.containsKey(slot)) return;
        final GuiItem oldItem = currentPage.get(slot);
        final int index = pageItems.indexOf(oldItem);
        currentPage.put(slot, item);
        if (index != -1) {
            pageItems.set(index, item);
        }
        getInventory().setItem(slot, item.getItemStack());
        if (baseGUI != null) {
            baseGUI.restoreItemAction(item);
        }
    }

    public void updatePageItem(final int row, final int col, @NotNull final GuiItem item) {
        updatePageItem(getSlotFromRowCol(row, col), item);
    }

    public void removePageItem(@NotNull final GuiItem item) {
        pageItems.remove(item);
        updatePage();
    }

    public void removePageItem(@NotNull final ItemStack item) {
        final Optional<GuiItem> guiItem = pageItems.stream().filter(it -> it.getItemStack().equals(item)).findFirst();
        guiItem.ifPresent(this::removePageItem);
    }

    @Override
    public void open(@NotNull final HumanEntity player) {
        open(player, 1);
    }

    public void open(@NotNull final HumanEntity player, final int openPage) {
        if (player.isSleeping()) return;
        if (openPage <= getPagesNum() && openPage > 0) pageNum = openPage;
        getInventory().clear();
        currentPage.clear();
        populateGui();
        if (pageSize == 0) pageSize = calculatePageSize();
        populatePage();
        replaceAirItem();
        player.openInventory(getInventory());
    }

    @Override
    @NotNull
    public GUISystem updateTitle(@NotNull final Component title) {
        setUpdating(true);
        final List<HumanEntity> viewers = new ArrayList<>(getInventory().getViewers());
        setInventory(Bukkit.createInventory(this, getInventory().getSize(), title));
        for (final HumanEntity player : viewers) {
            open(player, getPageNum());
        }
        setUpdating(false);
        return this;
    }

    @NotNull
    public Map<@NotNull Integer, @NotNull GuiItem> getCurrentPageItems() {
        return Collections.unmodifiableMap(currentPage);
    }

    @NotNull
    public List<@NotNull GuiItem> getPageItems() {
        return Collections.unmodifiableList(pageItems);
    }

    public int getCurrentPageNum() {
        return pageNum;
    }

    public int getNextPageNum() {
        if (pageNum + 1 > getPagesNum()) return pageNum;
        return pageNum + 1;
    }

    public int getPrevPageNum() {
        if (pageNum - 1 == 0) return pageNum;
        return pageNum - 1;
    }

    public boolean next() {
        MessageUtils.debug("Going to next page: " + (pageNum + 1) + " from " + getPagesNum());
        if (pageNum + 1 > getPagesNum()) return false;
        MessageUtils.debug("Next page is: " + (pageNum + 1));
        pageNum++;
        updatePage();
        return true;
    }

    public boolean previous() {
        if (pageNum - 1 == 0) return false;
        pageNum--;
        updatePage();
        return true;
    }

    GuiItem getPageItem(final int slot) {
        return currentPage.get(slot);
    }

    private List<GuiItem> getPageNum(final int givenPage) {
        final int page = givenPage - 1;
        final List<GuiItem> guiPage = new ArrayList<>();
        int max = ((page * pageSize) + pageSize);
        if (max > pageItems.size()) max = pageItems.size();
        for (int i = page * pageSize; i < max; i++) {
            guiPage.add(pageItems.get(i));
        }
        return guiPage;
    }

    public int getPagesNum() {
        return (int) Math.ceil((double) pageItems.size() / pageSize);
    }

    private void populatePage() {
        int slot = 0;
        final Iterator<GuiItem> iterator = getPageNum(pageNum).iterator();
        while (iterator.hasNext()) {
            if (slot >= getInventory().getSize()) {
                break;
            }
            if (getGuiItem(slot) != null || getInventory().getItem(slot) != null) {
                slot++;
                continue;
            }
            final GuiItem guiItem = iterator.next();
            // Restore action from BaseGUI's itemActions map
            if (guiItem.getAction() == null && baseGUI != null) {
                baseGUI.restoreItemAction(guiItem);
            }
            currentPage.put(slot, guiItem);
            getInventory().setItem(slot, guiItem.getItemStack());
            slot++;
        }
    }

    Map<Integer, GuiItem> getMutableCurrentPageItems() {
        return currentPage;
    }

    void clearPage() {
        for (Map.Entry<Integer, GuiItem> entry : currentPage.entrySet()) {
            getInventory().setItem(entry.getKey(), null);
        }
    }

    public void clearPageItems(final boolean update) {
        pageItems.clear();
        if (update) update();
    }

    public void clearPageItems() {
        clearPageItems(false);
    }

    int getPageSize() {
        return pageSize;
    }

    int getPageNum() {
        return pageNum;
    }

    void updatePage() {
        clearPage();
        populatePage();
    }

    int calculatePageSize() {
        int counter = 0;
        for (int slot = 0; slot < getRows() * 9; slot++) {
            if (getInventory().getItem(slot) == null) counter++;
        }
        return counter;
    }
}