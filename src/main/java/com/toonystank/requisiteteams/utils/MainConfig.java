package com.toonystank.requisiteteams.utils;

import lombok.Getter;

import java.io.IOException;

@Getter
public class MainConfig extends FileConfig{

    private boolean smallText;
    private boolean debug;

    private LanguageConfig languageConfig;

    public boolean guiBedrockGuiSupportEnable;


    public MainConfig() throws IOException {
        super("config.yml",false,false);
        init();
    }

    private void init() throws IOException {
        smallText = getBoolean("utils.smallText",true);
        debug = getBoolean("utils.debug",false);
        guiBedrockGuiSupportEnable = getBoolean("gui.bedrockGuiSupportEnable", true);
        try {
            if (languageConfig != null) {
                languageConfig.reload();
                return;
            }
            languageConfig = new LanguageConfig();
        } catch (Exception e) {
            MessageUtils.error("An error happend on initializing Language.yml " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void reload() throws IOException {
        super.reload();
        init();
    }
}
