package org.exmple.webprofileviewer.client.config;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.exmple.webprofileviewer.client.AntiAFKManager;

public class GeneralSettingsScreen extends Screen {
    private static final int SPACING = 8;
    
    private final Screen parent;
    private final ModConfig config;
    private HeaderAndFooterLayout layout;
    
    public GeneralSettingsScreen(Screen parent) {
        super(Component.translatable("text.webprofileviewer.general_settings"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }
    
    @Override
    protected void init() {
        AntiAFKManager.setEnabled(this.config.antiAFK);
        this.layout = new HeaderAndFooterLayout(this, 50, 100);
        // 添加顶部标题
        this.layout.addToHeader(new StringWidget(this.getTitle(), this.font));
        
        GridLayout gridWidget = this.layout.addToContents(new GridLayout()).spacing(SPACING);
        gridWidget.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        
        // AntiAFK 循环按钮
        CycleButton<Boolean> antiAFKButton = CycleButton.<Boolean>builder(
            value -> value 
                ? Component.translatable("text.webprofileviewer.anti_afk_on")
                : Component.translatable("text.webprofileviewer.anti_afk_off"),
            () -> config.antiAFK
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.antiAFK = value;
                AntiAFKManager.setEnabled(value);
                config.save();
            }
        );
        adder.addChild(antiAFKButton);
        
        // Show Dangerous Players 循环按钮
        CycleButton<Boolean> showDangerousPlayersButton = CycleButton.<Boolean>builder(
            value -> value 
                ? Component.translatable("text.webprofileviewer.show_dangerous_players_on")
                : Component.translatable("text.webprofileviewer.show_dangerous_players_off"),
            () -> config.showDangerousPlayers
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.showDangerousPlayers = value;
                config.save();
            }
        );
        adder.addChild(showDangerousPlayersButton);
        
        // Show Final KD 循环按钮
        CycleButton<Boolean> showFinalKDButton = CycleButton.<Boolean>builder(
            value -> value 
                ? Component.translatable("text.webprofileviewer.show_final_kd_on")
                : Component.translatable("text.webprofileviewer.show_final_kd_off"),
            () -> config.showFinalKD
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.showFinalKD = value;
                config.save();
            }
        );
        adder.addChild(showFinalKDButton);
        
        // Show Doubles Final KD 循环按钮
        CycleButton<Boolean> showDoublesFinalKDButton = CycleButton.<Boolean>builder(
            value -> value 
                ? Component.translatable("text.webprofileviewer.show_doubles_final_kd_on")
                : Component.translatable("text.webprofileviewer.show_doubles_final_kd_off"),
            () -> config.showDoublesFinalKD
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.showDoublesFinalKD = value;
                config.save();
            }
        );
        adder.addChild(showDoublesFinalKDButton);
        
        // Show Quads Final KD 循环按钮
        CycleButton<Boolean> showQuadsFinalKDButton = CycleButton.<Boolean>builder(
            value -> value 
                ? Component.translatable("text.webprofileviewer.show_quads_final_kd_on")
                : Component.translatable("text.webprofileviewer.show_quads_final_kd_off"),
            () -> config.showQuadsFinalKD
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.showQuadsFinalKD = value;
                config.save();
            }
        );
        adder.addChild(showQuadsFinalKDButton);
        
        // Show Total Wins 循环按钮
        CycleButton<Boolean> showTotalWinsButton = CycleButton.<Boolean>builder(
            value -> value 
                ? Component.translatable("text.webprofileviewer.show_total_wins_on")
                : Component.translatable("text.webprofileviewer.show_total_wins_off"),
            () -> config.showTotalWins
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.showTotalWins = value;
                config.save();
            }
        );
        adder.addChild(showTotalWinsButton);
        adder.addChild(
            net.minecraft.client.gui.components.Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build(),
            2
        );
        GridLayout footerGridWidget = this.layout.addToFooter(new GridLayout()).spacing(SPACING).rowSpacing(0);
        footerGridWidget.defaultCellSetting().alignHorizontallyCenter();
        
        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }
    
    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}

