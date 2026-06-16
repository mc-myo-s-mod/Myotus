package me.myogoo.myotus.data;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.data.recipe.advancedae.AdvancedAERecipes;
import me.myogoo.myotus.data.recipe.crafting.ExternalCraftingRecipes;
import me.myogoo.myotus.data.recipe.extendedae.ExtendedAERecipes;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Myotus.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MyotusDataGenerators {
    private MyotusDataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var registries = event.getLookupProvider();
        var existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new MyotusRecipeDataProvider(output));
        generator.addProvider(event.includeServer(), new ExtendedAERecipes(output));
        generator.addProvider(event.includeServer(), new AdvancedAERecipes(output));
        generator.addProvider(event.includeServer(), new ExternalCraftingRecipes(output));

        var blockTags = new MyotusBlockTagDataProvider(output, registries, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new MyotusItemTagDataProvider(output, registries,
                blockTags.contentsGetter(), existingFileHelper));
    }
}
