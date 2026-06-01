package me.myogoo.myotus.util;

import me.myogoo.myotus.api.annotation.MyotusSubscriber;
import me.myogoo.myotus.api.annotation.itemList.emi.EMI;
import me.myogoo.myotus.api.annotation.itemList.jei.JEI;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner;
import me.myogoo.myotus.util.reflect.annotation.AnnotationScanner.ScannedAnnotation;
import me.myogoo.myotus.util.reflect.annotation.ItemListScanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemListScannerTest {
    private static final List<String> calls = new ArrayList<>();

    @BeforeEach
    void setUp() {
        calls.clear();
        AnnotationScanner.setAnnotationProvider(Stream::empty);
    }

    @AfterEach
    void tearDown() {
        calls.clear();
        AnnotationScanner.setAnnotationProvider(Stream::empty);
    }

    @Test
    void invokesOnlyStaticSubscribersForRequestedItemListAnnotation() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(
                scanned(JEI.class, JeiSubscriber.class),
                scanned(EMI.class, EmiSubscriber.class)));

        ItemListScanner.invokeItemListSubscribers(JEI.class, "recipe");

        assertEquals(List.of("jei:recipe"), calls);
    }

    @Test
    void skipsNonStaticSubscriberMethods() {
        AnnotationScanner.setAnnotationProvider(() -> Stream.of(scanned(JEI.class, NonStaticSubscriber.class)));

        ItemListScanner.invokeItemListSubscribers(JEI.class, "recipe");

        assertEquals(List.of(), calls);
    }

    private static ScannedAnnotation scanned(Class<?> annotationClass, Class<?> targetClass) {
        return new ScannedAnnotation(Type.getType(annotationClass), ElementType.TYPE, Type.getType(targetClass));
    }

    private static final class JeiSubscriber {
        @MyotusSubscriber
        private static void register(String value) {
            calls.add("jei:" + value);
        }
    }

    private static final class EmiSubscriber {
        @MyotusSubscriber
        private static void register(String value) {
            calls.add("emi:" + value);
        }
    }

    private static final class NonStaticSubscriber {
        @MyotusSubscriber
        private void register(String value) {
            calls.add("non-static:" + value);
        }
    }
}
