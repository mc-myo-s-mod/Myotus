package me.myogoo.myotus.client;

import net.minecraft.network.chat.Component;

public enum TranslateKey {
    TITLE_AE2_TERMINAL_SETTING("gui.myotus.button.ae2setting"),
    TITLE_TERMINAL_SETTING("gui.myotus.config.title"),
    OPEN_TERMINAL_SETTING("gui.myotus.open_terminal_setting"),
    OPEN_TERMINAL_SETTING_KEY("key.myotus.open.terminal_setting"),
    TOGGLE_SUB_SIDE_PANEL_KEY("key.myotus.toggle.subsidepanel"),
    KEY_CATEGORY("key.categories.myotus"),

    CONFIG_TAB_SORTING("gui.myotus.checkbox.tab.sorting"),
    SHOW_UPGRADE_PANEL_BUTTON("gui.myotus.checkbox.show_upgrade_panel_button"),
    SHOW_VIEW_CELL_SLOT("gui.myotus.checkbox.show_view_cell_slots"),

    TOOLTIP_REPORT_BUG("gui.myotus.button.bug_report.tooltip"),

    ERROR_VERSION_MISMATCH("error.myotus.mod.loading.version.mismatch");

    private final String key;

    TranslateKey(String key){
        this.key = key;
    }

    public String key() {
        return key;
    }

    public Component getTranslate() {
        return Component.translatable(key);
    }
}
