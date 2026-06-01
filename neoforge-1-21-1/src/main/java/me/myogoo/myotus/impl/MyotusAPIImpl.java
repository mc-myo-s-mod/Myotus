package me.myogoo.myotus.impl;

import me.myogoo.myotus.api.IMyotusAPI;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.ICreativeTabRegistrar;
import me.myogoo.myotus.impl.registrar.CreativeTabRegistrarImpl;
import me.myogoo.myotus.impl.registrar.ConfigRegistrarImpl;

public enum MyotusAPIImpl implements IMyotusAPI {
    INSTANCE;

    private final IConfigRegistrar configRegistrar = new ConfigRegistrarImpl();
    private final ICreativeTabRegistrar creativeTabRegistrar = new CreativeTabRegistrarImpl();

    @Override
    public IConfigRegistrar configRegistrar() {
        return configRegistrar;
    }

    @Override
    public ICreativeTabRegistrar creativeTabRegistrar() {
        return creativeTabRegistrar;
    }

}
