package com.toonystank.requisitertp.utils;

import lombok.Getter;

import java.io.IOException;

@Getter
public class MainConfig extends FileConfig{

    private boolean smallText;
    private boolean debug;
    private LanguageConfig languageConfig;

    public MainConfig() throws IOException {
        super("config.yml",false,false);
    }
}
