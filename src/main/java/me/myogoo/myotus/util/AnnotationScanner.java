package me.myogoo.myotus.util;

import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnnotationScanner {

    private static Set<ModFileScanData.AnnotationData> cachedData = null;

    /**
     * 로드된 모드의 annotation만 필터링하여 반환.
     * ModIntegrationManager.initialize() 이후에 호출해야 정확한 결과를 얻을 수 있음.
     */
    public static Set<ModFileScanData.AnnotationData> getModAnnotations() {
        if (cachedData != null) {
            return cachedData;
        }

        List<Type> activeAnnotationTypes = ModIntegrationManager.getActiveIntegrations().values().stream()
                .map(Type::getType)
                .toList();

        cachedData = ModList.get()
                .getAllScanData()
                .stream()
                .flatMap(scanData -> scanData.getAnnotations().stream())
                .filter(a -> activeAnnotationTypes.contains(a.annotationType()))
                .collect(Collectors.toSet());

        return cachedData;
    }
}
