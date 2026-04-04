package me.myogoo.myotus.api.registrar;

import me.myogoo.myotus.api.config.MyoConfigTab;

import java.util.Objects;

/**
 * Registrar for terminal configuration tabs.
 *
 * <p>Tabs registered here become available in the terminal configuration UI.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyotusAPI.configRegistrar().registerTerminalConfigTab(new MyoConfigTab(
 *         Component.literal("Example"),
 *         Icon.WRENCH,
 *         "example_terminal.json",
 *         new ExampleConfigScreen()));
 * }</pre>
 */
public interface IConfigRegistrar {
    /**
     * Registers a single terminal configuration tab.
     *
     * @param tab tab definition to register
     */
    void terminalConfigTab(MyoConfigTab tab);

    /**
     * Fluent alias for {@link #terminalConfigTab(MyoConfigTab)}.
     *
     * @param tab tab definition to register
     * @return {@code this} for chaining
     */
    default IConfigRegistrar registerTerminalConfigTab(MyoConfigTab tab) {
        terminalConfigTab(tab);
        return this;
    }

    /**
     * Registers multiple terminal configuration tabs in iteration order.
     *
     * @param tabs tabs to register
     * @return {@code this} for chaining
     */
    default IConfigRegistrar registerTerminalConfigTabs(Iterable<MyoConfigTab> tabs) {
        Objects.requireNonNull(tabs, "tabs");
        for (var tab : tabs) {
            terminalConfigTab(tab);
        }
        return this;
    }
}
