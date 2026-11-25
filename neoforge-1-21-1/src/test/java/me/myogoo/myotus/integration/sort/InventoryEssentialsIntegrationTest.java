package me.myogoo.myotus.integration.sort;

import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import me.myogoo.myotus.client.InventoryEssentialsIntegration;
import me.myogoo.myotus.platform.AnnotationScanData;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import net.blay09.mods.inventoryessentials.InventoryEssentialsIgnores;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.Mockito.mockStatic;

class InventoryEssentialsIntegrationTest {
    @Test
    void registersExactMenuAndScreenNamesIncludingTheirDescendants() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                marked("addon/Menu"), marked("addon/gui/Screen")));
        try (var scans = mockStatic(AnnotationScanData.class);
                var ignores = mockStatic(InventoryEssentialsIgnores.class)) {
            scans.when(AnnotationScanData::getClassHierarchy).thenReturn(Map.of(
                    "addon.ChildMenu", "addon.Menu",
                    "addon.gui.ChildScreen", "addon.gui.Screen",
                    "addon.OtherScreen", "net.minecraft.client.gui.screens.Screen"));

            InventoryEssentialsIntegration.register();

            for (String name : new String[] {
                    "addon.Menu", "addon.ChildMenu", "addon.gui.Screen", "addon.gui.ChildScreen"}) {
                ignores.verify(() -> InventoryEssentialsIgnores.addIgnoredMenuClass(name));
                ignores.verify(() -> InventoryEssentialsIgnores.addIgnoredScreenClass(name));
            }
            ignores.verifyNoMoreInteractions();
        } finally {
            AnnotationScanner.setAnnotationProvider(Stream::empty);
        }
    }

    private static ScannedAnnotation marked(String name) {
        return new ScannedAnnotation(Type.getType(MyoSortBlacklist.class), Type.getObjectType(name));
    }
}
