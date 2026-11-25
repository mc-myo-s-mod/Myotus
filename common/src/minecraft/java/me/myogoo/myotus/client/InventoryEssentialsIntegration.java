package me.myogoo.myotus.client;

import me.myogoo.myotus.platform.AnnotationScanData;
import me.myogoo.myotus.util.reflect.annotation.SortBlacklistDiscovery;
import net.blay09.mods.inventoryessentials.InventoryEssentialsIgnores;

/** Loaded on the client only when Inventory Essentials is installed. */
public final class InventoryEssentialsIntegration {
    private InventoryEssentialsIntegration() {
    }

    public static void register() {
        for (String className : SortBlacklistDiscovery.findTargets(AnnotationScanData.getClassHierarchy())) {
            // Both sets compare exact names; classification does not need to load optional screen classes.
            InventoryEssentialsIgnores.addIgnoredMenuClass(className);
            InventoryEssentialsIgnores.addIgnoredScreenClass(className);
        }
    }
}
