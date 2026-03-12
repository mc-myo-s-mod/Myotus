package me.myogoo.myotus.client.gui.config;

import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.menu.me.common.MEStorageMenu;
import appeng.client.gui.widgets.TabButton;

import me.myogoo.myotus.api.config.AE2TerminalConfigTab;
import me.myogoo.myotus.api.config.TerminalConfigTab;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import me.myogoo.myotus.client.gui.widgets.button.MyoReportButton;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TabbedTerminalSettingsScreen<C extends MEStorageMenu> extends TerminalSettingsScreen<C> {

    private final List<TabButton> tabButtons = new ArrayList<>();
    private final MEStorageScreen<C> parentScreen;
    private final List<TerminalConfigTab> customTabs;

    public TabbedTerminalSettingsScreen(MEStorageScreen<C> parent) {
        super(parent);
        this.parentScreen = parent;
        this.customTabs = AE2TerminalConfigTab.getTabs();
        this.addToLeftToolbar(new MyoReportButton());
    }

    @Override
    protected void init() {
        super.init();

        tabButtons.clear();
        buildTabBar();
    }

    private void buildTabBar() {
        TabButton ae2Tab = new TabButton(Icon.COG, Component.translatable("gui.myotus.button.ae2setting"), btn -> selectTab(0));
        ae2Tab.setStyle(TabButton.Style.HORIZONTAL);
        ae2Tab.setSelected(true);
        this.addRenderableWidget(ae2Tab);
        tabButtons.add(ae2Tab);

        for (int i = 0; i < customTabs.size(); i++) {
            var tab = customTabs.get(i);
            final int tabIndex = i + 1;
            CustomTabButton tabBtn = tab.getTabButton(button -> selectTab(tabIndex));
            tabBtn.setStyle(TabButton.Style.HORIZONTAL);
            tabBtn.setSelected(false);
            this.addRenderableWidget(tabBtn);
            tabButtons.add(tabBtn);
        }

        positionTabs();
    }

    private void positionTabs() {
        int startX = this.leftPos + this.imageWidth - 3;
        int currentY = this.topPos + 2;
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButton tab = tabButtons.get(i);
            tab.setPosition(startX, currentY);
            currentY += tab.getHeight();
        }
    }

    private void selectTab(int index) {
        if (index != 0) {
            Minecraft.getInstance().setScreen(new CustomConfigTabScreen<>(this.parentScreen, index));
        }
    }

}
