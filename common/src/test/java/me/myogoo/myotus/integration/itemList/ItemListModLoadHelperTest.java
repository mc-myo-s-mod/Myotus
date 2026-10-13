package me.myogoo.myotus.integration.itemList;

import me.myogoo.myotus.api.annotation.MyotusSubscriber;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.transformer.throwables.IllegalClassLoadError;

import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemListModLoadHelperTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unreadableClassMetadataDoesNotStopLaterSubscribers(boolean mixinFailure) {
        assertSubscribers(mixinFailure, UnreadableSubscriber.class, List.of("healthy"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void unreadableMethodMetadataDoesNotHideHealthyMethods(boolean mixinFailure) {
        assertSubscribers(mixinFailure, PartiallyReadableSubscriber.class, List.of("sibling", "healthy"));
    }

    private static void assertSubscribers(boolean mixinFailure, Class<?> target, List<String> expected) {
        var thread = Thread.currentThread();
        var originalLoader = thread.getContextClassLoader();
        var calls = new ArrayList<String>();
        try {
            thread.setContextClassLoader(blockedContainerLoader(mixinFailure,
                    target == PartiallyReadableSubscriber.class));
            AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                    new ScannedAnnotation(Type.getType(Marker.class), Type.getType(target)),
                    new ScannedAnnotation(Type.getType(Marker.class), Type.getType(HealthySubscriber.class))));
            assertDoesNotThrow(() -> ItemListModLoadHelper.invokeItemListMod(Marker.class, List.class, calls));
            assertEquals(expected, calls);
        } finally {
            AnnotationScanner.setAnnotationProvider(Stream::empty);
            thread.setContextClassLoader(originalLoader);
        }
    }

    private static ClassLoader blockedContainerLoader(boolean mixinFailure, boolean blockMethodAnnotation) {
        return new ClassLoader(ItemListModLoadHelperTest.class.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(UnreadableContainer.class.getName())
                        || blockMethodAnnotation && name.equals(UnreadableAnnotation.class.getName())) {
                    if (mixinFailure) {
                        throw new IllegalClassLoadError("Protected repeatable annotation container");
                    }
                    throw new LinkageError("Unavailable repeatable annotation container");
                }
                if (!name.equals(UnreadableAnnotation.class.getName())
                        && !name.equals(UnreadableSubscriber.class.getName())
                        && !name.equals(PartiallyReadableSubscriber.class.getName())) {
                    return super.loadClass(name, resolve);
                }
                Class<?> loaded = findLoadedClass(name);
                if (loaded == null) {
                    try (var input = getResourceAsStream(name.replace('.', '/') + ".class")) {
                        byte[] bytes = input.readAllBytes();
                        loaded = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                if (resolve) {
                    resolveClass(loaded);
                }
                return loaded;
            }
        };
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Marker {
    }

    @Repeatable(UnreadableContainer.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface UnreadableAnnotation {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface UnreadableContainer {
        UnreadableAnnotation[] value();
    }

    @Marker
    @UnreadableAnnotation
    public static class UnreadableSubscriber {
        @MyotusSubscriber
        public static void register(List<String> calls) {
            calls.add("unreadable");
        }
    }

    @Marker
    public static class PartiallyReadableSubscriber {
        @MyotusSubscriber
        @UnreadableAnnotation
        public static void unreadable(List<String> calls) {
            calls.add("unreadable");
        }

        @MyotusSubscriber
        public static void healthy(List<String> calls) {
            calls.add("sibling");
        }
    }

    @Marker
    public static class HealthySubscriber {
        @MyotusSubscriber
        public static void register(List<String> calls) {
            calls.add("healthy");
        }
    }
}
