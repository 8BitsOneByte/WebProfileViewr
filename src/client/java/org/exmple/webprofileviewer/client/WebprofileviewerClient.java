package org.exmple.webprofileviewer.client;


import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class WebprofileviewerClient implements ClientModInitializer {
    private static final java.util.concurrent.Executor IO_EXEC = Executors.newCachedThreadPool(runnable->{
        Thread thread = new Thread(runnable);

        thread.setDaemon(true); // 设置为守护线程，确保不会阻止JVM退出
        thread.setName("WebProfileViewer-IO-Thread");
        return thread;
    });//创建线程池
    //清理玩家名称，去除格式符
    private String cleanPlayerName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "";
        }
        // StripFormatting的作用:一键移除所有§开头的格式符（如§r等）
        return ChatFormatting.stripFormatting(rawName);
    }
    public String extractBWStats(String PlayerName) throws Exception{
        String url ="https://hypixel.net/player/"+PlayerName;
        Document doc =Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();
        //抓取各项数据部分：

        //4s final
        String finalKD4v4 = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "4v4v4v4 Final K/D".equals(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //2s final
        String finalKD2v2 = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "Doubles Final K/D".equals(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //total wins(未知原因无法使用?)(更新：2026/3/6 18:32已修复，现可用)
        String totalWins= doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "Wins".equals(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //final K/D
        String finalKD = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "Final K/D".equals(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //3s final（仅测试使用）
        String finalKD3v3 = doc.select("td.statName").stream()
                .filter(td -> "3v3v3v3 Final K/D".equals(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");

        //拼接数据后返回
        String BWStats=PlayerName+"'s Bedwars Stats:Final K/D:"+finalKD+"\nDoubles Final K/D:"+finalKD2v2+"\n4v4v4v4 Final K/D:"+finalKD4v4+"\nTotal Wins:"+totalWins;

        return BWStats;
    }




    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("web")
                        .then(ClientCommandManager.argument("playername", StringArgumentType.string())
                                .suggests((context, builder) -> {

                                    if (Minecraft.getInstance().getConnection() != null) {
                                        String getInputPrefix = builder.getRemaining().toLowerCase();
                                        List<String> matchedPlayers = Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                                                .map(info -> {String rawName=info.getProfile().name();
                                                return cleanPlayerName(rawName);})
                                                .filter(name -> name.toLowerCase().startsWith(getInputPrefix))
                                                .toList();
                                        return SharedSuggestionProvider.suggest(matchedPlayers, builder);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String player = StringArgumentType.getString(ctx, "playername");
                                    String cleanPlayer = cleanPlayerName(player);
                                    CompletableFuture
                                            .supplyAsync(() -> {
                                                try {
                                                    return extractBWStats(cleanPlayer);
                                                } catch (Exception e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }, IO_EXEC)
                                            .thenAcceptAsync(msg -> ctx.getSource().sendFeedback(
                                                    Component.literal(msg).withStyle(ChatFormatting.AQUA)),Minecraft.getInstance())
                                            .exceptionally(ex -> {
                                               Minecraft.getInstance().execute(()->ctx.getSource().sendFeedback(
                                                       Component.literal("Failed: " + ex.getMessage()))) ;
                                                return null;
                                            });

                                    return 1; // 立即返回，不阻塞服务器主线程

                                })))

        );

    }
}












