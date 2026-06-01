package me.myogoo.myotus.impl;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public enum CreativeTabManager {
    INSTANCE;

    private final List<Supplier<? extends ItemLike>> items = new ArrayList<>();
    private final List<Supplier<ItemStack>> stacks = new ArrayList<>();

    public void registerItem(Supplier<? extends ItemLike> item) {
        items.add(item);
    }

    public void registerStack(Supplier<ItemStack> stack) {
        stacks.add(stack);
    }

    public void populate(CreativeModeTab.Output output) {
        for (var item : items) {
            output.accept(item.get());
        }

        for (var stack : stacks) {
            output.accept(stack.get());
        }
    }
}
