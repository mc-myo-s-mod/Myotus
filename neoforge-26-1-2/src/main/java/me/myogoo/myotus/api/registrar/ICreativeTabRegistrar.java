package me.myogoo.myotus.api.registrar;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registrar for contributing entries to the Myotus creative tab.
 *
 * <p>This is intended for add-on mods that want their items to appear alongside
 * Myotus-related content in a shared tab.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * MyotusAPI.creativeTabs()
 *         .registerCreativeTabItem(MY_ITEM);
 * }</pre>
 */
public interface ICreativeTabRegistrar {
    /**
     * Registers a single item-like supplier for the Myotus creative tab.
     *
     * @param item item supplier to add
     */
    void creativeTabItem(Supplier<? extends ItemLike> item);

    /**
     * Registers a single {@link ItemStack} supplier for the Myotus creative tab.
     *
     * @param stack stack supplier to add
     */
    void creativeTabStack(Supplier<ItemStack> stack);

    /**
     * Fluent alias for {@link #creativeTabItem(Supplier)}.
     *
     * @param item item supplier to add
     * @return {@code this} for chaining
     */
    default ICreativeTabRegistrar registerCreativeTabItem(Supplier<? extends ItemLike> item) {
        creativeTabItem(item);
        return this;
    }

    /**
     * Fluent alias for {@link #creativeTabStack(Supplier)}.
     *
     * @param stack stack supplier to add
     * @return {@code this} for chaining
     */
    default ICreativeTabRegistrar registerCreativeTabStack(Supplier<ItemStack> stack) {
        creativeTabStack(stack);
        return this;
    }

    /**
     * Registers multiple item suppliers in iteration order.
     *
     * @param items item suppliers to add
     * @return {@code this} for chaining
     */
    default ICreativeTabRegistrar registerCreativeTabItems(Iterable<? extends Supplier<? extends ItemLike>> items) {
        Objects.requireNonNull(items, "items");
        for (var item : items) {
            creativeTabItem(item);
        }
        return this;
    }

    /**
     * Registers multiple stack suppliers in iteration order.
     *
     * @param stacks stack suppliers to add
     * @return {@code this} for chaining
     */
    default ICreativeTabRegistrar registerCreativeTabStacks(Iterable<? extends Supplier<ItemStack>> stacks) {
        Objects.requireNonNull(stacks, "stacks");
        for (var stack : stacks) {
            creativeTabStack(stack);
        }
        return this;
    }
}
