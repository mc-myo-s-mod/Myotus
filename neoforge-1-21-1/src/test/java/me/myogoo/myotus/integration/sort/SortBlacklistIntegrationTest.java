package me.myogoo.myotus.integration.sort;

import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import me.myogoo.myotus.platform.AnnotationScanData;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import org.junit.jupiter.api.BeforeAll;
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
    @BeforeAll
    static void bootstrapVanillaRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void markedMenuDoesNotBlacklistOtherMenusWithTheSameType() {
        var type = MenuType.GENERIC_9x3;
        var marked = new MarkedMenu(type);
        var unmarked = new TestMenu(type);
        var chest = ChestMenu.threeRows(1, mock(Inventory.class));

        assertSame(marked.getType(), unmarked.getType());
        assertSame(marked.getType(), chest.getType());
        assertTrue(SortBlacklistRegistration.isMenuBlacklisted(marked));
        assertFalse(SortBlacklistRegistration.isMenuBlacklisted(unmarked));
        assertFalse(SortBlacklistRegistration.isMenuBlacklisted(chest));
    }

    @Test
    void serverGuardHonorsInheritedMarkersWithoutImcOrMenuType() {
        assertTrue(SortBlacklistRegistration.isMenuBlacklisted(new ChildMenu(MenuType.GENERIC_9x3)));

        var untyped = new ChildMenu(null);
        assertThrows(UnsupportedOperationException.class, untyped::getType);
        assertTrue(SortBlacklistRegistration.isMenuBlacklisted(untyped));
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
        TestMenu(MenuType<?> type) {
            super(type, 0);
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
        MarkedMenu(MenuType<?> type) {
            super(type);
        }
    }

    private static final class ChildMenu extends MarkedMenu {
        ChildMenu(MenuType<?> type) {
            super(type);
        }
    }
}
