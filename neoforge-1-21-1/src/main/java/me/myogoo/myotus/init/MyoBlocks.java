package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MyoBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Myotus.MODID);

    public static final DeferredHolder<Block, Block> ENDER_PEARL_BLOCK = registerPearlBlock("ender_pearl_block");
    public static final DeferredHolder<Block, Block> CHARGED_ENDER_PEARL_BLOCK = registerPearlBlock("charged_ender_pearl_block");

    private static DeferredHolder<Block, Block> registerPearlBlock(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .strength(0.6F, 3.0F)
                .sound(SoundType.GLASS)));
    }

    private MyoBlocks() {
    }
}
