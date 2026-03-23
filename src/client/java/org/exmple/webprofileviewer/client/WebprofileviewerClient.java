package org.exmple.webprofileviewer.client;


import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import org.exmple.webprofileviewer.client.ui.AntiAfkHud;
import org.exmple.webprofileviewer.client.ui.ModScreen;
//弃用
//import net.minecraft.ChatFormatting;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.chat.Component;
//import com.mojang.brigadier.arguments.StringArgumentType;
//import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
//import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//由于提取了一个专门的类处理Jsoup对网页的抓取，下列导入语句已弃用并移动至BWStatsExtractor.java中
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//由于提取了一个专门的类AsyncExecutor来提供线程池服务，下列导入语句已弃用并移动至AsyncExecutor.java中
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.Executors;

public class WebprofileviewerClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Initialize UI commands
        ModScreen.initClass();
        // Register global keybindings (mod menu, shared category)
        KeyBindingRegistry.register();
        
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.CROSSHAIR,
            Identifier.parse("webprofileviewer:antiafk_hud"),
            (context, tickCounter) -> AntiAfkHud.render(context)
        );

        // Initialize other commands
        WebCommand.register();
        WeballCommand.register();
        
        // Initialize AntiAFK handler (test)
        AntiAFKHandler.register();
        
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> AsyncExecutor.shutdown());
    }
}
