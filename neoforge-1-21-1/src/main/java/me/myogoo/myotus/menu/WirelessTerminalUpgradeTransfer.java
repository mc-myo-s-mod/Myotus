package me.myogoo.myotus.menu;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.PlayerInternalInventory;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Carries recipe provenance without touching the per-player inventories during recipe previews. */
public final class WirelessTerminalUpgradeTransfer {
    static final String SOURCES_TAG = "myotus_terminal_upgrade_sources";
    private static final String UUID_PATTERN = "[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}";
    private static final Pattern SOURCE = Pattern.compile("[a-z0-9_.-]+:[a-z0-9/._-]+:" + UUID_PATTERN);
    private static final Pattern STORAGE_KEY = Pattern.compile(
            "item:(?:[\\p{javaJavaIdentifierPart}.]+:)?([a-z0-9_.-]+:[a-z0-9/._-]+:(" + UUID_PATTERN + "))");

    private WirelessTerminalUpgradeTransfer() {}

    public static void prepareResult(ItemStack result, Iterable<ItemStack> inputs) {
        if (result.isEmpty()) return;
        Set<String> sources = new TreeSet<>();
        for (ItemStack input : inputs) {
            CompoundTag data = customData(input);
            sources.addAll(sources(data));
            if (!input.isEmpty() && data.hasUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG)) {
                sources.add(BuiltInRegistries.ITEM.getKey(input.getItem()) + ":"
                        + data.getUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG));
            }
        }
        if (sources.isEmpty()) return;
        CompoundTag data = customData(result);
        ListTag savedSources = new ListTag();
        sources.forEach(source -> savedSources.add(StringTag.valueOf(source)));
        data.put(SOURCES_TAG, savedSources);
        // Recipe assembly is repeated for previews; the result identity must remain stable.
        data.putUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG, UUID.nameUUIDFromBytes(
                ("myotus:wut-upgrade-transfer\n" + String.join("\n", sources)).getBytes(StandardCharsets.UTF_8)));
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    static void migrate(ServerPlayer player, ItemStack stack, String destinationKey,
            AppEngInternalInventory destination, int visibleSlots) {
        CompoundTag data = customData(stack);
        if (!data.hasUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG)) return;
        var playerInventory = new PlayerInternalInventory(player.getInventory());
        boolean pending = transfer(player.getPersistentData(), destinationKey, sources(data),
                data.getUUID(TerminalUpgradeStorageKey.STACK_UUID_TAG).toString(), destination, visibleSlots,
                item -> ItemStack.parseOptional(player.registryAccess(), item),
                playerInventory::addItems,
                () -> destination.writeToNBT(player.getPersistentData(), destinationKey, player.registryAccess()));
        if (pending) player.sendSystemMessage(Component.translatable("message.myotus.terminal_upgrades_pending"));
    }

    static boolean transfer(CompoundTag persistentData, String destinationKey, Set<String> sources,
            String ownUuid, InternalInventory destination, int visibleSlots,
            Function<CompoundTag, ItemStack> decode, Function<ItemStack, ItemStack> returnToPlayer, Runnable save) {
        Tag existing = persistentData.get(destinationKey);
        if (existing != null) {
            if (!PlayerUpgradeContainer.isUpgradeInventory(existing)) return true;
            for (Tag entry : (ListTag) existing) {
                if (decode.apply(((CompoundTag) entry).copy()).isEmpty()) return true;
            }
        }
        boolean pending = false;
        for (String key : new ArrayList<>(persistentData.getAllKeys())) {
            var match = STORAGE_KEY.matcher(key);
            if (key.equals(destinationKey) || !match.matches()
                    || (!sources.contains(match.group(1)) && !ownUuid.equals(match.group(2))
                            && !sources.contains("ae2wtlib:wireless_universal_terminal:" + match.group(2)))) continue;
            Tag raw = persistentData.get(key);
            if (!PlayerUpgradeContainer.isUpgradeInventory(raw)) continue;
            ListTag remaining = new ListTag();
            boolean changed = false;
            for (Tag entry : (ListTag) raw) {
                CompoundTag original = (CompoundTag) entry;
                ItemStack remainder = decode.apply(original.copy());
                if (remainder.isEmpty()) {
                    remaining.add(original.copy());
                    continue;
                }
                int count = remainder.getCount();
                for (int slot = 0; slot < Math.min(visibleSlots, destination.size()) && !remainder.isEmpty(); slot++) {
                    remainder = destination.insertItem(slot, remainder, false);
                }
                if (!remainder.isEmpty()) remainder = returnToPlayer.apply(remainder);
                changed |= remainder.getCount() < count;
                if (!remainder.isEmpty()) {
                    CompoundTag retained = original.copy();
                    if (remainder.getCount() != count) retained.putInt("count", remainder.getCount());
                    remaining.add(retained);
                }
            }
            if (changed) {
                save.run();
                if (remaining.isEmpty()) persistentData.remove(key);
                else persistentData.put(key, remaining);
            }
            pending |= !remaining.isEmpty();
        }
        return pending;
    }

    private static CompoundTag customData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static Set<String> sources(CompoundTag data) {
        Set<String> sources = new TreeSet<>();
        for (Tag entry : data.getList(SOURCES_TAG, Tag.TAG_STRING)) {
            String source = entry.getAsString();
            if (SOURCE.matcher(source).matches()) sources.add(source);
        }
        return sources;
    }
}
