package com.toonystank.requisiteteams.gui.system;

import com.toonystank.requisiteteams.gui.system.builder.guibuilders.PaginatedBuilder;
import com.toonystank.requisiteteams.gui.system.components.GuiType;
import com.toonystank.requisiteteams.gui.system.components.InteractionModifier;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Standard GUI implementation of {@link GUISystem}
 */
public class Gui extends GUISystem {

    /**
     * Main constructor for the GUI
     *
     * @param rows                 The amount of rows the GUI should have
     * @param title                The GUI's title using {@link String}
     * @param interactionModifiers A set containing the {@link InteractionModifier} this GUI should use
     */
    public Gui(final int rows, @NotNull final Component title, @NotNull final Set<InteractionModifier> interactionModifiers) {
        super(rows, title, interactionModifiers);
    }

    /**
     * Alternative constructor that takes both a {@link GuiType} and a set of {@link InteractionModifier}
     *
     * @param guiType              The {@link GuiType} to be used
     * @param title                The GUI's title using {@link String}
     * @param interactionModifiers A set containing the {@link InteractionModifier} this GUI should use
     */
    public Gui(@NotNull final GuiType guiType, @NotNull final Component title, @NotNull final Set<InteractionModifier> interactionModifiers) {
        super(guiType, title, interactionModifiers);
    }

    /**
     * Old main constructor for the GUI
     *
     * @param rows  The amount of rows the GUI should have
     * @param title The GUI's title
     * @deprecated In favor of {@link Gui#Gui(int, Component, Set)}
     */
    @Deprecated
    public Gui(final int rows, @NotNull final Component title) {
        super(rows, title);
    }

    /**
     * Alternative constructor that defaults to 1 row
     *
     * @param title The GUI's title
     * @deprecated In favor of {@link Gui#Gui(int, Component, Set)}
     */
    @Deprecated
    public Gui(@NotNull final Component title) {
        super(1, title);
    }

    /**
     * Main constructor that takes a {@link GuiType} instead of rows
     *
     * @param guiType The {@link GuiType} to be used
     * @param title   The GUI's title
     * @deprecated In favor of {@link Gui#Gui(GuiType, Component, Set)}
     */
    @Deprecated
    public Gui(@NotNull final GuiType guiType, @NotNull final Component title) {
        super(guiType, title);
    }

    /**
     * Creates a {@link PaginatedBuilder} to build a {@link PaginatedGuiSystem}
     *
     * @return A {@link PaginatedBuilder}
     */
    @NotNull
    @Contract(" -> new")
    public static PaginatedBuilder paginated() {
        return new PaginatedBuilder();
    }


}
