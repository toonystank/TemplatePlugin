package com.toonystank.requisiteteams.gui.system.components;


@FunctionalInterface
public interface GuiClickAction {

    /**
     * Executes the event passed to it
     *
     * @param record Inventory action
     */
    void execute(final ActionRecord record);
}