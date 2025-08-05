package com.toonystank.requisiteteams.gui.data.filesystem.types;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Requirement that checks if two strings are equal (case-insensitive).
 */
public class StringEqualsIgnoreCase extends Requirements {
    private String input;
    private String output;

    public StringEqualsIgnoreCase(String path, ItemSection section) {
        super(RequirementTypes.STRING_EQUALS_IGNORECASE, path, section);
    }

    @Override
    public void save() throws IOException {
        section.getMenuConfig().set(path + ".input", input);
        section.getMenuConfig().set(path + ".output", output);
        section.getMenuConfig().save();
    }

    @Override
    public void load() {
        input = section.getMenuConfig().getString(path + ".input");
        output = section.getMenuConfig().getString(path + ".output");
    }

    @Override
    public boolean parse(@NotNull RequisitePlayer player, @Nullable List<String> args, @NotNull BaseGUI manager) {
        String formattedInput = manager.getGuiData().formatText(player, input, args, true, false);
        String formattedOutput = manager.getGuiData().formatText(player, output, args, true, false);
        return formattedInput != null && formattedOutput != null && formattedInput.equalsIgnoreCase(formattedOutput);
    }
}