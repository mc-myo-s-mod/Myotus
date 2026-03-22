package me.myogoo.myotus.api.config;

import appeng.client.gui.Icon;
import appeng.client.gui.style.Blitter;
import me.myogoo.myotus.client.gui.MyoIcon;
import me.myogoo.myotus.client.gui.widgets.button.CustomTabButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Immutable description of a tab shown in the terminal configuration screen.
 *
 * <p>The tab can render either an AE2 {@link Icon}, a Myotus {@link MyoIcon},
 * or an {@link ItemStack}. The supplied {@code stylePath} points to the screen
 * style JSON that should be applied while the tab is active.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyoConfigTab tab = new MyoConfigTab(
 *         Component.literal("Example"),
 *         Icon.COG,
 *         "example_terminal.json",
 *         new ExampleConfigScreen());
 * }</pre>
 *
 * @param title tab title and tooltip text
 * @param blitter icon blitter used when the tab renders a texture-based icon
 * @param stack item stack icon used when the tab renders an item
 * @param stylePath relative path to the screen style JSON
 * @param configTabScreen screen builder used when this tab is selected
 */

public record MyoConfigTab(Component title, Blitter blitter, ItemStack stack, String stylePath,
                           MyoConfigTabScreen configTabScreen) {
    /**
     * Creates a tab backed by an AE2 icon.
     *
     * @param title tab title and tooltip text
     * @param icon AE2 icon to render
     * @param stylePath relative path to the screen style JSON
     * @param configTabScreen screen builder used when this tab is selected
     */
    public MyoConfigTab(Component title, Icon icon, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(title, icon.getBlitter(), null, stylePath, configTabScreen);
    }

    /**
     * Creates a tab backed by a Myotus icon.
     *
     * @param title tab title and tooltip text
     * @param icon Myotus icon to render
     * @param stylePath relative path to the screen style JSON
     * @param configTabScreen screen builder used when this tab is selected
     */
    public MyoConfigTab(Component title, MyoIcon icon, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(title, icon.getBlitter(), null, stylePath, configTabScreen);
    }

    /**
     * Creates a tab backed by an item icon.
     *
     * @param title tab title and tooltip text
     * @param stack item stack to render on the tab button
     * @param stylePath relative path to the screen style JSON
     * @param configTabScreen screen builder used when this tab is selected
     */
    public MyoConfigTab(Component title, ItemStack stack, String stylePath, MyoConfigTabScreen configTabScreen) {
        this(title, null, stack, stylePath, configTabScreen);
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
        } else return new CustomTabButton(Objects.requireNonNullElseGet(blitter, Icon.COG::getBlitter), title, onPress);
    }
}
