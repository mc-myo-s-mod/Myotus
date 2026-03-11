package me.myogoo.myotus.client.gui.config;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.config.ConfigTab;
import me.myogoo.myotus.client.gui.widgets.button.MyoReportButton;
import me.myogoo.myotus.integration.ae2.TerminalConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 커스텀 설정 탭을 그리는 화면. AE2의 기본 설정 컴포넌트는 포함하지 않습니다.
 */
public class CustomTabSettingsScreen<C extends MEStorageMenu>
        extends AESubScreen<C, MEStorageScreen<C>> {

    private final List<TabButton> tabButtons = new ArrayList<>();
    private final int selectedTab;
    private final MEStorageScreen<C> parentScreen;
    private final List<ConfigTab> customTabs;

    public CustomTabSettingsScreen(MEStorageScreen<C> parent, int tabIndex, String stylePath) {
        //super(parent, "/screens/terminals/terminal_settings.json");
        super(parent, String.format("/screens/config/%s", stylePath));
        this.parentScreen = parent;
        this.selectedTab = tabIndex;
        this.customTabs = TerminalConfig.getTabs();

        this.addToLeftToolbar(new MyoReportButton());
        addBackButton();

        int customTabIndex = this.selectedTab - 1;
        if (customTabIndex >= 0 && customTabIndex < this.customTabs.size()) {
            this.customTabs.get(customTabIndex).provider().buildTab(this.widgets, this);
        }
    }

    @Override
    protected void init() {
        super.init();
        setSlotsHidden(SlotSemantics.TOOLBOX, true);

        // 부모 JSON 화면에 정의된 글자(TextWidget)들이 Custom 화면에 그려지는 것을 막기 위해 가시성을 끕니다.
        for (var child : this.renderables) {
            String className = child.getClass().getSimpleName();
            if (className.contains("Text") || className.contains("Label")) {
                if (child instanceof AbstractWidget widget) {
                    widget.visible = false;
                }
            }
        }

        // JSON에 명시된 이름(dialog_title, search_settings_title)으로 위젯을 찾아서 명시적으로 가리기
        this.setTextHidden("dialog_title", true);
        this.setTextHidden("search_settings_title", true);

        tabButtons.clear();
        buildTabBar();
    }

    private void buildTabBar() {
        TabButton ae2Tab = new TabButton(Icon.COG, Component.translatable("gui.myotus.button.ae2setting"), btn -> selectTab(0));
        ae2Tab.setStyle(TabButton.Style.HORIZONTAL);
        ae2Tab.setSelected(selectedTab == 0);
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
            tabBtn.setSelected(selectedTab == tabIndex);
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

        if (this.selectedTab != index) {
            if (index == 0) {
                Minecraft.getInstance().setScreen(new TabbedTerminalSettingsScreen<>(this.parentScreen));
            } else {
                Minecraft.getInstance().setScreen(new CustomTabSettingsScreen<>(this.parentScreen, index, TerminalConfig.getTabStyle(index - 1)));
            }
        }
    }

    private void addBackButton() {
        var label = menu.getHost().getMainMenuIcon().getHoverName();
        TabButton button = new TabButton(Icon.BACK, label, btn -> returnToParent());
        widgets.add("back", button);
    }
}
