package me.myogoo.myotus.platform.mod;

import me.myogoo.myotus.dto.MyoModInfo;
import net.neoforged.fml.ModList;

public final class NeoForgeModList implements IModList {
    public static final NeoForgeModList INSTANCE = new NeoForgeModList();

    private NeoForgeModList() {
    }

    @Override
    public boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public MyoModInfo getModInfoById(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> new MyoModInfo(
                        container.getModId(),
                        container.getNamespace(),
                        container.getModInfo().getDisplayName(),
                        container.getModInfo().getVersion()))
                .orElse(null);
    }
}
