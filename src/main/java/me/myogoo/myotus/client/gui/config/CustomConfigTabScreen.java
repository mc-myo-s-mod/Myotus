package me.myogoo.myotus.client.gui.config;

import appeng.client.gui.AESubScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.me.common.MEStorageScreen;
import me.myogoo.myotus.api.config.MyoConfigTabScreen;
import appeng.client.gui.widgets.TabButton;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.impl.ConfigManager;
import me.myogoo.myotus.client.TranslateKey;
import me.myogoo.myotus.client.gui.widgets.KeyBindingButton;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import me.myogoo.myotus.client.gui.widgets.button.MyoReportButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.Rect2i;

import java.util.ArrayList;
import java.util.List;

/**
 * 커스텀 설정 탭을 그리는 화면. AE2의 기본 설정 컴포넌트는 포함하지 않습니다.
 */
public class CustomConfigTabScreen<C extends MEStorageMenu>
        extends AESubScreen<C, MEStorageScreen<C>> implements MyoConfigTabScreen {

    private final List<TabButton> tabButtons = new ArrayList<>();
    private final int selectedTab;
    private final MEStorageScreen<C> parentScreen;
    private final List<MyoConfigTab> customTabs;

    public CustomConfigTabScreen(MEStorageScreen<C> parent, int tabIndex) {
        super(parent,
                String.format("/screens/config/%s", ConfigManager.INSTANCE.getTabs().get(tabIndex - 1).stylePath()));
        this.parentScreen = parent;
        this.selectedTab = tabIndex;
        this.customTabs = ConfigManager.INSTANCE.getTabs();

        this.addToLeftToolbar(new MyoReportButton());
        addBackButton();

        int customTabIndex = this.selectedTab - 1;
        if (customTabIndex >= 0 && customTabIndex < this.customTabs.size()) {
            this.customTabs.get(customTabIndex).configTabScreen().buildTab(this.widgets, this);
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
        TabButton ae2Tab = new TabButton(Icon.COG, TranslateKey.TITLE_AE2_TERMINAL_SETTING.getTranslate(),
                btn -> selectTab(0));
        ae2Tab.setStyle(TabButton.Style.HORIZONTAL);
        ae2Tab.setSelected(selectedTab == 0);
        this.addRenderableWidget(ae2Tab);
        tabButtons.add(ae2Tab);

        // 커스텀 탭들
        for (int i = 0; i < customTabs.size(); i++) {
            var tab = customTabs.get(i);
            final int tabIndex = i + 1;
            CustomTabButton tabBtn = tab.getTabButton(btn -> selectTab(tabIndex));
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

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // listening 상태인 KeyBindingButton에 키 이벤트를 먼저 전달
        for (var renderable : this.renderables) {
            if (renderable instanceof KeyBindingButton keyBinding && keyBinding.isListening()) {
                if (keyBinding.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public List<Rect2i> getExclusionZones() {
        var list = super.getExclusionZones();
        int startX = this.leftPos + this.imageWidth - 3;
        int startY = this.topPos + 2;
        list.add(new Rect2i(startX, startY, 22, 22 * tabButtons.size()));
        return list;
    }

    private void selectTab(int index) {
        if (this.selectedTab != index) {
            if (index == 0) {
                Minecraft.getInstance().setScreen(new TabbedTerminalSettingsScreen<>(this.parentScreen));
            } else {
                Minecraft.getInstance().setScreen(new CustomConfigTabScreen<>(this.parentScreen, index));
            }
        }
    }

    @Override
    public void buildTab(appeng.client.gui.WidgetContainer widgets, appeng.client.gui.AEBaseScreen<?> screen) {
        // This is the parent container for other widgets
    }

    private void addBackButton() {
        var label = menu.getHost().getMainMenuIcon().getHoverName();
        TabButton button = new TabButton(Icon.BACK, label, btn -> returnToParent());
        widgets.add("back", button);
    }
}
