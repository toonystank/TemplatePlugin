package com.toonystank.requisiteteams.gui.data.filesystem.types;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@Setter
@Getter
public class HasPermission extends Requirements {

    public static RequirementTypes type = RequirementTypes.HAS_PERMISSION;
    public String permission;
    public HasPermission(String path, ItemSection section) {
        super(type,path,section);
    }

    @Override
    public void save() throws IOException {
        super.section.getMenuConfig().set(path + ".permission", permission);
        super.section.getMenuConfig().save();
    }

    @Override
    public void load() {
        permission = super.section.getMenuConfig().getString(path + ".permission");
    }

    @Override
    public boolean parse(@NotNull RequisitePlayer player, @Nullable List<String> args, @NotNull BaseGUI manager) {
        if (player.getPlayer().isOp()) return true;
        if (!player.getPlayer().isOnline()) return false;
        return player.getOnlinePlayer().hasPermission(permission);
    }

}
