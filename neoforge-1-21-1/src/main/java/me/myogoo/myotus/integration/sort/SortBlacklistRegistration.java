package me.myogoo.myotus.integration.sort;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import me.myogoo.myotus.platform.AnnotationScanData;
import me.myogoo.myotus.util.reflect.annotation.SortBlacklistDiscovery;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.regex.Pattern;

public final class SortBlacklistRegistration {
    private SortBlacklistRegistration() {
    }

    public static boolean isMenuBlacklisted(AbstractContainerMenu menu) {
        return menu.getClass().isAnnotationPresent(MyoSortBlacklist.class);
    }

    public static void enqueue(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("invtweaks")) {
            var targets = SortBlacklistDiscovery.findTargets(AnnotationScanData.getClassHierarchy());
            for (String target : targets) {
                InterModComms.sendTo(Myotus.MODID, "invtweaks", "blacklist-screen", () -> Pattern.quote(target));
            }
        }
    }
}
