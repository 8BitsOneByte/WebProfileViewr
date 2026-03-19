package org.exmple.webprofileviewer.client.utils;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;

public class Scheduler {
    public static Command<FabricClientCommandSource> queueOpenScreenCommand(Supplier<Screen> screenSupplier) {
        return context -> queueOpenScreen(screenSupplier.get());
    }
    public static int queueOpenScreen(Screen screen) {
        Minecraft.getInstance().schedule(() -> Minecraft.getInstance().setScreen(screen));
        return Command.SINGLE_SUCCESS;
    }
}
