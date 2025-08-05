package com.toonystank.requisiteteams.gui.data.filesystem.types;

import com.toonystank.requisiteteams.RequisiteTeams;
import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.data.ItemSection;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
public class HasMoney extends Requirements {


    public static RequirementTypes type = RequirementTypes.HAS_MONEY;
    public String isInverted;
    @Setter
    public String money;

    public HasMoney(String path, @NotNull ItemSection section) {
        super(type, path, section);
    }

    @Override
    public void save() throws IOException {
        super.section.getMenuConfig().set(path + ".amount", money);
        super.section.getMenuConfig().save();
    }

    @Override
    public void load() {
        setMoney(super.section.getMenuConfig().getString(path + ".amount"));
    }

    @Override
    public boolean parse(@NotNull RequisitePlayer player, @Nullable List<String> args, @NotNull BaseGUI manager) {
        return RequisiteTeams.getInstance().getVault().has(player.getPlayer(), Double.parseDouble(money));
    }
    

}
