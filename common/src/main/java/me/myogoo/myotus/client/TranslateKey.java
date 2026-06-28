package me.myogoo.myotus.client;

public enum TranslateKey implements MyoTranslateKey {
    ITEM_GROUP_MYOTUS("itemGroup.myotus");

    private final String key;

    TranslateKey(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }

    public enum GUI implements MyoTranslateKey {
        TOOLTIP_REPORT_BUG("gui.myotus.button.bug_report.tooltip"),
        TITLE_AE2_TERMINAL_SETTING("gui.myotus.button.ae2setting"),
        TITLE_TERMINAL_SETTING("gui.myotus.config.title"),
        CONFIG_TAB_SORTING("gui.myotus.checkbox.tab.sorting"),
        SHOW_UPGRADE_PANEL_BUTTON("gui.myotus.checkbox.show_upgrade_panel_button"),
        SHOW_VIEW_CELL_SLOT("gui.myotus.checkbox.show_view_cell_slots"),
        SHOW_VIEW_CELL_SLOT_ONLY("gui.myotus.checkbox.show_view_cell_slots.only"),
        KEYBINDING_LISTENING("gui.myotus.keybinding.listening"),
        TOGGLE_UPGRADE_TERMINAL_PANEL("gui.myotus.config.toggle_upgrade_terminal_panel"),
        UPGRADE_TERMINAL_PANEL_SHOW("gui.myotus.config.upgrade_terminal_panel.show"),
        UPGRADE_TERMINAL_PANEL_HIDE("gui.myotus.config.upgrade_terminal_panel.hide"),
        UPGRADE_SLOT_EMPTY_TOOLTIP_HEADER("gui.myotus.upgrade_slot.empty_tooltip.header"),
        UPGRADE_SLOT_EMPTY_TOOLTIP_NONE("gui.myotus.upgrade_slot.empty_tooltip.none");

        private final String key;

        GUI(String key) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }

    public enum KEY implements MyoTranslateKey {
        OPEN_TERMINAL_SETTING("key.myotus.open.terminal_setting"),
        TOGGLE_UPGRADE_TERMINAL_PANEL("key.myotus.toggle.upgrade_terminal_panel"),
        CATEGORY_MYOTUS("key.categories.myotus");

        private final String key;

        KEY(String key) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }

    public enum ITEM implements MyoTranslateKey {
        COMPAT_PROCESSOR("item.myotus.compat_processor"),
        PRINTED_COMPAT_PROCESSOR("item.myotus.printed_compat_processor"),
        COMPAT_PRESS("item.myotus.compat_press"),
        CHARGED_ENDER_PEARL("item.myotus.charged_ender_pearl"),
        MYOTUS_UPGRADE_CARD("item.myotus.myotus_upgrade_card");

        private final String key;

        ITEM(String key) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }

    public enum BLOCK implements MyoTranslateKey {
        ENDER_PEARL_BLOCK("block.myotus.ender_pearl_block"),
        CHARGED_ENDER_PEARL_BLOCK("block.myotus.charged_ender_pearl_block");

        private final String key;

        BLOCK(String key) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }

    public enum ERROR implements MyoTranslateKey {
        VERSION_MISMATCH("error.myotus.mod.loading.version.mismatch");

        private final String key;

        ERROR(String key) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }
}
