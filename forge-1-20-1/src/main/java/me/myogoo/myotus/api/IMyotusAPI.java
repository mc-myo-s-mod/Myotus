package me.myogoo.myotus.api;

import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.api.network.IMyotusNetwork;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.ICreativeTabRegistrar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

/**
 * Main service interface exposed by Myotus.
 *
 * <p>This interface groups the three primary extension areas:</p>
 * <ul>
 *     <li>Registering terminal configuration tabs</li>
 *     <li>Registering custom packets on the shared Myotus channel</li>
 *     <li>Registering items in the shared Myotus creative tab</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyotusAPI.configTabs().registerTerminalConfigTab(new MyoConfigTab(
 *                 ResourceLocation.fromNamespaceAndPath("examplemod", "terminal_settings"),
 *                 Component.literal("Example"),
 *                 Icon.WRENCH,
 *                 "example.json",
 *                 new ExampleConfigScreen()));
 * }</pre>
 */
public interface IMyotusAPI {

    /**
     * Returns the registrar for terminal configuration tabs.
     *
     * @return the config registrar
     */
    IConfigRegistrar configRegistrar();

    /**
     * Returns the registrar for contributing entries to the shared Myotus creative tab.
     *
     * @return the creative tab registrar
     */
    ICreativeTabRegistrar creativeTabRegistrar();

    /**
     * Canonical alias for {@link #configRegistrar()}.
     *
     * @return the config tab registrar
     */
    default IConfigRegistrar configTabs() {
        return configRegistrar();
    }

    /**
     * Canonical alias for {@link #creativeTabRegistrar()}.
     *
     * @return the creative tab registrar
     */
    default ICreativeTabRegistrar creativeTabs() {
        return creativeTabRegistrar();
    }

    /**
     * Returns the public facade for Myotus-managed optional integrations.
     *
     * @return the integration facade
     */
    default MyotusAPI.IntegrationFacade integrations() {
        return MyotusAPI.integrations();
    }

    /**
     * Returns the public facade for Applied Experienced / {@code fluid:xp} calculations.
     *
     * @return the experience calculation facade
     */
    default MyotusAPI.ExperienceFacade experience() {
        return MyotusAPI.experience();
    }

    /**
     * Returns the shared networking facade used to register and send Myotus packets.
     *
     * @return the networking facade
     */
    IMyotusNetwork network();



    /**
     * Registers a terminal configuration tab.
     *
     * @param tab tab definition to add
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerConfigTab(MyoConfigTab tab) {
        configTabs().registerTerminalConfigTab(tab);
        return this;
    }

    /**
     * Registers an item supplier in the shared Myotus creative tab.
     *
     * @param item item supplier to add
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerCreativeTabItem(Supplier<? extends ItemLike> item) {
        creativeTabs().registerCreativeTabItem(item);
        return this;
    }

    /**
     * Registers a stack supplier in the shared Myotus creative tab.
     *
     * @param stack stack supplier to add
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerCreativeTabStack(Supplier<ItemStack> stack) {
        creativeTabs().registerCreativeTabStack(stack);
        return this;
    }

    /**
     * Returns the shared networking facade.
     *
     * @return the networking facade
     */
    default IMyotusNetwork getNetwork() {
        return network();
    }

}
