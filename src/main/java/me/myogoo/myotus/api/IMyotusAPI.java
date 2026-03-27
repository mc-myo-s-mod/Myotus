package me.myogoo.myotus.api;

import me.myogoo.myotus.api.config.MyoConfigTab;
import me.myogoo.myotus.api.integration.IModIntegrationManager;
import me.myogoo.myotus.api.registrar.IConfigRegistrar;
import me.myogoo.myotus.api.registrar.ICreativeTabRegistrar;
import me.myogoo.myotus.api.registrar.IModRegistrar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;

/**
 * Main service interface exposed by Myotus.
 *
 * <p>This interface groups the three primary extension areas:</p>
 * <ul>
 *     <li>Registering optional integrations</li>
 *     <li>Registering terminal configuration tabs</li>
 *     <li>Registering items in the shared Myotus creative tab</li>
 *     <li>Querying runtime integration state</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyotusAPI.get()
 *         .registerLoadableMod(MyMarker.class, "examplemod", "[1.2.0,)")
 *         .registerConfigTab(new MyoConfigTab(
 *                 Component.literal("Example"),
 *                 Icon.COG,
 *                 "example.json",
 *                 new ExampleConfigScreen()));
 * }</pre>
 */
public interface IMyotusAPI {

    /**
     * Returns the registrar for declaring optional mod integrations.
     *
     * @return the mod registrar
     */
    IModRegistrar modRegistrar();

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
     * Returns the runtime manager used to inspect integration state.
     *
     * @return the integration manager
     */
    IModIntegrationManager modIntegrationManager();

    /**
     * Registers an optional integration using the mod ID as both the lookup key
     * and display name.
     *
     * @param annotationClass marker annotation used to scan extension classes
     * @param modId target mod ID
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerLoadableMod(Class<? extends Annotation> annotationClass, String modId) {
        modRegistrar().loadableMod(annotationClass, modId);
        return this;
    }

    /**
     * Registers an optional integration with a required version range.
     *
     * @param annotationClass marker annotation used to scan extension classes
     * @param modId target mod ID
     * @param versionRange Maven-style version range checked at runtime
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerLoadableMod(Class<? extends Annotation> annotationClass, String modId,
            String versionRange) {
        modRegistrar().loadableMod(annotationClass, modId, versionRange);
        return this;
    }

    /**
     * Registers an optional integration with a custom display name and version range.
     *
     * @param annotationClass marker annotation used to scan extension classes
     * @param modId target mod ID
     * @param displayModName display name used when matching and reporting the mod
     * @param versionRange Maven-style version range checked at runtime
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerLoadableMod(Class<? extends Annotation> annotationClass, String modId,
            String displayModName, String versionRange) {
        modRegistrar().loadableMod(annotationClass, modId, displayModName, versionRange);
        return this;
    }

    /**
     * Registers a terminal configuration tab.
     *
     * @param tab tab definition to add
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerConfigTab(MyoConfigTab tab) {
        configRegistrar().terminalConfigTab(tab);
        return this;
    }

    /**
     * Registers an item supplier in the shared Myotus creative tab.
     *
     * @param item item supplier to add
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerCreativeTabItem(Supplier<? extends ItemLike> item) {
        creativeTabRegistrar().creativeTabItem(item);
        return this;
    }

    /**
     * Registers a stack supplier in the shared Myotus creative tab.
     *
     * @param stack stack supplier to add
     * @return {@code this} for chaining
     */
    default IMyotusAPI registerCreativeTabStack(Supplier<ItemStack> stack) {
        creativeTabRegistrar().creativeTabStack(stack);
        return this;
    }

    /**
     * Returns whether the integration registered for the supplied mod ID is active.
     *
     * @param modId target mod ID
     * @return {@code true} if the integration is active
     */
    default boolean isIntegrationLoaded(String modId) {
        return modIntegrationManager().isLoaded(modId);
    }

    /**
     * Returns whether the integration registered for the supplied marker
     * annotation is active.
     *
     * @param annotationClass marker annotation used during registration
     * @return {@code true} if the integration is active
     */
    default boolean isIntegrationLoaded(Class<? extends Annotation> annotationClass) {
        return modIntegrationManager().isLoaded(annotationClass);
    }
}
