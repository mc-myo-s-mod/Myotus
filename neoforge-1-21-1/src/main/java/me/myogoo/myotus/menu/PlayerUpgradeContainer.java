package me.myogoo.myotus.menu;

import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import me.myogoo.myotus.Myotus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.regex.Pattern;

/**
 * Stores upgrade slot contents in player persistent data using a terminal-specific key.
 */
@EventBusSubscriber(modid = Myotus.MODID)
public class PlayerUpgradeContainer extends AppEngInternalInventory implements InternalInventoryHost {

    public static final int SIZE = 5;

    private static final Pattern STORAGE_KEY_PATTERN = Pattern.compile(
            "(?:item:(?:ae2wtlib:wireless_universal_terminal|[\\p{javaJavaIdentifierPart}.]+:[a-z0-9_.-]+:[a-z0-9/._-]+):"
                    + "[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}"
                    + "|host:[\\p{javaJavaIdentifierPart}.]+"
                    + "|part:[\\p{javaJavaIdentifierPart}.]+:(?:[a-z0-9_.-]+:[a-z0-9/._-]+|unknown):"
                    + "-?[0-9]+,-?[0-9]+,-?[0-9]+:(?:down|up|north|south|west|east))");

    private final ServerPlayer player;
    private final String storageKey;

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        copyUpgrades(event.getOriginal().getPersistentData(), event.getEntity().getPersistentData());
    }

    static void copyUpgrades(CompoundTag original, CompoundTag target) {
        // Root terminal data predates this handler; copy unopened terminals too, without migrating other mod data.
        for (String key : original.getAllKeys()) {
            Tag value = original.get(key);
            if (STORAGE_KEY_PATTERN.matcher(key).matches() && isUpgradeInventory(value)) {
                target.put(key, value.copy());
            }
        }
    }

    private static boolean isUpgradeInventory(Tag value) {
        if (!(value instanceof ListTag items) || items.size() > SIZE) {
            return false;
        }
        for (Tag entry : items) {
            if (!(entry instanceof CompoundTag item) || !(item.get("Slot") instanceof NumericTag)
                    || !(item.get("id") instanceof StringTag)) {
                return false;
            }
            int slot = item.getInt("Slot");
            if (slot < 0 || slot >= SIZE) {
                return false;
            }
        }
        return true;
    }

    public PlayerUpgradeContainer(ServerPlayer player, String storageKey) {
        super(null, SIZE, 1, TerminalUpgradeSlotFilter.INSTANCE); // host=null 로 시작하여 로드 중 save 이벤트 방지
        this.player = player;
        this.storageKey = storageKey;

        if (player.getPersistentData().contains(storageKey)) {
            readFromNBT(player.getPersistentData(), storageKey, player.registryAccess());
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
