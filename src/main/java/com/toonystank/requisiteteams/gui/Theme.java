package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.utils.FileConfig;

import java.io.IOException;

public class Theme extends FileConfig {


    public Theme(String fileName, String path) throws IOException {
        super(fileName + "_EN.yml", path, false, true);
    }

}
