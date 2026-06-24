package me.myogoo.myotus.data;

import me.myogoo.myotus.data.recipe.ae2cs.AE2CrystalScienceRecipes;
import me.myogoo.myotus.data.recipe.ae2lt.AE2LightningTechRecipes;
import me.myogoo.myotus.data.recipe.advancedae.AdvancedAERecipes;
import me.myogoo.myotus.data.recipe.crafting.ExternalCraftingRecipes;
import me.myogoo.myotus.data.recipe.extendedae.ExtendedAERecipes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class MyotusDataGenerators {
    private MyotusDataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();
        var pack = generator.getVanillaPack(true);

        pack.addProvider(packOutput -> new MyotusRecipeDataProvider(packOutput, registries));
        pack.addProvider(ExtendedAERecipes::new);
        pack.addProvider(AdvancedAERecipes::new);
        pack.addProvider(AE2CrystalScienceRecipes::new);
        pack.addProvider(AE2LightningTechRecipes::new);
        pack.addProvider(ExternalCraftingRecipes::new);

        var blockTags = pack.addProvider(packOutput -> new MyotusBlockTagDataProvider(packOutput, registries,
                existingFileHelper));
        pack.addProvider(packOutput -> new MyotusItemTagDataProvider(packOutput, registries,
                blockTags.contentsGetter(), existingFileHelper));
    }
}
