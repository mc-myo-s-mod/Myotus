package me.myogoo.myotus.data;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.tag.MyotusTags;
import me.myogoo.myotus.init.MyoItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class MyotusItemTagDataProvider extends ItemTagsProvider {
    public MyotusItemTagDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                     CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, registries, Myotus.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(MyotusTags.Items.AE2_INSCRIBER_PRESSES).add(MyoItems.COMPAT_PRESS.get());
        getOrCreateRawBuilder(MyotusTags.Items.AE2CS_BLANK_PRINT_PRESSES)
                .addOptionalElement(Identifier.parse("ae2cs:blank_print_press"));
        tag(MyotusTags.Items.STORAGE_BLOCKS_ENDER_PEARL).add(MyoItems.ENDER_PEARL_BLOCK.get());
    }
}
