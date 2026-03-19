package org.exmple.webprofileviewer.client.config;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.exmple.webprofileviewer.client.GlobalConstants;
import org.jspecify.annotations.Nullable;

public class ModConfigManager {
    public static Screen createGUI(@Nullable Screen parent) {
        return createGUI(parent, "");
    }
    public static Screen createGUI(@Nullable Screen parent, String search) {
        return new ConfigScreen(parent, Component.literal("WebProfileViewer Config Menu" ));
    }
}
