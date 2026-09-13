package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MyoBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Myotus.MODID);

    public static final DeferredBlock<Block> ENDER_PEARL_BLOCK = registerPearlBlock("ender_pearl_block");
    public static final DeferredBlock<Block> CHARGED_ENDER_PEARL_BLOCK = registerPearlBlock("charged_ender_pearl_block");

    private static DeferredBlock<Block> registerPearlBlock(String name) {
        return BLOCKS.registerSimpleBlock(name, properties -> properties
                .strength(0.6F, 3.0F)
                .sound(SoundType.GLASS));
    }

    private MyoBlocks() {
    }
}
