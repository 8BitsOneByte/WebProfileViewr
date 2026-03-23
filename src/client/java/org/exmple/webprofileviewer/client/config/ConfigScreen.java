package org.exmple.webprofileviewer.client.config;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private static final int SPACING = 8;
    
    private final Screen parent;
    private HeaderAndFooterLayout layout;
    
    public ConfigScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }
    
    @Override
    protected void init() {
        this.layout = new HeaderAndFooterLayout(this, 50, 100);
        this.layout.addToHeader(new StringWidget(this.getTitle(), this.font));
        GridLayout gridWidget = this.layout.addToContents(new GridLayout()).spacing(SPACING);
        gridWidget.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        adder.addChild(Button.builder(Component.translatable("text.webprofileviewer.general_settings"), button -> {
            this.minecraft.setScreen(new GeneralSettingsScreen(this));
        }).build());
        
        adder.addChild(Button.builder(Component.translatable("text.webprofileviewer.ui_settings"), button -> {
            this.minecraft.setScreen(new UiSettingsScreen(this));
        }).build());
        
        adder.addChild(Button.builder(Component.translatable("text.webprofileviewer.placeholder_2"), button -> {
            // TODO: 打开设置页面3
        }).build());
        
        adder.addChild(Button.builder(Component.translatable("text.webprofileviewer.placeholder_3"), button -> {
            // TODO: 打开设置页面4
        }).build());
        adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build(), 2);
        GridLayout footerGridWidget = this.layout.addToFooter(new GridLayout()).spacing(SPACING).rowSpacing(0);
        footerGridWidget.defaultCellSetting().alignHorizontallyCenter();
        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }
    
    @Override
    public void onClose() {
        // 返回到父屏幕（上一级菜单）
        this.minecraft.setScreen(this.parent);
    }
}
