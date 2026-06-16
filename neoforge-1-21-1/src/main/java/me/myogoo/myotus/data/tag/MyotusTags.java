package me.myogoo.myotus.data.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class MyotusTags {
    private MyotusTags() {
    }

    public static final class Items {
        public static final TagKey<Item> AE2_INSCRIBER_PRESSES = itemTag("ae2", "inscriber_presses");
        public static final TagKey<Item> STORAGE_BLOCKS_ENDER_PEARL = itemTag("c", "storage_blocks/ender_pearl");

        private Items() {
        }

        private static TagKey<Item> itemTag(String namespace, String path) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(namespace, path));
        }
    }
}
