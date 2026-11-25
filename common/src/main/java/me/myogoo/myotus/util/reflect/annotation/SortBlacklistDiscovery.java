package me.myogoo.myotus.util.reflect.annotation;

import me.myogoo.myotus.api.annotation.MyoSortBlacklist;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Internal discovery support shared by the loader-specific sorting integrations. */
public final class SortBlacklistDiscovery {
    private static final Type MARKER = Type.getType(MyoSortBlacklist.class);

    private SortBlacklistDiscovery() {
    }

    /** Collects marked types and scanned subclasses without loading their classes. */
    public static Set<String> findTargets(Map<String, String> parents) {
        Set<String> roots = new HashSet<>();
        AnnotationScanner.getAnnotations().stream()
                .filter(annotation -> annotation.targetType() == ElementType.TYPE)
                .filter(annotation -> annotation.annotationType().equals(MARKER))
                .forEach(annotation -> roots.add(annotation.className()));
        if (roots.isEmpty()) {
            return Set.of();
        }

        Set<String> result = new TreeSet<>(roots);
        for (String candidate : parents.keySet()) {
            Set<String> visited = new HashSet<>();
            String current = candidate;
            while (current != null && visited.add(current)) {
                if (roots.contains(current)) {
                    result.add(candidate);
                    break;
                }
                current = parents.get(current);
            }
        }
        return Collections.unmodifiableSet(result);
    }
}
