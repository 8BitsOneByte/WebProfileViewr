package org.exmple.webprofileviewer.client;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.exmple.webprofileviewer.client.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WeballCommand {
    public static final String CMD_WEBALL = "weball";
    private static final Logger LOGGER = LoggerFactory.getLogger("webprofileviewer");
    private static final RateLimiter RATE_LIMITER = new RateLimiter(5, 1.0);

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal(CMD_WEBALL)
                        .executes(WeballCommand::execute)
                        .then(ClientCommandManager.literal("config")
                                .then(ClientCommandManager.literal("Dangerous_Players_Final_KD")
                                        // get 子命令：获取当前值
                                        .then(ClientCommandManager.literal("get")
                                                .executes(WeballCommand::handleGetCommand))
                                        // set 子命令：设置新值
                                        .then(ClientCommandManager.literal("set")
                                                .then(ClientCommandManager.argument("threshold", DoubleArgumentType.doubleArg(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY))
                                                        .executes(WeballCommand::handleSetCommand)))
                                        // reset 子命令：重置为默认值
                                        .then(ClientCommandManager.literal("reset")
                                                .executes(WeballCommand::handleResetCommand))))));
    }

    private static int handleGetCommand(CommandContext<FabricClientCommandSource> ctx) {
        double currentThreshold = ModConfig.getInstance().dangerousPlayersKDThreshold;
        ctx.getSource().sendFeedback(
            Component.literal("Current Dangerous Players KD Threshold: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.valueOf(currentThreshold)).withStyle(ChatFormatting.YELLOW))
        );
        return 1;
    }

    private static int handleSetCommand(CommandContext<FabricClientCommandSource> ctx) {
        double threshold = DoubleArgumentType.getDouble(ctx, "threshold");
        
        // 检测阈值是否 <= 0,不允许
        if (threshold <= 0.0) {
            ctx.getSource().sendFeedback(
                Component.literal("Invalid threshold value. Must be greater than 0.0")
                    .withStyle(ChatFormatting.RED)
            );
            return 0;
        }
        
        // 保存配置
        ModConfig config = ModConfig.getInstance();
        config.dangerousPlayersKDThreshold = threshold;
        config.save();
        
        // 发送阈值设置成功消息
        ctx.getSource().sendFeedback(
            Component.literal("Dangerous Players KD Threshold set to: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.valueOf(threshold)).withStyle(ChatFormatting.YELLOW))
        );
        
        return 1;
    }

    private static int handleResetCommand(CommandContext<FabricClientCommandSource> ctx) {
        ModConfig config = ModConfig.getInstance();
        config.dangerousPlayersKDThreshold = 1.0;
        config.save();
        
        ctx.getSource().sendFeedback(
            Component.literal("Dangerous Players KD Threshold reset to default: ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal("1.0").withStyle(ChatFormatting.YELLOW))
        );
        
        return 1;
    }

    private static int execute(CommandContext<FabricClientCommandSource> ctx) {
        if (Minecraft.getInstance().getConnection() == null) return 0;

        // Capture player list on main thread
        List<String> names = Minecraft.getInstance().getConnection().getListedOnlinePlayers().stream()
                .map(info -> ServiceContainer.getNameFormatter().cleanPlayerName(info.getProfile().name()))
                .collect(Collectors.toList());

        CompletableFuture.supplyAsync(() -> processPlayers(ctx, names), AsyncExecutor.getExecutor())
                .thenAcceptAsync(result -> displaySummary(ctx, result), Minecraft.getInstance())
                .exceptionally(ex -> handleError(ctx, ex));

        return 1;
    }

    private static FetchResult processPlayers(CommandContext<FabricClientCommandSource> ctx, List<String> names) {
        long startNano = System.nanoTime();
        List<PlayerKD> dangerous = new ArrayList<>();
        int total = names.size();

        for (int i = 0; i < total; i++) {
            String name = names.get(i);
            int current = i + 1;

            try {
                processSinglePlayer(ctx, name, current, total, dangerous);
            } catch (Exception e) {
                handlePlayerError(ctx, name, current, total, dangerous);
            }

            // Rate limiter
            try {
                RATE_LIMITER.consume();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        double elapsedSeconds = (System.nanoTime() - startNano) / 1_000_000_000.0;
        String summary = String.format("Finished fetching stats for all online players. Took %.3f seconds", elapsedSeconds);
        return new FetchResult(summary, dangerous);
    }

    private static void processSinglePlayer(CommandContext<FabricClientCommandSource> ctx, String name, int current, int total, List<PlayerKD> dangerous) throws Exception {
        BWStatsExtractor.BWStats stats = ServiceContainer.getStatsExtractor().extractBWStats(name);

        // Detect dangerous player using configured threshold
        String kdValue = stats.getFinalKD();
        if (kdValue != null && !GlobalConstants.NOT_FOUND.equals(kdValue)) {
            double kdVal = StatsFormatter.parseStatAsDouble(kdValue);
            double threshold = ModConfig.getInstance().dangerousPlayersKDThreshold;
            if (kdVal > threshold) {
                dangerous.add(new PlayerKD(name, kdValue));
            }
        }

        // Update UI
        Minecraft.getInstance().execute(() -> {
            String headerStr = ChatFormatting.YELLOW + name + " " + ChatFormatting.GOLD + "[" + current + "/" + total + "]:";
            ctx.getSource().sendFeedback(Component.literal(headerStr));

            for (Component line : StatsFormatter.formatStats(stats)) {
                ctx.getSource().sendFeedback(line);
            }
        });
    }

    private static void handlePlayerError(CommandContext<FabricClientCommandSource> ctx, String name, int current, int total, List<PlayerKD> dangerous) {
        dangerous.add(new PlayerKD(name, "nicked"));
        Minecraft.getInstance().execute(() -> {
            String failMsg = ChatFormatting.YELLOW + name + " " + ChatFormatting.GOLD + "[" + current + "/" + total + "]:" + ChatFormatting.RED + "\nThis player may be nicked!";
            ctx.getSource().sendFeedback(Component.literal(failMsg));
        });
    }

    private static void displaySummary(CommandContext<FabricClientCommandSource> ctx, FetchResult result) {
        ctx.getSource().sendFeedback(Component.literal(result.summary).withStyle(ChatFormatting.AQUA));

        if (result.dangerous != null && !result.dangerous.isEmpty()) {
            ctx.getSource().sendFeedback(Component.literal("Dangerous Players:").withStyle(ChatFormatting.RED));
            for (PlayerKD pk : result.dangerous) {
                Component comp = Component.literal(pk.name).withStyle(ChatFormatting.RED)
                        .append(Component.literal(" (" + pk.kd + ")").withStyle(ChatFormatting.WHITE));
                ctx.getSource().sendFeedback(comp);
            }
        }
    }

    private static Void handleError(CommandContext<FabricClientCommandSource> ctx, Throwable ex) {
        LOGGER.error("Weball execution failed", ex);
        Minecraft.getInstance().execute(() -> ctx.getSource().sendFeedback(
                Component.literal("Failed: " + ex.getMessage())));
        return null;
    }

    private record FetchResult(String summary, List<PlayerKD> dangerous) {}
    private record PlayerKD(String name, String kd) {}
}
