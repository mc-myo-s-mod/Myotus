package me.myogoo.myotus.util;

import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import me.myogoo.myotus.util.reflect.annotation.SortBlacklistDiscovery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortBlacklistDiscoveryTest {
    private static boolean menuInitialized;

    @BeforeEach
    void setUp() {
        AnnotationScanner.setAnnotationProvider(Stream::empty);
    }

    @AfterEach
    void tearDown() {
        AnnotationScanner.setAnnotationProvider(Stream::empty);
    }

    @Test
    void discoversUnannotatedDescendantsAndReturnsSortedImmutableNames() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(marked(RootMenu.class)));
        Map<String, String> parents = new LinkedHashMap<>();
        parents.put(GrandchildMenu.class.getName(), ChildMenu.class.getName());
        parents.put(ChildMenu.class.getName(), RootMenu.class.getName());
        parents.put(UnrelatedMenu.class.getName(), Menu.class.getName());

        Set<String> result = SortBlacklistDiscovery.findTargets(parents);

        assertEquals(Set.of(RootMenu.class.getName(), ChildMenu.class.getName(), GrandchildMenu.class.getName()), result);
        assertEquals(result.stream().sorted().toList(), List.copyOf(result));
        assertThrows(UnsupportedOperationException.class, () -> result.add("another.Menu"));
    }

    @Test
    void annotationIsInheritedByMenuSubclasses() {
        assertTrue(ChildMenu.class.isAnnotationPresent(MyoSortBlacklist.class));
        assertTrue(GrandchildMenu.class.isAnnotationPresent(MyoSortBlacklist.class));
        assertFalse(UnrelatedMenu.class.isAnnotationPresent(MyoSortBlacklist.class));
    }

    @Test
    void doesNotLoadMarkedMenusScreensOrTheirDescendants() {
        String root = InitializingMenu.class.getName();
        String child = "addon.client.UnloadedChildMenu";
        String grandchild = "addon.OtherMenu";
        String screen = "addon.gui.UnloadedScreen";
        String childScreen = "addon.gui.ChildScreen";
        String irrelevantAnnotation = "blocked.IfModPresent$List";
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                marked(InitializingMenu.class),
                marked(screen),
                new ScannedAnnotation(Type.getObjectType(irrelevantAnnotation.replace('.', '/')),
                        Type.getObjectType("unrelated/Target"))));
        var thread = Thread.currentThread();
        var originalLoader = thread.getContextClassLoader();
        var attemptedLoads = new ArrayList<String>();
        try {
            thread.setContextClassLoader(new ClassLoader(originalLoader) {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    attemptedLoads.add(name);
                    throw new AssertionError("Unexpected class load: " + name);
                }
            });

            assertEquals(Set.of(root, child, grandchild, screen, childScreen),
                    SortBlacklistDiscovery.findTargets(Map.of(child, root, grandchild, child,
                            childScreen, screen, screen, "net.minecraft.client.gui.screens.Screen")));
            assertTrue(attemptedLoads.isEmpty());
            assertFalse(menuInitialized);
        } finally {
            thread.setContextClassLoader(originalLoader);
        }
    }

    @Test
    void collectsTypeNamesWithoutValidationAndIgnoresOtherTargets() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                marked(String.class),
                marked(MenuInterface.class),
                new ScannedAnnotation(Type.getType(MyoSortBlacklist.class), Type.getObjectType("missing/Menu")),
                new ScannedAnnotation(Type.getType(MyoSortBlacklist.class), ElementType.METHOD,
                        Type.getType(UnrelatedMenu.class)),
                marked(RootMenu.class)));

        assertEquals(Set.of(String.class.getName(), MenuInterface.class.getName(), "missing.Menu",
                        RootMenu.class.getName(), "arbitrary.Child"),
                SortBlacklistDiscovery.findTargets(Map.of("arbitrary.Child", String.class.getName())));
        AnnotationScanner.setAnnotationProvider(Stream::empty);
        assertTrue(SortBlacklistDiscovery.findTargets(Map.of()).isEmpty());
    }

    @Test
    void cyclesTerminateAndRepeatedRootsAndDescendantsAreDeduplicated() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                marked(RootMenu.class), marked(RootMenu.class), marked(ChildMenu.class)));
        Map<String, String> parents = Map.of(
                ChildMenu.class.getName(), RootMenu.class.getName(),
                GrandchildMenu.class.getName(), ChildMenu.class.getName(),
                "cycle.First", "cycle.Second",
                "cycle.Second", "cycle.First",
                "cycle.Self", "cycle.Self");

        assertEquals(Set.of(RootMenu.class.getName(), ChildMenu.class.getName(), GrandchildMenu.class.getName()),
                SortBlacklistDiscovery.findTargets(parents));
    }

    @Test
    void screenMarkerIncludesDescendantsButNotSiblingScreens() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(marked(RootScreen.class)));

        assertEquals(Set.of(RootScreen.class.getName(), ChildScreen.class.getName()),
                SortBlacklistDiscovery.findTargets(Map.of(
                        RootScreen.class.getName(), Screen.class.getName(),
                        ChildScreen.class.getName(), RootScreen.class.getName(),
                        SiblingScreen.class.getName(), Screen.class.getName())));
        assertTrue(ChildScreen.class.isAnnotationPresent(MyoSortBlacklist.class));
        assertFalse(SiblingScreen.class.isAnnotationPresent(MyoSortBlacklist.class));
    }

    @Test
    void interfaceAndComposedMarkersDoNotPropagate() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                marked(MenuInterface.class), marked(ComposedMarker.class),
                new ScannedAnnotation(Type.getType(ComposedMarker.class), Type.getType(ComposedMenu.class))));

        assertEquals(Set.of(MenuInterface.class.getName(), ComposedMarker.class.getName()),
                SortBlacklistDiscovery.findTargets(Map.of(
                        InterfaceMenu.class.getName(), Menu.class.getName(),
                        ComposedMenu.class.getName(), Menu.class.getName())));
        assertFalse(InterfaceMenu.class.isAnnotationPresent(MyoSortBlacklist.class));
        assertFalse(ComposedMenu.class.isAnnotationPresent(MyoSortBlacklist.class));
    }

    private static ScannedAnnotation marked(Class<?> target) {
        return new ScannedAnnotation(Type.getType(MyoSortBlacklist.class), Type.getType(target));
    }

    private static ScannedAnnotation marked(String target) {
        return new ScannedAnnotation(Type.getType(MyoSortBlacklist.class),
                Type.getObjectType(target.replace('.', '/')));
    }

    private abstract static class Menu {
    }

    @MyoSortBlacklist
    private abstract static class RootMenu extends Menu {
    }

    private static class ChildMenu extends RootMenu {
    }

    private static final class GrandchildMenu extends ChildMenu {
    }

    private static final class UnrelatedMenu extends Menu {
    }

    @MyoSortBlacklist
    private interface MenuInterface {
    }

    private static final class InterfaceMenu extends Menu implements MenuInterface {
    }

    @MyoSortBlacklist
    private @interface ComposedMarker {
    }

    @ComposedMarker
    private static final class ComposedMenu extends Menu {
    }

    private abstract static class Screen {
    }

    @MyoSortBlacklist
    private abstract static class RootScreen extends Screen {
    }

    private static final class ChildScreen extends RootScreen {
    }

    private static final class SiblingScreen extends Screen {
    }

    private static final class InitializingMenu extends Menu {
        static {
            menuInitialized = true;
        }
    }
}
