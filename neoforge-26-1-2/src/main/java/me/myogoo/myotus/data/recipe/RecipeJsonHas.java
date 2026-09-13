package me.myogoo.myotus.data.recipe;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public final class RecipeJsonHas {
    private RecipeJsonHas() {
    }

    public static Criterion<?> has(HolderGetter<Item> items, ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, item).build());
    }
}
