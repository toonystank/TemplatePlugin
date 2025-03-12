package com.toonystank.requisitertp.utils;

import lombok.Getter;

import java.io.IOException;

@Getter
public class LanguageConfig extends FileConfig {

    private String playerOnly;
    private String consoleOnly;
    private String playerNotFound;
    private String noPermission;

    public LanguageConfig() throws IOException {
        super("language.yml",false,false);
    }

}
