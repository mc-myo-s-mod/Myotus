package me.myogoo.myotus.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MyotusClientConfig {
    public static final MyotusClientConfig INSTANCE = new MyotusClientConfig();
    public final ModConfigSpec spec;
    public final ModConfigSpec.BooleanValue activeTabSorting;
    public final ModConfigSpec.BooleanValue openSidePanel;

    public MyotusClientConfig() {
        var builder = new ModConfigSpec.Builder();
        builder.push("tab");
        this.activeTabSorting = builder.comment("Whether to sort the active tab first in the inventory screen.")
                .define("activeTabSorting", true);
        this.openSidePanel = builder
                .comment("Whether to open the side panel when the terminal is opened.")
                .define("openSidePanel", false);
        builder.pop();

        this.spec = builder.build();
    }
}
