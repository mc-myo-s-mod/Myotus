package me.myogoo.myotus.impl;

import me.myogoo.myotus.api.IMyotusAPI;
import me.myogoo.myotus.api.integration.IModIntegrationManager;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.IModRegistrar;
import me.myogoo.myotus.impl.integration.ModIntegrationManagerImpl;
import me.myogoo.myotus.impl.registrar.ConfigRegistrarImpl;
import me.myogoo.myotus.impl.registrar.ModRegistrarImpl;

public enum MyotusAPIImpl implements IMyotusAPI {
    INSTANCE;

    private final IModRegistrar modRegistrar = new ModRegistrarImpl();
    private final IConfigRegistrar configRegistrar = new ConfigRegistrarImpl();
    private final IModIntegrationManager modIntegrationManager = new ModIntegrationManagerImpl();

    @Override
    public IModRegistrar modRegistrar() {
        return modRegistrar;
    }

    @Override
    public IConfigRegistrar configRegistrar() {
        return configRegistrar;
    }

    @Override
    public IModIntegrationManager modIntegrationManager() {
        return modIntegrationManager;
    }
}
