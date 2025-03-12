package com.toonystank.requisitertp.command;

import com.toonystank.requisitertp.RequisiteRTP;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RTPCommand extends BaseCommand{

    protected RTPCommand(RequisiteRTP plugin) {
        super(plugin, "rtp"
                ,false
                , false
                ,"Teleport to a random location"
                ,"/rtp"
                ,"rollerite.rtp"
                , "wild", "wilderness", "randomtp");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public void execute(ConsoleCommandSender sender, String[] args) {

    }

    @Override
    public void execute(Player player, String[] args) {

    }
}
