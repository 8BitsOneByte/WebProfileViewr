package org.exmple.webprofileviewer.client.config;

import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class UiSettingsScreen extends Screen {
    private static final int SPACING = 8;

    private final Screen parent;
    private final ModConfig config;
    private HeaderAndFooterLayout layout;

    public UiSettingsScreen(Screen parent) {
        super(Component.translatable("text.webprofileviewer.ui_settings"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        this.layout = new HeaderAndFooterLayout(this, 50, 100);
        this.layout.addToHeader(new StringWidget(this.getTitle(), this.font));

        GridLayout gridWidget = this.layout.addToContents(new GridLayout()).spacing(SPACING);
        gridWidget.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);

        CycleButton<Boolean> antiAfkIconSizeButton = CycleButton.<Boolean>builder(
            value -> value
                ? Component.translatable("text.webprofileviewer.anti_afk_icon_small")
                : Component.translatable("text.webprofileviewer.anti_afk_icon_large"),
            () -> config.antiAfkIconSmall
        )
        .withValues(Boolean.TRUE, Boolean.FALSE)
        .displayOnlyValue()
        .create(
            Component.literal(""),
            (button, value) -> {
                config.antiAfkIconSmall = value;
                config.save();
            }
        );
        adder.addChild(antiAfkIconSizeButton);

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
