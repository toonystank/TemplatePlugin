package com.toonystank.requisiteteams.command;

import java.util.ArrayList;
import java.util.List;

import com.toonystank.requisiteteams.RequisiteTeams;
import org.bukkit.command.CommandSender;

import com.toonystank.requisiteteams.command.manager.SubCommand;
import com.toonystank.requisiteteams.utils.Handlers;

public class ReloadCommand implements SubCommand {


    @Override
    public void execute(CommandSender sender, String[] args) {
        RequisiteTeams.getInstance().reload();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    @Override
    public boolean hasBasePermission(CommandSender sender) {
        return Handlers.hasPermission(sender,"reload") 
        || Handlers.hasPermission(sender, "admin");
    }

}
