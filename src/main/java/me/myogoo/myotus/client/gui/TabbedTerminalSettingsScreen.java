package me.myogoo.myotus.client.gui;

import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.me.common.TerminalSettingsScreen;
import appeng.menu.me.common.MEStorageMenu;
import appeng.client.gui.widgets.TabButton;

import me.myogoo.myotus.api.ConfigTab;
import me.myogoo.myotus.client.gui.widgets.button.MyoReportButton;
import me.myogoo.myotus.integration.ae2.TerminalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AE2의 TerminalSettingsScreen을 확장한 탭 기반 설정 화면.
 * 첫 번째 탭은 AE2 기본 설정(기존 상속 구조) 그대로 유지,
 * 이후 탭은 TerminalConfig에 등록된 CustomTabSettingsScreen으로 전환됩니다.
 */
public class TabbedTerminalSettingsScreen<C extends MEStorageMenu> extends TerminalSettingsScreen<C> {

    private final List<TabButton> tabButtons = new ArrayList<>();
    private final MEStorageScreen<C> parentScreen;
    private final List<ConfigTab> customTabs;

    public TabbedTerminalSettingsScreen(MEStorageScreen<C> parent) {
        super(parent);
        this.parentScreen = parent;
        this.customTabs = TerminalConfig.getTabs();
        this.addToLeftToolbar(new MyoReportButton());
    }

    @Override
    protected void init() {
        super.init();

        tabButtons.clear();
        buildTabBar();
    }

    private void buildTabBar() {
        // AE2 기본 탭
        TabButton ae2Tab = new TabButton(Icon.COG, Component.literal("AE2 Settings"), btn -> selectTab(0));
        ae2Tab.setStyle(TabButton.Style.HORIZONTAL);
        ae2Tab.setSelected(true); // 항상 0번째 탭 상태
        this.addRenderableWidget(ae2Tab);
        tabButtons.add(ae2Tab);

        // 커스텀 탭들
        for (int i = 0; i < customTabs.size(); i++) {
            ConfigTab tab = customTabs.get(i);
            final int tabIndex = i + 1;
            TabButton tabBtn;
            if (tab.iconStack() != null) {
                tabBtn = new TabButton(tab.iconStack(), tab.title(), btn -> selectTab(tabIndex));
            } else {
                tabBtn = new TabButton(tab.icon() != null ? tab.icon() : Icon.COG, tab.title(),
                        btn -> selectTab(tabIndex));
            }
            tabBtn.setStyle(TabButton.Style.HORIZONTAL);
            tabBtn.setSelected(false);
            this.addRenderableWidget(tabBtn);
            tabButtons.add(tabBtn);
        }

        positionTabs();
    }

    private void positionTabs() {
        // 탭 버튼들을 화면 오른쪽 위부터 수직으로 배치 (AE2 외곽선과 자연스럽게 연결하기 위해 X를 -4 쉬프트)
        int startX = this.leftPos + this.imageWidth - 3;
        int currentY = this.topPos + 2;
        for (int i = 0; i < tabButtons.size(); i++) {
            TabButton tab = tabButtons.get(i);
            tab.setPosition(startX, currentY);
            // 간격 없이 탭을 쌓기 위해 탭의 높이만큼만 더함 (보통 22 또는 28 등)
            currentY += tab.getHeight();
        }
    }

    private void selectTab(int index) {
        if (index != 0) {
            Minecraft.getInstance().setScreen(new CustomTabSettingsScreen<>(this.parentScreen, index));
        }
    }

}
