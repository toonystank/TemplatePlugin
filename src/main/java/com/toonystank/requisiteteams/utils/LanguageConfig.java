package com.toonystank.requisiteteams.utils;

import com.toonystank.requisiteteams.RequisiteTeams;
import lombok.Getter;

import java.io.IOException;

@Getter
public class LanguageConfig extends FileConfig {

    private String prefix;
    private String title;
    private String playerOnly;
    private String consoleOnly;
    private String playerNotFound;
    private String noPermission;

    public LanguageConfig() throws IOException {
        super("language.yml",false,false);
        init();
    }

    private void init() throws IOException {
        prefix = getString("prefix","&#baffc9-#00ffcc&▌ &#f0fff5&"+ RequisiteTeams.getInstance().getPluginName()+" &#999999&› ");
        title = getString("title","&#baffc9-#00ffcc& " + RequisiteTeams.getInstance().getPluginName());
        noPermission = getString("no-permission","&cYou do not have permission to do this.");
        playerOnly = getString("player-only","&cOnly players can use this command.");
        consoleOnly = getString("console-only","&cOnly console can use this command.");
        playerNotFound = getString("player-not-found","&cPlayer not found."); 

    
    }

    @Override
    public void reload() throws IOException {
        super.reload();
        init();
    }

}
