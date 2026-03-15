package org.exmple.webprofileviewer.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class StatsFormatter {
    //将统计数据格式化存储在Component数组中，方便后续对不同部分进行对应的格式化处理
    public static Component[] formatStatsLines(String statsString) {
        String[] lines = statsString.split("\\r?\\n");
        Component[] components = new Component[lines.length];

        for (int i = 0; i < lines.length; i++) {
            components[i] = formatSingleLine(lines[i]);
        }

        return components;
    }

    public static Component formatSingleLine(String line) {
        //解析单行的数据，如果该行为Name:Value格式，则将Name部分设置为AQUA颜色，Value部分设置为WHITE颜色；如果不包含冒号，则整行设置为WHITE颜色
        if (line.contains(":")) {
            int colonIdx = line.indexOf(":");
            String label = line.substring(0, colonIdx).trim();
            String value = line.substring(colonIdx + 1).trim();
            MutableComponent component = Component.literal(label + ": ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(value).withStyle(ChatFormatting.WHITE));

            return component;
        } else {
            return Component.literal(line).withStyle(ChatFormatting.WHITE);
        }
    }

    public static String extractStatValue(String statsString, String statLabel) {
        //提取特定统计数据名对应的值，返回这个值的字符串形式；如果没有找到对应的统计数据名，则返回null
        int index = statsString.indexOf(statLabel);
        if (index < 0) {
            return null;
        }
        String rest = statsString.substring(index + statLabel.length());
        String firstLine = rest.split("\\r?\\n")[0].trim();

        return firstLine;
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
