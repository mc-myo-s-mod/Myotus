package me.myogoo.myotus.impl.registrar;

import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.impl.ConfigManager;

public class ConfigRegistrarImpl implements IConfigRegistrar {
    @Override
    public void terminalConfigTab(MyoConfigTab tab) {
        ConfigManager.INSTANCE.registerTab(tab);
    }
}
