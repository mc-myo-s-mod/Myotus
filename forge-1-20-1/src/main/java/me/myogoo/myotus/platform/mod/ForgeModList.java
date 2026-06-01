package me.myogoo.myotus.platform.mod;

import me.myogoo.myotus.dto.MyoModInfoDto;
import net.minecraftforge.fml.ModList;

public final class ForgeModList implements IModList {
    public static final ForgeModList INSTANCE = new ForgeModList();

    private ForgeModList() {
    }

    @Override
    public boolean isLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public MyoModInfoDto getModInfoById(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> new MyoModInfoDto(
                        container.getModId(),
                        container.getNamespace(),
                        container.getModInfo().getDisplayName(),
                        container.getModInfo().getVersion()))
                .orElse(null);
    }
}
