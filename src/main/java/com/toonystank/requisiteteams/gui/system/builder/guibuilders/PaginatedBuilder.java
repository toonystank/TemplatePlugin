package com.toonystank.requisiteteams.gui.system.builder.guibuilders;

import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.system.PaginatedGuiSystem;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * GUI builder for creating a {@link PaginatedGuiSystem}
 */
public class PaginatedBuilder extends BaseGuiBuilder<PaginatedGuiSystem, PaginatedBuilder> {

    private int pageSize = 0;

    /**
     * Sets the desirable page size, most of the time this isn't needed
     *
     * @param pageSize The amount of free slots that page items should occupy
     * @return The current builder
     */
    @NotNull
    @Contract("_ -> this")
    public PaginatedBuilder pageSize(final int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * Creates a new {@link PaginatedGuiSystem}
     *
     * @return A new {@link PaginatedGuiSystem}
     */
    @NotNull
    @Override
    public PaginatedGuiSystem create(BaseGUI baseGUI) {
        final PaginatedGuiSystem gui = new PaginatedGuiSystem(getRows(), pageSize, getTitle(), getModifiers(),baseGUI);

        final Consumer<PaginatedGuiSystem> consumer = getConsumer();
        if (consumer != null) consumer.accept(gui);

        return gui;
    }

}
