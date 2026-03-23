package org.exmple.webprofileviewer.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.exmple.webprofileviewer.client.config.ModConfig;
import org.lwjgl.glfw.GLFW;

public class AntiAFKHandler {
    private static KeyMapping toggleKey;

    public static void register() {
        AntiAFKManager.setEnabled(ModConfig.getInstance().antiAFK);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.webprofileviewer.antiafk_toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            KeyBindingRegistry.getKeyCategory()
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey != null && toggleKey.consumeClick()) {
                ModConfig config = ModConfig.getInstance();
                config.antiAFK = !config.antiAFK;
                config.save();
                AntiAFKManager.setEnabled(config.antiAFK);
            }

            AntiAFKManager.update(client.player);
        });
    }
}
