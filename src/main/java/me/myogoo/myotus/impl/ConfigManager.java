package me.myogoo.myotus.impl;

import me.myogoo.myotus.api.config.MyoConfigTab;
import appeng.menu.me.common.MEStorageMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ConfigManager {
    INSTANCE;

    private final List<MyoConfigTab> tabs = new ArrayList<>();

    public void registerTab(MyoConfigTab tab) {
        tabs.add(tab);
    }

    public List<MyoConfigTab> getTabs() {
        return Collections.unmodifiableList(tabs);
    }

    public List<MyoConfigTab> getVisibleTabs(MEStorageMenu menu) {
        return tabs.stream()
                .filter(tab -> tab.isVisible(menu))
                .toList();
    }
}
