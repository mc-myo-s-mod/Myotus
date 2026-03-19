package me.myogoo.myotus.api;

import me.myogoo.myotus.api.integration.IModIntegrationManager;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.IModRegistrar;

/**
 * Main API interface for Myotus.
 */
public interface IMyotusAPI {

    IModRegistrar modRegistrar();

    IConfigRegistrar configRegistrar();

    IModIntegrationManager modIntegrationManager();
}
