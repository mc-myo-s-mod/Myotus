package me.myogoo.myotus.init;

import me.myogoo.myotus.Myotus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MyoBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Myotus.MODID);

    public static final RegistryObject<Block> ENDER_PEARL_BLOCK = registerPearlBlock("ender_pearl_block");
    public static final RegistryObject<Block> CHARGED_ENDER_PEARL_BLOCK = registerPearlBlock("charged_ender_pearl_block");

    private static RegistryObject<Block> registerPearlBlock(String name) {
        return BLOCKS.register(name, () -> new Block(BlockBehaviour.Properties.of()
                .strength(0.6F, 3.0F)
                .sound(SoundType.GLASS)));
    }

    private MyoBlocks() {
    }
}
