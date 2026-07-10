package me.myogoo.myotus.client.gui.widgets;

import me.myogoo.myotus.client.TranslateKey;
import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.client.TerminalUpgradePanel;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.widgets.button.CustomImageButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.IconButton;

import me.myogoo.myotus.config.MyotusConfig;

/**
 * TerminalUpgradePanel의 가시성을 토글하는 버튼.
 * config 값을 업데이트하고 TerminalUpgradePanel의 visible 상태를 동기화합니다.
 */
public class SidePanelToggleButton extends CustomImageButton {

    private final TerminalUpgradePanel sidePanel;

    public SidePanelToggleButton(TerminalUpgradePanel sidePanel) {
        super(btn -> {
            if (btn instanceof SidePanelToggleButton toggleButton) {
                toggleButton.run();
            }
        });
        this.sidePanel = sidePanel;
        updateTooltip();
    }

    private void updateTooltip() {
        boolean isOpen = MyotusConfig.CLIENT.openSidePanel.get();
        setMessage(Component.translatable(isOpen ? TranslateKey.GUI.UPGRADE_TERMINAL_PANEL_HIDE.key() :
                TranslateKey.GUI.UPGRADE_TERMINAL_PANEL_SHOW.key()));
    }

    @Override
    protected Blitter getIcon() {
        boolean isOpen = sidePanel.isVisible();
        return isOpen ?  MyoIcon.SHOW_UPGRADE_PANEL.getBlitter() : MyoIcon.HIDE_UPGRADE_PANEL.getBlitter();
    }

    public void run() {
        boolean newValue = !MyotusConfig.CLIENT.openSidePanel.get();
        MyotusConfig.CLIENT.openSidePanel.set(newValue);
        MyotusConfig.CLIENT.save();
        this.sidePanel.setVisible(newValue);
        this.updateTooltip();
    }
}
