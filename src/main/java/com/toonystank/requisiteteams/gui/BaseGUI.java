package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.GeyserHandler;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import com.toonystank.requisiteteams.gui.data.filesystem.RequirementSectionTypes;
import com.toonystank.requisiteteams.gui.system.Gui;
import com.toonystank.requisiteteams.gui.system.PaginatedGuiSystem;
import com.toonystank.requisiteteams.gui.system.SimpleBedrockGUI;
import com.toonystank.requisiteteams.gui.system.components.GuiClickAction;
import com.toonystank.requisiteteams.gui.system.GuiItem;
import com.toonystank.requisiteteams.utils.MessageUtils;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class BaseGUI {

    private final GuiData guiData;
    private final GuiRegistry.GuiInfo guiInfo;

    private final RequisitePlayer player;
    public UnifiedGUI gui;

    private final Map<UUID, GuiClickAction> itemActions = new HashMap<>();

    public BaseGUI(GuiData guiData,RequisitePlayer player) {
        this.guiData = guiData;
        this.guiInfo = guiData.getDataHolder().getDataSection().getGuiInfo();
        this.player = player;
        if (GeyserHandler.isBedrockPlayer(player.getUuid())) {
            SimpleBedrockGUI bedrockGui = new SimpleBedrockGUI(); // Adjust constructor as needed
            gui = new SimpleBedrockGuiAdapter(bedrockGui);
        } else {
            PaginatedGuiSystem paginatedGui = Gui.paginated().title(Component.text("")).rows(guiData.getRow()).create(this);
            paginatedGui.setGuiInfo(guiInfo);
            gui = new PaginatedGuiAdapter(paginatedGui);
        }
    }

    // Register a GuiItem's action in the persistent storage
    private void registerItemAction(GuiItem item) {
        if (item.getAction() != null) {
            itemActions.put(item.getUuid(), item.getAction());
        }
    }

    // Restore a GuiItem's action from persistent storage
    public void restoreItemAction(GuiItem item) {
        GuiClickAction action = itemActions.get(item.getUuid());
        if (action != null) {
            item.setAction(action);
            MessageUtils.debug("Restored action for item UUID: " + item.getUuid());
        } else {
            MessageUtils.debug("No action found for item UUID: " + item.getUuid());
        }
    }

    private void populateExtras(List<String> args, @Nullable RequisitePlayer admin) {
        guiData.getDataHolder().getPrioritizedItemSections().forEach((slot, sections) -> {
            if (sections.isEmpty()) return;
            ItemSection section = sections.size() == 1
                    ? sections.get(0)
                    : guiData.getDataHolder().getCurrentPrioritizedItemSections(slot, player, this, args, RequirementSectionTypes.VIEW_REQUIREMENT);
            if (section == null) {
                return;
            }
            MessageUtils.debug("Adding section: " + section.getSectionName() + " to slot: " + slot);
            addSection(args, admin, slot, section);
        });
        populateCustom(args, admin);
        setMenuControl(args);
        openGUI(admin);
    }

    public abstract void populateCustom(List<String> args, @Nullable RequisitePlayer admin);

    private void addSection(List<String> args, @Nullable RequisitePlayer admin, Integer slot, ItemSection section) {
/*        if (containsSortPath(section.getSectionName())) {
            GuiItem item = sortFilter.getFilterItem(args, player, section, admin);
            registerItemAction(item);
            setItem(List.of(slot), item, false, section);
            return;
        }*/
        if (GuiRegistry.getCustomTypes().stream().anyMatch(guiInfo -> guiInfo.guiName().equalsIgnoreCase(section.getSectionName()))) {
            return;
        }
        GuiItem item = guiData.createItem(player, args, section, false, this, null);
        setGuiItem(slot, item, args, section, admin);
    }

    private void setMenuControl(List<String> args) {
        for (ItemSection menuControl : guiData.getMenuControls()) {
            GuiItem item = guiData.createItem(player, menuControl.getSectionName(), args, true, this);
            MessageUtils.debug("Setting menu control item: " + menuControl.getSectionName());
            item.setAction(event -> {
                if (GeyserHandler.isBedrockPlayer(player.getUuid())) {
                    SimpleBedrockGUI bedrockGui = getSimpleBedrockGUI(menuControl);
                    bedrockGui.refresh();
                } else {
                    PaginatedGuiAdapter adapter = (PaginatedGuiAdapter) gui;
                    PaginatedGuiSystem paginatedGui = adapter.gui();
                    MessageUtils.debug("Menu control clicked: " + menuControl.getSectionName());
                    boolean pageChanged;
                    if (menuControl.getSectionName().equals("Next")) {
                        pageChanged = paginatedGui.next();
                    } else {
                        pageChanged = paginatedGui.previous();
                    }
                    if (pageChanged) {
                        // Update menu control items' ItemStacks without replacing GuiItems
                        ItemSection next = guiData.getMenuControls("Next");
                        ItemSection previous = guiData.getMenuControls("Previous");
                        if (next != null && !next.getSlots().isEmpty()) {
                            GuiItem nextItem = paginatedGui.getGuiItem(next.getSlots().get(0));
                            if (nextItem != null) {
                                ItemStack updatedStack = guiData.createItem(player, "Next", args, true, this).getItemStack();
                                nextItem.setItemStack(updatedStack);
                                restoreItemAction(nextItem);
                                gui.updateItem(next.getSlots(), nextItem, next);
                                MessageUtils.debug("Next item action after update: " + (nextItem.getAction() != null ? nextItem.getAction() : "null"));
                            } else {
                                MessageUtils.warning("Next item not found in slot: " + next.getSlots().get(0));
                            }
                        } else {
                            MessageUtils.warning("Next menu control section is null or has no slots");
                        }
                        if (previous != null && !previous.getSlots().isEmpty()) {
                            GuiItem prevItem = paginatedGui.getGuiItem(previous.getSlots().get(0));
                            if (prevItem != null) {
                                ItemStack updatedStack = guiData.createItem(player, "Previous", args, true, this).getItemStack();
                                prevItem.setItemStack(updatedStack);
                                restoreItemAction(prevItem);
                                gui.updateItem(previous.getSlots(), prevItem, previous);
                                MessageUtils.debug("Previous item action after update: " + (prevItem.getAction() != null ? prevItem.getAction() : "null"));
                            } else {
                                MessageUtils.warning("Previous item not found in slot: " + previous.getSlots().get(0));
                            }
                        } else {
                            MessageUtils.warning("Previous menu control section is null or has no slots");
                        }
                    }
                }
            });
            registerItemAction(item);
            setItem(menuControl.getSlots(), item, false, menuControl);
        }
    }

    private SimpleBedrockGUI getSimpleBedrockGUI(ItemSection menuControl) {
        SimpleBedrockGuiAdapter adapter = (SimpleBedrockGuiAdapter) gui;
        SimpleBedrockGUI bedrockGui = adapter.gui();
        if (menuControl.getSectionName().equals("Next")) {
            bedrockGui.nextPage(); // Implement nextPage in SimpleBedrockGUI
        } else if (menuControl.getSectionName().equals("Previous")) {
            bedrockGui.previousPage(); // Implement previousPage in SimpleBedrockGUI
        }
        return bedrockGui;
    }

    private void setGuiItem(int slot, GuiItem item, List<String> args, ItemSection section, @Nullable RequisitePlayer admin) {
        if (section.isMenuControl()) return;
        item.setAction(event -> {
            guiData.execute(player, player, section.getSectionName(), args, BaseGUI.this, event != null && event.isLeftClick(), admin);
        });
        registerItemAction(item);
        setItem(List.of(slot), item, false, section);
    }

    private void setItem(List<Integer> slots, GuiItem item, boolean update, ItemSection itemSection) {
        slots.forEach(slot -> setItem(slot, item, update, itemSection));
    }

    private void setItem(Integer slot, GuiItem item, boolean update, ItemSection itemSection) {
        try {
            if (update) {
                restoreItemAction(item);
                gui.updateItem(List.of(slot), item, itemSection);
            } else {
                gui.setItem(List.of(slot), item, itemSection);
            }
        } catch (Exception e) {
            MessageUtils.warning("Error setting item to slot " + slot + " in " + guiData.getDataHolder().getDataSection().getGuiInfo());
        }
    }

    public void openGUI(@Nullable RequisitePlayer admin) {
        RequisitePlayer targetPlayer = (admin != null) ? admin : this.player;
        if (!gui.isViewer(targetPlayer)) {
            gui.open(targetPlayer);
        } else {
            gui.update();
        }
    }
}
