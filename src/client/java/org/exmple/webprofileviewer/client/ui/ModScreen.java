package org.exmple.webprofileviewer.client.ui;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.exmple.webprofileviewer.client.GlobalConstants;
import org.exmple.webprofileviewer.client.annotations.Init;
import org.exmple.webprofileviewer.client.config.ModConfigManager;
import org.exmple.webprofileviewer.client.utils.Scheduler;

public class ModScreen extends Screen {
    private static final int SPACING = 8;
    private static final int BUTTON_WIDTH = 210;
    private static final int HALF_BUTTON_WIDTH = 101; //Same as (210 - 8) / 2
    private static final Component TITLE= Component.literal("WebProfileViewer Mod " + GlobalConstants.VERSION);;
    private static final Component CONFIGURATION_TEXT = Component.translatable("text.webprofileviewer.config");
    private static final Component SOURCE_TEXT = Component.translatable("text.webprofileviewer.source");
    private static final Component REPORT_BUGS_TEXT = Component.translatable("menu.reportBugs");
    private static final Component WEBSITE_TEXT = Component.translatable("text.webprofileviewer.website");
    private static final Component MODRINTH_TEXT = Component.translatable("text.webprofileviewer.modrinth");
    private HeaderAndFooterLayout layout;
protected ModScreen(){
    super(TITLE);
}
    @Init
    public static void initClass() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal(GlobalConstants.NAMESPACE)
                    .executes(Scheduler.queueOpenScreenCommand(ModScreen::new)));
        });
    }

    @Override
    protected void init() {
        this.layout = new HeaderAndFooterLayout(this, 50, 100);
        this.layout.addToHeader(new StringWidget(this.getTitle(), this.font));
        GridLayout gridWidget = this.layout.addToContents(new GridLayout()).spacing(SPACING);
        gridWidget.defaultCellSetting().alignHorizontallyCenter();
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        adder.addChild(Button.builder(CONFIGURATION_TEXT, button -> this.openConfig()).width(BUTTON_WIDTH).build(), 2);
        adder.addChild(Button.builder(SOURCE_TEXT, ConfirmLinkScreen.confirmLink(this, "https://github.com/SkyblockerMod/Skyblocker")).width(HALF_BUTTON_WIDTH).build());
        adder.addChild(Button.builder(REPORT_BUGS_TEXT, ConfirmLinkScreen.confirmLink(this, "https://github.com/SkyblockerMod/Skyblocker/issues")).width(HALF_BUTTON_WIDTH).build());
        adder.addChild(Button.builder(WEBSITE_TEXT, ConfirmLinkScreen.confirmLink(this, "https://hysky.de/")).width(HALF_BUTTON_WIDTH).build());
        adder.addChild(Button.builder(MODRINTH_TEXT, ConfirmLinkScreen.confirmLink(this, "https://modrinth.com/mod/skyblocker-liap")).width(HALF_BUTTON_WIDTH).build());
        adder.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(BUTTON_WIDTH).build(), 2);
        GridLayout footerGridWidget = this.layout.addToFooter(new GridLayout()).spacing(SPACING).rowSpacing(0);
        footerGridWidget.defaultCellSetting().alignHorizontallyCenter();
        this.layout.arrangeElements();
        this.layout.visitWidgets(this::addRenderableWidget);
    }
    private void openConfig() {
        this.minecraft.setScreen(ModConfigManager.createGUI(this));
    }


}
