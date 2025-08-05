package com.toonystank.requisiteteams.gui;

import com.toonystank.requisiteteams.gui.implimentation.TeamList;
import com.toonystank.requisiteteams.utils.MessageUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuiRegistry {

    public static final Map<String,GuiInfo> guis = new ConcurrentHashMap<>();


    public GuiRegistry() {
        // Initialize the GUI registry if needed
        // This could be used to load default GUIs or perform initial setup
        registerGUI("teamlist","TeamList.yml",false, TeamList.class);
        MessageUtils.debug("GUI Registry initialized.");

    }

    public static GuiInfo getGUI(String guiName) {
        return guis.get(guiName.toLowerCase());
    }
    public static boolean isGUI(String guiName) {
        return guis.containsKey(guiName.toLowerCase());
    }
    public static GuiInfo registerGUI(String guiName,String fileName,boolean isCustom,Class<? extends BaseGUI> guiClass) {
        if (isGUI(guiName)) {
            MessageUtils.warning("GUI with name " + guiName + " already exists. Skipping new registration.");
            return getGUI(guiName);
        }
        GuiInfo info = new GuiInfo(guiName,fileName,isCustom,guiClass);
        guis.put(guiName.toLowerCase(),info);
        return info;
    }
    public static List<GuiInfo> getCustomTypes() {
        return guis.values().stream().filter(GuiInfo::isCustom).toList();
    }


    public record GuiInfo(String guiName, String fileName, boolean isCustom,Class<? extends BaseGUI> guiClass) {

    }

}
