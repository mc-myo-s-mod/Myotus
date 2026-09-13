package me.myogoo.myotus.impl.registrar;

import me.myogoo.myotus.api.registrar.ICreativeTabRegistrar;
import me.myogoo.myotus.impl.CreativeTabManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public class CreativeTabRegistrarImpl implements ICreativeTabRegistrar {
    @Override
    public void creativeTabItem(Supplier<? extends ItemLike> item) {
        CreativeTabManager.INSTANCE.registerItem(item);
    }

    @Override
    public void creativeTabStack(Supplier<ItemStack> stack) {
        CreativeTabManager.INSTANCE.registerStack(stack);
    }
}
