package me.myogoo.myotus.api.config;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Immutable description of a tab shown in the terminal configuration screen.
 *
 * <p>This is a client-only API. Construct and register tabs from client setup, never from a common
 * class that can load on a dedicated server.</p>
 *
 * <p>Every tab has a stable {@link ResourceLocation} id. Add-on mods should use
 * their own namespace, for example {@code examplemod:terminal_settings}. The id
 * is used for duplicate detection, ordering/debugging, and future tab-specific
 * configuration.</p>
 *
 * <p>The tab can render either an AE2 {@link Icon}, a Myotus {@link MyoIcon},
 * or an {@link ItemStack}. The supplied {@code stylePath} points to the screen
 * style JSON that should be applied while the tab is active.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyoConfigTab tab = new MyoConfigTab(
 *         ResourceLocation.fromNamespaceAndPath("examplemod", "terminal_settings"),
 *         Component.literal("Example"),
 *         Icon.WRENCH,
 *         "example_terminal.json",
 *         new ExampleConfigScreen());
 * }</pre>
 *
 * @param id stable tab id, usually namespaced by the registering mod
 * @param title tab title and tooltip text
 * @param blitter icon blitter used when the tab renders a texture-based icon
 * @param stack item stack icon used when the tab renders an item
 * @param stylePath relative path to the screen style JSON
 * @param configTabScreen screen builder used when this tab is selected
 * @param visibility visibility predicate used to decide whether the tab should
 *                   be shown for the currently opened terminal
 */
public record MyoConfigTab(ResourceLocation id, Component title, Blitter blitter, ItemStack stack, String stylePath,
                           MyoConfigTabScreen configTabScreen, MyoConfigTabVisibility visibility) {
    public MyoConfigTab {
        id = Objects.requireNonNull(id, "id");
        title = Objects.requireNonNull(title, "title");
        stylePath = Objects.requireNonNull(stylePath, "stylePath");
        if (stylePath.isBlank()) {
            throw new IllegalArgumentException("stylePath must not be blank");
        }
        stack = stack == null ? null : stack.copy();
        configTabScreen = Objects.requireNonNull(configTabScreen, "configTabScreen");
        visibility = Objects.requireNonNullElse(visibility, MyoConfigTabVisibility.ALWAYS_VISIBLE);
    }

    public MyoConfigTab(ResourceLocation id, Component title, Blitter blitter, ItemStack stack, String stylePath,
            MyoConfigTabScreen configTabScreen) {
        this(id, title, blitter, stack, stylePath, configTabScreen, MyoConfigTabVisibility.ALWAYS_VISIBLE);
    }

    public MyoConfigTab(ResourceLocation id, Component title, Icon icon, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(id, title, icon.getBlitter(), null, stylePath, configTabScreen, MyoConfigTabVisibility.ALWAYS_VISIBLE);
    }

    public MyoConfigTab(ResourceLocation id, Component title, Blitter blitter, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(id, title, blitter, null, stylePath, configTabScreen, MyoConfigTabVisibility.ALWAYS_VISIBLE);
    }

    public MyoConfigTab(ResourceLocation id, Component title, MyoIcon icon, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(id, title, icon.getBlitter(), null, stylePath, configTabScreen, MyoConfigTabVisibility.ALWAYS_VISIBLE);
    }

    public MyoConfigTab(ResourceLocation id, Component title, ItemStack stack, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(id, title, null, stack, stylePath, configTabScreen, MyoConfigTabVisibility.ALWAYS_VISIBLE);
    }

    /**
     * Builds the button used to represent this tab in the UI.
     *
     * @param onPress click handler invoked when the tab is selected
     * @return the configured tab button
     */
    public CustomTabButton getTabButton(Button.OnPress onPress) {
        if(stack != null) {
            return new CustomTabButton(stack, title, onPress);
        } else return new CustomTabButton(Objects.requireNonNullElseGet(blitter, Icon.WRENCH::getBlitter), title, onPress);
    }

    /** Builds this tab's button without exposing the Myotus implementation class. */
    public Button createButton(Button.OnPress onPress) {
        return getTabButton(onPress);
    }

    /** Returns a defensive copy of the item icon, if present. */
    @Override
    public ItemStack stack() {
        return stack == null ? null : stack.copy();
    }

    /**
     * Returns whether this tab should be shown for the supplied menu.
     *
     * @param menu current terminal menu
     * @return {@code true} if the tab should be visible
     */
    public boolean isVisible(MEStorageMenu menu) {
        return visibility.isVisible(MyoConfigTabContext.from(menu));
    }

    /**
     * Returns a copy of this tab with the supplied visibility predicate.
     *
     * @param visibility visibility predicate to apply
     * @return a new tab definition with the predicate attached
     */
    public MyoConfigTab visibleWhen(MyoConfigTabVisibility visibility) {
        return new MyoConfigTab(id, title, blitter, stack, stylePath, configTabScreen,
                Objects.requireNonNull(visibility, "visibility"));
    }

}
