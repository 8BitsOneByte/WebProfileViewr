package org.exmple.webprofileviewer.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "webprofileviewer.json";
    
    // 配置字段
    public boolean antiAFK = false;
    public boolean antiAfkIconSmall = true;
    public boolean showDangerousPlayers = true;
    public boolean showFinalKD = true;
    public boolean showDoublesFinalKD = true;
    public boolean showQuadsFinalKD = true;
    public boolean showTotalWins = true;
    public double dangerousPlayersKDThreshold = 1.0;
    
    private static ModConfig instance;
    
    public static ModConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }
    
    public static ModConfig load() {
        Path configPath = getConfigPath();
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath, StandardCharsets.UTF_8);
                return GSON.fromJson(content, ModConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ModConfig();
    }
    
    public void save() {
        Path configPath = getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            String json = GSON.toJson(this);
            Files.writeString(configPath, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
    }
}
