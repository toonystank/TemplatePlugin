package com.toonystank.requisiteteams.gui.data;

import com.toonystank.requisiteteams.gui.GuiRegistry;
import com.toonystank.requisiteteams.gui.Theme;
import com.toonystank.requisiteteams.utils.FileConfig;
import lombok.Getter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Represents the configuration data for a GUI menu.
 * Immutable to prevent unintended modifications.
 */
public class DataSection {
    private final FileConfig configManager;
    @Getter
    private String menuTitle;
    private List<String> args;
    @Getter
    private String version;
    @Getter
    private int rows;
    @Getter
    private GuiRegistry.GuiInfo guiInfo;
    @Getter
    private Theme theme;
    @Getter
    private final String defaultTheme;

    public DataSection(FileConfig configManager, String defaultTheme) {
        if (configManager == null) {
            throw new IllegalArgumentException("FileConfig cannot be null");
        }
        if (defaultTheme == null) {
            throw new IllegalArgumentException("Default theme cannot be null");
        }
        this.configManager = configManager;
        this.defaultTheme = defaultTheme;
        initialize();
    }

    private void initialize() {
        try {
            String menuTitle = configManager.getString("data.menu_title");
            if (menuTitle == null) {
                throw new ConfigurationException("Menu title is missing in " + configManager.getFile().getName());
            }
            this.menuTitle = menuTitle;
            this.args = configManager.getStringList("data.args");
            this.version = configManager.getString("data.version", "1.0");
            int rows = configManager.getInt("data.rows");
            if (rows <= 0 || rows > 6) {
                throw new ConfigurationException("Invalid row count (" + rows + ") in " + configManager.getFile().getName());
            }
            this.rows = rows;
            String guiType = configManager.getString("data.gui_type");
            if (guiType == null) {
                throw new ConfigurationException("GUI type is missing in " + configManager.getFile().getName());
            }
            this.guiInfo = GuiRegistry.getGUI(guiType);
            if (guiInfo == null) {
                throw new ConfigurationException("Invalid GUI type: " + guiType);
            }
            String themeName = configManager.getString("data.theme", defaultTheme);
            this.theme = new Theme(themeName, "Themes");
        } catch (IOException e) {
            throw new ConfigurationException("Failed to initialize DataSection: " + e.getMessage(), e);
        }
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

}

/**
 * Custom exception for configuration-related errors.
 */
class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}