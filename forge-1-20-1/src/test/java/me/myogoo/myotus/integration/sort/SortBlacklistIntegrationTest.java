package me.myogoo.myotus.integration.sort;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import me.myogoo.myotus.platform.AnnotationScanData;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.objectweb.asm.Type;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SortBlacklistIntegrationTest {
    @Test
    void markedMenuDoesNotBlacklistUnmarkedMenus() {
        var marked = mock(MarkedMenu.class);
        var unmarked = mock(TestMenu.class);

        assertTrue(SortBlacklistRegistration.isMenuBlacklisted(marked));
        assertFalse(SortBlacklistRegistration.isMenuBlacklisted(unmarked));
    }

    @Test
    void serverGuardHonorsInheritedMarkersWithoutImcOrMenuType() {
        var child = mock(ChildMenu.class);
        when(child.getType()).thenThrow(new UnsupportedOperationException());
        assertThrows(UnsupportedOperationException.class, child::getType);
        assertTrue(SortBlacklistRegistration.isMenuBlacklisted(child));
    }

    @Test
    void sendsEscapedRegexForMarkedRootsAndScannedDescendantsOnly() {
        var modList = mock(ModList.class);
        when(modList.isLoaded("invtweaks")).thenReturn(true);
        String screen = "addon.gui.MarkedScreen$Nested";
        String childScreen = "addon.gui.ChildScreen";
        var hierarchy = Map.of(
                ChildMenu.class.getName(), MarkedMenu.class.getName(),
                "addon.gui.SiblingScreen", "net.minecraft.client.gui.screens.Screen",
                childScreen, screen);
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                new ScannedAnnotation(Type.getType(MyoSortBlacklist.class), Type.getType(MarkedMenu.class)),
                new ScannedAnnotation(Type.getType(MyoSortBlacklist.class), Type.getObjectType(screen.replace('.', '/'))),
                new ScannedAnnotation(Type.getType(Deprecated.class), Type.getType(TestMenu.class))));

        try (var mods = mockStatic(ModList.class);
                var scanData = mockStatic(AnnotationScanData.class);
                var comms = mockStatic(InterModComms.class)) {
            mods.when(ModList::get).thenReturn(modList);
            scanData.when(AnnotationScanData::getClassHierarchy).thenReturn(hierarchy);

            SortBlacklistRegistration.enqueue(mock(InterModEnqueueEvent.class));

            @SuppressWarnings({"rawtypes", "unchecked"})
            ArgumentCaptor<Supplier<String>> payloads = (ArgumentCaptor) ArgumentCaptor.forClass(Supplier.class);
            comms.verify(() -> InterModComms.sendTo(eq(Myotus.MODID), eq("invtweaks"),
                    eq("blacklist-screen"), payloads.capture()), times(4));
            assertEquals(Set.of(Pattern.quote(MarkedMenu.class.getName()), Pattern.quote(ChildMenu.class.getName()),
                            Pattern.quote(screen), Pattern.quote(childScreen)),
                    payloads.getAllValues().stream().map(Supplier::get).collect(Collectors.toSet()));
            comms.verifyNoMoreInteractions();
        } finally {
            AnnotationScanner.setAnnotationProvider(Stream::empty);
        }
    }

    @Test
    void skipsImcDiscoveryWhenRefoxedIsAbsent() {
        try (var mods = mockStatic(ModList.class);
                var scanData = mockStatic(AnnotationScanData.class);
                var comms = mockStatic(InterModComms.class)) {
            var modList = mock(ModList.class);
            mods.when(ModList::get).thenReturn(modList);

            SortBlacklistRegistration.enqueue(mock(InterModEnqueueEvent.class));

            scanData.verifyNoInteractions();
            comms.verifyNoInteractions();
        }
    }

    private static class TestMenu extends AbstractContainerMenu {
        TestMenu() {
            super((MenuType<?>) null, 0);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slotIndex) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    @MyoSortBlacklist
    private static class MarkedMenu extends TestMenu {
    }

    private static final class ChildMenu extends MarkedMenu {
    }
}
