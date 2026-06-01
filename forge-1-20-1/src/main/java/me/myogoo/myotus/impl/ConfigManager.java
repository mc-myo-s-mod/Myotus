package me.myogoo.myotus.impl;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.config.MyoConfigTab;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum ConfigManager {
    INSTANCE;

    private final Map<ResourceLocation, MyoConfigTab> tabs = new LinkedHashMap<>();

    public void registerTab(MyoConfigTab tab) {
        var previous = tabs.putIfAbsent(tab.id(), tab);
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate Myotus config tab id: " + tab.id());
        }
    }

    public List<MyoConfigTab> getTabs() {
        return Collections.unmodifiableList(new ArrayList<>(tabs.values()));
    }

    public List<MyoConfigTab> getVisibleTabs(MEStorageMenu menu) {
        return tabs.values().stream()
                .filter(tab -> tab.isVisible(menu))
                .toList();
    }
}
