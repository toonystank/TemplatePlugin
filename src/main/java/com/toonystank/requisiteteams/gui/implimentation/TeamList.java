package com.toonystank.requisiteteams.gui.implimentation;

import com.toonystank.requisiteteams.data.RequisitePlayer;
import com.toonystank.requisiteteams.gui.BaseGUI;
import com.toonystank.requisiteteams.gui.GuiData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeamList extends BaseGUI {


    public TeamList(GuiData guiData, RequisitePlayer player) {
        super(guiData, player);
    }

    @Override
    public void populateCustom(List<String> args, @Nullable RequisitePlayer admin) {

    }
}
