package me.myogoo.myotus.client.gui.widgets;

import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.client.SidePanelSubScreen;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.widgets.button.CustomImageButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;

import me.myogoo.myotus.config.MyotusClientConfig;

/**
 * TerminalSidePanel의 가시성을 토글하는 버튼.
 * config 값을 업데이트하고 TerminalSidePanel의 visible 상태를 동기화합니다.
 */
public class SidePanelToggleButton extends CustomImageButton {

    private final SidePanelSubScreen sidePanel;

    public SidePanelToggleButton(SidePanelSubScreen sidePanel) {
        super(btn -> {
            if (btn instanceof SidePanelToggleButton toggleButton) {
                toggleButton.run();
            }
        });
        this.sidePanel = sidePanel;
        updateTooltip();
    }

    private void updateTooltip() {
        boolean isOpen = MyotusClientConfig.CLIENT.openSidePanel.get();
        setMessage(Component.translatable(isOpen ? "gui.myotus.config.toggle_side_panel.hide" :
                "gui.myotus.config.toggle_side_panel.show"));
    }

    @Override
    protected Blitter getIcon() {
        boolean isOpen = sidePanel.isVisible();
        return isOpen ? MyoIcon.HIDE_UPGRADE_PANEL.getBlitter() : MyoIcon.SHOW_UPGRADE_PANEL.getBlitter();
    }

    public void run() {
        boolean newValue = !MyotusClientConfig.CLIENT.openSidePanel.get();
        MyotusClientConfig.CLIENT.openSidePanel.set(newValue);
        this.sidePanel.setVisible(newValue);
        this.updateTooltip();
    }
}
