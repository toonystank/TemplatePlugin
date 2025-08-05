package com.toonystank.requisiteteams.gui.system.components;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Serializable {

    List<String> encodeGui();

    void decodeGui(@NotNull final List<String> gui);

}
