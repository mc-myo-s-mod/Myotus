package me.myogoo.myotus.impl;

import me.myogoo.myotus.api.IMyotusAPI;
import me.myogoo.myotus.api.network.IMyotusNetwork;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.ICreativeTabRegistrar;
import me.myogoo.myotus.impl.network.MyotusNetwork;
import me.myogoo.myotus.impl.registrar.ConfigRegistrarImpl;
import me.myogoo.myotus.impl.registrar.CreativeTabRegistrarImpl;

public enum MyotusAPIImpl implements IMyotusAPI {
    INSTANCE;

    private final IConfigRegistrar configRegistrar = new ConfigRegistrarImpl();
    private final ICreativeTabRegistrar creativeTabRegistrar = new CreativeTabRegistrarImpl();
    private final IMyotusNetwork network = MyotusNetwork.INSTANCE;

    @Override
    public IConfigRegistrar configRegistrar() {
        return configRegistrar;
    }

    @Override
    public ICreativeTabRegistrar creativeTabRegistrar() {
        return creativeTabRegistrar;
    }

    @Override
    public IMyotusNetwork network() {
        return network;
    }

}
