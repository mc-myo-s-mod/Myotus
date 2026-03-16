package me.myogoo.myotus.client.gui.widgets;

import me.myogoo.myotus.client.SidePanelSubScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;

import me.myogoo.myotus.config.MyotusClientConfig;

/**
 * TerminalSidePanel의 가시성을 토글하는 버튼.
 * config 값을 업데이트하고 TerminalSidePanel의 visible 상태를 동기화합니다.
 */
public class SidePanelToggleButton extends IconButton {

    private final SidePanelSubScreen sidePanel;

    public SidePanelToggleButton(SidePanelSubScreen sidePanel) {
        super(btn -> {
            if (btn instanceof SidePanelToggleButton t) {
                t.run(btn);
            }
        });
        this.sidePanel = sidePanel;
        updateTooltip();
    }

    private void updateTooltip() {
        boolean isOpen = MyotusClientConfig.INSTANCE.openSidePanel.get();
        setMessage(Component.literal("Side Panel: " + (isOpen ? "ON" : "OFF")));
    }

    @Override
    protected Icon getIcon() {
        boolean isOpen = sidePanel.isVisible();
        return isOpen ? Icon.ARROW_LEFT : Icon.ARROW_RIGHT;
    }

    private void run(Button button) {
        boolean newValue = !MyotusClientConfig.INSTANCE.openSidePanel.get();
        MyotusClientConfig.INSTANCE.openSidePanel.set(newValue);
        this.sidePanel.setVisible(newValue);
        this.updateTooltip();
    }
}
