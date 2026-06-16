package me.myogoo.myotus.data;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.tag.MyotusTags;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MyotusItemTagDataProvider extends ItemTagsProvider {
    public MyotusItemTagDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                     CompletableFuture<TagLookup<Block>> blockTags,
                                     @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, blockTags, Myotus.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(MyotusTags.Items.AE2_INSCRIBER_PRESSES).add(MyoItems.COMPAT_PRESS.get());
        tag(MyotusTags.Items.STORAGE_BLOCKS_ENDER_PEARL).add(MyoItems.ENDER_PEARL_BLOCK.get());
    }
}
