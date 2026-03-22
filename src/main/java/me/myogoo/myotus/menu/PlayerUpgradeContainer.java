package me.myogoo.myotus.menu;

import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import net.minecraft.server.level.ServerPlayer;

/**
 * Stores upgrade slot contents in player persistent data using a terminal-specific key.
 */
public class PlayerUpgradeContainer extends AppEngInternalInventory implements InternalInventoryHost {

    private static final String LEGACY_NBT_KEY = "terminal_upgrades";
    public static final int SIZE = 5;

    private final ServerPlayer player;
    private final String storageKey;

    public PlayerUpgradeContainer(ServerPlayer player, String storageKey) {
        super(null, SIZE, 1, TerminalUpgradeSlotFilter.INSTANCE); // host=null 로 시작하여 로드 중 save 이벤트 방지
        this.player = player;
        this.storageKey = storageKey;

        if (player.getPersistentData().contains(storageKey)) {
            readFromNBT(player.getPersistentData(), storageKey, player.registryAccess());
        } else if (player.getPersistentData().contains(LEGACY_NBT_KEY)) {
            // Migrate the old shared terminal inventory into the first terminal opened after the update.
            readFromNBT(player.getPersistentData(), LEGACY_NBT_KEY, player.registryAccess());
            writeToNBT(player.getPersistentData(), storageKey, player.registryAccess());
            player.getPersistentData().remove(LEGACY_NBT_KEY);
        }

        setHost(this); // 로드 완료 후 호스트 등록 → 이후 변경 시 자동 저장
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        writeToNBT(player.getPersistentData(), storageKey, player.registryAccess());
    }

    @Override
    public boolean isClientSide() {
        return false;
    }
}
