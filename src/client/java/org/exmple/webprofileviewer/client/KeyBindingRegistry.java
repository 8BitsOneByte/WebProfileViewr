package org.exmple.webprofileviewer.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.exmple.webprofileviewer.client.ui.ModScreen;
import org.lwjgl.glfw.GLFW;

public final class KeyBindingRegistry {
    private static final Category KEY_CATEGORY = Category.register(Identifier.parse("webprofileviewer:custom_category"));

    private static KeyMapping openMenuKey;

    private KeyBindingRegistry() {
    }

    public static Category getKeyCategory() {
        return KEY_CATEGORY;
    }

    public static void register() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.webprofileviewer.open_mod_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (openMenuKey != null && openMenuKey.consumeClick()) {
                Minecraft.getInstance().setScreen(ModScreen.create());
            }
        });
    }
}
