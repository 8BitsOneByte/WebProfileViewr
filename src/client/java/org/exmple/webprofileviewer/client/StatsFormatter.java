package org.exmple.webprofileviewer.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.exmple.webprofileviewer.client.config.ModConfig;

import java.util.*;
import java.util.function.Function;

public class StatsFormatter {
    // 定义要显示的统计字段配置
    private static final LinkedHashMap<String, Function<BWStatsExtractor.BWStats, String>> STAT_FIELDS =
            new LinkedHashMap<>();

    static {
        STAT_FIELDS.put(BWStatsConstants.BWCONST_FINAL_KD, BWStatsExtractor.BWStats::getFinalKD);
        STAT_FIELDS.put(BWStatsConstants.BWCONST_DOUBLES_FINAL_KD, BWStatsExtractor.BWStats::getFinalKD2v2);
        STAT_FIELDS.put(BWStatsConstants.BWCONST_4V4V4V4_FINAL_KD, BWStatsExtractor.BWStats::getFinalKD4v4);
        STAT_FIELDS.put(BWStatsConstants.BWCONST_TOTALWINS, BWStatsExtractor.BWStats::getTotalWins);
    }

    // 将统计数据格式化存储在Component列表中，根据配置动态决定显示哪些字段
    public static List<Component> formatStats(BWStatsExtractor.BWStats stats) {
        List<Component> components = new ArrayList<>();
        ModConfig config = ModConfig.getInstance();  // 读取配置
        
        for (Map.Entry<String, Function<BWStatsExtractor.BWStats, String>> entry : STAT_FIELDS.entrySet()) {
            String label = entry.getKey();
            String value = entry.getValue().apply(stats);
            
            // 根据配置决定是否显示该字段
            if (shouldDisplayField(label, config)) {
                components.add(formatLine(label, value));
            }
        }
        
        return components;
    }
    

    private static boolean shouldDisplayField(String fieldLabel, ModConfig config) {
        if (BWStatsConstants.BWCONST_FINAL_KD.equalsIgnoreCase(fieldLabel)) {
            return config.showFinalKD;
        } else if (BWStatsConstants.BWCONST_DOUBLES_FINAL_KD.equalsIgnoreCase(fieldLabel)) {
            return config.showDoublesFinalKD;
        } else if (BWStatsConstants.BWCONST_4V4V4V4_FINAL_KD.equalsIgnoreCase(fieldLabel)) {
            return config.showQuadsFinalKD;
        } else if (BWStatsConstants.BWCONST_TOTALWINS.equalsIgnoreCase(fieldLabel)) {
            return config.showTotalWins;
        }
        
        // 默认显示（对于未配置的字段）
        return true;
    }

    private static Component formatLine(String label, String value) {
        // 将网页标签转换为显示标签(主要是由于Total Wins在网页上显示为Wins,容易引起歧义)
        String displayLabel = label;
        if (BWStatsConstants.BWCONST_TOTALWINS.equalsIgnoreCase(label)) {
            displayLabel = "Total Wins";
        }
        
        return Component.literal(displayLabel + ": ")
                .withStyle(ChatFormatting.AQUA)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    public static double parseStatAsDouble(String statValue) {
        //将返回的字符串类型的统计数据解析为double格式
        try {
            return Double.parseDouble(statValue);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}



