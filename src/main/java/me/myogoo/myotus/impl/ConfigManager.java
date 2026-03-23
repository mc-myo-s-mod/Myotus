package me.myogoo.myotus.impl;

import me.myogoo.myotus.api.config.MyoConfigTab;

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
}
