package com.toonystank.requisiteteams;

import lombok.Getter;
import lombok.Setter;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class GeyserHandler {

    @Getter@Setter
    private @Nullable static FloodgateApi floodgateApi;

    public static boolean isBedrockPlayer(UUID uuid) {
        if (!RequisiteTeams.getInstance().getMainConfig().guiBedrockGuiSupportEnable) return false;
        if (floodgateApi== null) return false;
        return floodgateApi.isFloodgatePlayer(uuid);
    }
}
