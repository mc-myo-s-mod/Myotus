package me.myogoo.myotus.client.gui.config;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.widgets.AECheckbox;
import me.myogoo.myotus.config.MyotusClientConfig;
import net.minecraft.network.chat.Component;

public class MyotusConfigScreen extends BaseConfigTabScreen {
    private AECheckbox configTabSorting;

    @Override
    public void buildTab(WidgetContainer widgets, AEBaseScreen<?> screen) {
        configTabSorting = widgets.addCheckbox("sort", Component.translatable("gui.myotus.checkbox.tab_sorting"),
                this::updateConfigTabSorting);


        updateState();
    }

    @Override
    public void updateState() {
        configTabSorting.setSelected(MyotusClientConfig.INSTANCE.activeTabSorting.get());
    }

    @Override
    public void save() {
        MyotusClientConfig.INSTANCE.spec.save();
        updateState();
    }

    private void updateConfigTabSorting() {
        MyotusClientConfig.INSTANCE.activeTabSorting.set(!MyotusClientConfig.INSTANCE.activeTabSorting.get());
        save();
    }
}