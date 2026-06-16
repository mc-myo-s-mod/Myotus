package me.myogoo.myotus.data;

import me.myogoo.myotus.Myotus;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MyotusBlockTagDataProvider extends BlockTagsProvider {
    public MyotusBlockTagDataProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
                                      @Nullable ExistingFileHelper existingFileHelper) {
        super(output, registries, Myotus.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
    }
}
