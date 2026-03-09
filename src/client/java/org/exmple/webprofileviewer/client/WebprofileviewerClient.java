package org.exmple.webprofileviewer.client;


import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class WebprofileviewerClient implements ClientModInitializer {
    private static final java.util.concurrent.Executor IO_EXEC = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName("WebProfileViewer-IO");
        return thread;
    });

    // Token bucket rate limiter to replace fixed sleeps in /weball.
    // Capacity allows brief bursts; refillRate is tokens per second.
    private static final TokenBucket RATE_LIMITER = new TokenBucket(5, 1.0);

    private static class TokenBucket {
        private final double capacity;
        private final double refillPerSecond;
        private double tokens;
        private long lastRefillNanos;

        TokenBucket(double capacity, double refillPerSecond) {
            this.capacity = capacity;
            this.refillPerSecond = refillPerSecond;
            this.tokens = capacity; // start full so first few requests can go fast
            this.lastRefillNanos = System.nanoTime();
        }

        private void refill() {
            long now = System.nanoTime();
            double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
            if (elapsedSeconds <= 0) return;
            tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
            lastRefillNanos = now;
        }

        // Blocks until a token is available or the thread is interrupted.
        synchronized void consume() throws InterruptedException {
            while (true) {
                refill();
                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return;
                }
                // compute how long until next token is available (seconds)
                double needed = 1.0 - tokens;
                long waitNanos = (long) Math.ceil((needed / refillPerSecond) * 1_000_000_000.0);
                // convert to millis for Thread.sleep, but keep at least 1ms to avoid tight-loop
                long waitMillis = Math.max(1, waitNanos / 1_000_000);
                this.wait(waitMillis);
            }
        }

        // notify to wake up potential waiters when time passes; caller can optionally call
        // but since refill is time-based we don't have a separate scheduler. We'll notifyAll
        // from places that might change state after sleep; not strictly necessary here.
        synchronized void wake() {
            this.notifyAll();
        }
    }

    // Simple holder used to return both the summary message and the list of dangerous players
    private static class FetchResult {
        final String summary;
        final java.util.List<PlayerKD> dangerous;

        FetchResult(String summary, java.util.List<PlayerKD> dangerous) {
            this.summary = summary;
            this.dangerous = dangerous;
        }
    }

    // small holder for a dangerous player's name and final KD string
    private static class PlayerKD {
        final String name;
        final String kd;

        PlayerKD(String name, String kd) {
            this.name = name;
            this.kd = kd;
        }
    }

    private String cleanPlayerName(String rawName) {
        if (rawName == null || rawName.isEmpty()) {
            return "";
        }
        // StripFormatting的作用:一键移除所有§开头的格式符（如§r等）
        return ChatFormatting.stripFormatting(rawName);
    }// 方法作用：移除玩家名字中的格式符，确保后续处理时使用干净的名字

    public String extractBWStats(String Playername) throws Exception {
        String url = "https://hypixel.net/player/" + Playername;
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();
        //抓取各项数据部分：

        //4s final
        String finalKD4v4 = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "4v4v4v4 Final K/D".equalsIgnoreCase(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //2s final
        String finalKD2v2 = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "Doubles Final K/D".equalsIgnoreCase(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //total wins
        String totalWins = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "Wins".equalsIgnoreCase(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //final K/D
        String finalKD = doc.select("#stats-content-bedwars td.statName").stream()
                .filter(td -> "Final K/D".equalsIgnoreCase(td.text().trim()))
                .findFirst()
                .map(Element::parent)                  // tr
                .map(tr -> tr.selectFirst("td.statValue"))
                .map(Element::text)
                .orElse("未找到");
        //拼接数据后返回
        String stats="Final K/D:" + finalKD + "\nDoubles Final K/D:" + finalKD2v2 + "\n4v4v4v4 Final K/D:" + finalKD4v4 + "\nTotal Wins:" + totalWins;
        return stats;
    }


    @Override
    public void onInitializeClient() {

        String cmd = "web";



                ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                        dispatcher.register(ClientCommandManager.literal(cmd)
                                .executes(ctx -> {
                                    // When user types only `/web` without args, show a red usage message
                                    ctx.getSource().sendFeedback(
                                            Component.literal("Correct usage:/web <Username>").withStyle(ChatFormatting.RED)
                                    );
                                    return 1;
                                })
                                .then(ClientCommandManager.argument("playername", StringArgumentType.string())
                                        .suggests((context, builder) -> {

                                            if (Minecraft.getInstance().getConnection() != null) {
                                                String getInputPrefix = builder.getRemaining().toLowerCase();
                                                Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                                                        .map(info -> {String rawName=info.getProfile().name();
                                                            return cleanPlayerName(rawName);})
                                                        .filter(name -> name.toLowerCase().startsWith(getInputPrefix))
                                                        .forEach(builder::suggest);
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
                                                    .thenAcceptAsync(msg -> Minecraft.getInstance().execute(() -> {
                                                        // Show player name in yellow
                                                        Component header = Component.literal(cleanPlayer + ":").withStyle(ChatFormatting.YELLOW);
                                                        ctx.getSource().sendFeedback(header);

                                                        // Split stats into lines and colorize label vs value
                                                        String[] lines = msg.split("\\r?\\n");//根据换行符分割成多行
                                                        for (String line : lines) {
                                                            if (line.contains(":")) {
                                                                int colonIdx = line.indexOf(":");
                                                                String label = line.substring(0, colonIdx).trim();
                                                                String value = line.substring(colonIdx + 1).trim();
                                                                String colored = ChatFormatting.AQUA + label + ": " + ChatFormatting.WHITE + value;
                                                                ctx.getSource().sendFeedback(Component.literal(colored));
                                                            } else {
                                                                ctx.getSource().sendFeedback(Component.literal(ChatFormatting.WHITE + line));
                                                            }
                                                        }
                                                    }), Minecraft.getInstance())
                                                    .exceptionally(ex -> {
                                                        Minecraft.getInstance().execute(() -> {
                                                            // Show username in yellow, then a red friendly message
                                                            String failMsg = ChatFormatting.YELLOW + cleanPlayer + " :\n" +  ChatFormatting.RED + "This player may be nicked!";
                                                            ctx.getSource().sendFeedback(Component.literal(failMsg));
                                                        });
                                                        return null;
                                                    });

                                            return 1; // 立即返回，不阻塞服务器主线程
                                        }))));



        String cmds = "weball";




                ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                        dispatcher.register(ClientCommandManager.literal(cmds)
                                .executes(ctx -> {

                                    CompletableFuture
                                            .supplyAsync(() -> {
                                                // start timer for total elapsed time
                                                long startNano = System.nanoTime();

                                                java.util.List<PlayerKD> dangerous = new java.util.ArrayList<>();

                                                if (Minecraft.getInstance().getConnection() != null) {
                                                    // collect cleaned player names first, then iterate sequentially
                                                    String[] names = Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                                                            .map(info -> cleanPlayerName(info.getProfile().name()))
                                                            .toArray(String[]::new);
                                                    int total = names.length;
                                                    for (int idx = 0; idx < total; idx++) {
                                                        final int current = idx + 1; // effectively final for lambda
                                                        String name = names[idx];
                                                         try {
                                                             String stats = extractBWStats(name);

                                                             // detect Final K/D value from stats string
                                                             int kdIndex = stats.indexOf("Final K/D:");
                                                             if (kdIndex >= 0) {
                                                                 String rest = stats.substring(kdIndex + "Final K/D:".length());
                                                                 String firstLine = rest.split("\\r?\\n")[0].trim();
                                                                 try {
                                                                     double kdVal = Double.parseDouble(firstLine);
                                                                     if (kdVal > 1.0) {
                                                                         // store both name and KD string for later colored printing
                                                                         dangerous.add(new PlayerKD(name, firstLine));
                                                                     }
                                                                 } catch (NumberFormatException nfe) {
                                                                     // ignore unparsable values
                                                                 }
                                                             }

                                                             Minecraft.getInstance().execute(() -> {
                                                                 // Show player name and [current/total] (bracket+colon in gold)
                                                                 String headerStr = ChatFormatting.YELLOW + name + " " + ChatFormatting.GOLD + "[" + current + "/" + total + "]:";
                                                                 ctx.getSource().sendFeedback(Component.literal(headerStr));

                                                                 // Split stats into lines and colorize label vs value
                                                                 String[] lines = stats.split("\\r?\\n");
                                                                 for (String line : lines) {
                                                                     if (line.contains(":")) {
                                                                         int colonIdx = line.indexOf(":");
                                                                         String label = line.substring(0, colonIdx).trim(); // label without ':'
                                                                         String value = line.substring(colonIdx + 1).trim();
                                                                         // color label in AQUA, then a space, then value in WHITE
                                                                         String colored = ChatFormatting.AQUA + label + ": " + ChatFormatting.WHITE + value;
                                                                         ctx.getSource().sendFeedback(Component.literal(colored));
                                                                     } else {
                                                                         ctx.getSource().sendFeedback(Component.literal(ChatFormatting.WHITE + line));
                                                                     }
                                                                 }
                                                             });
                                                         } catch (Exception e) {
                                                             // On failure, show a user-friendly, colored message similar to /web
                                                             Minecraft.getInstance().execute(() -> {
                                                                 String failMsg = ChatFormatting.YELLOW + name + " " + ChatFormatting.GOLD + "[" + current + "/" + total + "]:" + ChatFormatting.RED + "\nThis player may be nicked!";
                                                                 ctx.getSource().sendFeedback(Component.literal(failMsg));
                                                             });
                                                         }
                                                         //设置延迟，防止触发Hypixel的反爬虫机制
                                                        try {
                                                            // Use token-bucket based rate limiter instead of fixed sleep.
                                                            // This blocks only as long as necessary and allows short bursts.
                                                            RATE_LIMITER.consume();
                                                        } catch (InterruptedException ie) {
                                                            Thread.currentThread().interrupt();
                                                            // If interrupted, stop further processing
                                                            break;
                                                        }
                                                    }
                                                }

                                                double elapsedSeconds = (System.nanoTime() - startNano) / 1_000_000_000.0;
                                                String summary = String.format("Finished fetching stats for all online players. Took %.3f seconds", elapsedSeconds);
                                                return new FetchResult(summary, dangerous);
                                            }, IO_EXEC)
                                            .thenAcceptAsync(result -> {
                                                // print summary first
                                                ctx.getSource().sendFeedback(Component.literal(result.summary).withStyle(ChatFormatting.AQUA));

                                                // if any dangerous players found, print a red header and each name in red on its own line
                                                if (result.dangerous != null && !result.dangerous.isEmpty()) {
                                                    ctx.getSource().sendFeedback(Component.literal("Dangerous Players:").withStyle(ChatFormatting.RED));
                                                    for (PlayerKD pk : result.dangerous) {
                                                        // name in red, KD in white within parentheses
                                                        Component comp = Component.literal(pk.name).withStyle(ChatFormatting.RED)
                                                                .append(Component.literal(" (" + pk.kd + ")").withStyle(ChatFormatting.WHITE));
                                                        ctx.getSource().sendFeedback(comp);
                                                    }
                                                }
                                            }, Minecraft.getInstance())
                                             .exceptionally(ex -> {
                                                 Minecraft.getInstance().execute(() -> ctx.getSource().sendFeedback(
                                                         Component.literal("Failed: " + ex.getMessage())));
                                                 return null;
                                             });

                                    return 1; // 立即返回，不阻塞服务器主线程
                                })));
            }//onInitializeClient的大括号



    }//WebprofileviewerClient类的大括号













