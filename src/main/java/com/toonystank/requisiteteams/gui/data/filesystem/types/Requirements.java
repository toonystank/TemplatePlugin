package com.toonystank.requisiteteams.gui.data.filesystem.types;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/**
 * Base class for requirement types.
 */
@Getter
public abstract class Requirements {
    protected final RequirementTypes type;
    protected final String path;
    protected final ItemSection section;

    public Requirements(RequirementTypes type, String path, ItemSection section) {
        if (type == null) {
            throw new IllegalArgumentException("RequirementTypes cannot be null");
        }
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        if (section == null) {
            throw new IllegalArgumentException("ItemSection cannot be null");
        }
        this.type = type;
        this.path = path;
        this.section = section;
        load();
    }

    public abstract void save() throws IOException;
    public abstract void load();
    public abstract boolean parse(@NotNull RequisitePlayer player, @Nullable List<String> args, @NotNull BaseGUI manager);


}