package me.myogoo.myotus.menu;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.parts.AEBasePart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

/**
 * Resolves a stable storage key for a terminal host so each terminal can have
 * its own upgrade inventory.
 */
public final class TerminalUpgradeStorageKey {
    private static final String STACK_UUID_TAG = "myotus_terminal_storage_uuid";
    private static final String MERGED_TERMINAL_STORAGE_TAG = "myotus_merged_terminal_storage";
    private static final String MERGED_TERMINAL_ITEM_TAG = "item";
    private static final String MERGED_TERMINAL_UUID_TAG = "uuid";
    private static final ResourceLocation WUT_ITEM_ID = ResourceLocation.fromNamespaceAndPath("ae2wtlib",
            "wireless_universal_terminal");

    private TerminalUpgradeStorageKey() {
    }

    public static String of(ITerminalHost host) {
        if (host instanceof AEBasePart part) {
            BlockEntity blockEntity = part.getBlockEntity();
            BlockPos pos = blockEntity.getBlockPos();
            String dimension = blockEntity.getLevel() != null
                    ? blockEntity.getLevel().dimension().location().toString()
                    : "unknown";
            return "part:" + host.getClass().getName()
                    + ":" + dimension
                    + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ()
                    + ":" + part.getSide().getName();
        }

        if (host instanceof ItemMenuHost<?> itemMenuHost) {
            ItemStack stack = itemMenuHost.getItemStack();
            String mergedTerminalStorageKey = getMergedTerminalStorageKey(host, stack);
            if (mergedTerminalStorageKey != null) {
                return mergedTerminalStorageKey;
            }

            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            return toItemStorageKey(host, itemId, getOrCreateStackUuid(stack).toString());
        }

        return "host:" + host.getClass().getName();
    }

    @Nullable
    public static UUID getStackUuid(ItemStack stack) {
        CompoundTag tag = copyCustomData(stack);
        return tag.hasUUID(STACK_UUID_TAG)
                ? tag.getUUID(STACK_UUID_TAG)
                : null;
    }

    public static CompoundTag copyWutStorageData(ItemStack stack) {
        CompoundTag tag = copyCustomData(stack);
        CompoundTag storageData = new CompoundTag();
        if (tag.hasUUID(STACK_UUID_TAG)) {
            storageData.putUUID(STACK_UUID_TAG, tag.getUUID(STACK_UUID_TAG));
        }
        if (tag.contains(MERGED_TERMINAL_STORAGE_TAG, Tag.TAG_COMPOUND)) {
            storageData.put(MERGED_TERMINAL_STORAGE_TAG, tag.getCompound(MERGED_TERMINAL_STORAGE_TAG).copy());
        }
        return storageData;
    }

    public static void rememberMergedTerminalStorage(ItemStack result, ItemStack mergedTerminal,
            Object terminalDefinition, @Nullable CompoundTag originalWutStorageData) {
        if (result.isEmpty()) {
            return;
        }

        CompoundTag originalStorageData = originalWutStorageData != null ? originalWutStorageData : new CompoundTag();
        UUID mergedTerminalUuid = getStackUuid(mergedTerminal);
        String terminalName = getTerminalName(terminalDefinition);
        CompoundTag resultTag = copyCustomData(result);
        CompoundTag mergedTerminals = new CompoundTag();
        boolean changed = false;

        if (originalStorageData.contains(MERGED_TERMINAL_STORAGE_TAG, Tag.TAG_COMPOUND)) {
            mergedTerminals.merge(originalStorageData.getCompound(MERGED_TERMINAL_STORAGE_TAG).copy());
            changed = true;
        }
        if (resultTag.contains(MERGED_TERMINAL_STORAGE_TAG, Tag.TAG_COMPOUND)) {
            mergedTerminals.merge(resultTag.getCompound(MERGED_TERMINAL_STORAGE_TAG).copy());
            changed = true;
        }
        if (mergedTerminalUuid != null && terminalName != null) {
            CompoundTag mergedTerminalInfo = new CompoundTag();
            mergedTerminalInfo.putString(MERGED_TERMINAL_ITEM_TAG,
                    BuiltInRegistries.ITEM.getKey(mergedTerminal.getItem()).toString());
            mergedTerminalInfo.putUUID(MERGED_TERMINAL_UUID_TAG, mergedTerminalUuid);
            mergedTerminals.put(terminalName, mergedTerminalInfo);
            changed = true;
        }
        if (!mergedTerminals.isEmpty()) {
            resultTag.put(MERGED_TERMINAL_STORAGE_TAG, mergedTerminals);
        }

        if (originalStorageData.hasUUID(STACK_UUID_TAG)) {
            resultTag.putUUID(STACK_UUID_TAG, originalStorageData.getUUID(STACK_UUID_TAG));
            changed = true;
        }

        if (changed) {
            setCustomData(result, resultTag);
        }
    }

    private static UUID getOrCreateStackUuid(ItemStack stack) {
        CompoundTag tag = copyCustomData(stack);
        if (tag.hasUUID(STACK_UUID_TAG)) {
            return tag.getUUID(STACK_UUID_TAG);
        }

        UUID uuid = UUID.randomUUID();
        tag.putUUID(STACK_UUID_TAG, uuid);
        setCustomData(stack, tag);
        return uuid;
    }

    @Nullable
    private static String getMergedTerminalStorageKey(ITerminalHost host, ItemStack stack) {
        if (stack.isEmpty() || !WUT_ITEM_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return null;
        }

        String terminalName = getCurrentTerminalName(stack);
        if (terminalName == null) {
            return null;
        }

        CompoundTag tag = copyCustomData(stack);
        if (!tag.contains(MERGED_TERMINAL_STORAGE_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag mergedTerminals = tag.getCompound(MERGED_TERMINAL_STORAGE_TAG);
        if (!mergedTerminals.contains(terminalName, Tag.TAG_COMPOUND)) {
            return null;
        }

        CompoundTag mergedTerminalInfo = mergedTerminals.getCompound(terminalName);
        if (!mergedTerminalInfo.hasUUID(MERGED_TERMINAL_UUID_TAG)) {
            return null;
        }

        ResourceLocation mergedItemId = ResourceLocation.tryParse(
                mergedTerminalInfo.getString(MERGED_TERMINAL_ITEM_TAG));
        if (mergedItemId == null) {
            return null;
        }

        return toItemStorageKey(host, mergedItemId,
                mergedTerminalInfo.getUUID(MERGED_TERMINAL_UUID_TAG).toString());
    }

    private static CompoundTag copyCustomData(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static String toItemStorageKey(ITerminalHost host, ResourceLocation itemId, String storageId) {
        return "item:" + host.getClass().getName() + ":" + itemId + ":" + storageId;
    }

    @Nullable
    private static String getCurrentTerminalName(ItemStack stack) {
        String terminalName = getTerminalName(getDataComponentValue(stack,
                "de.mari_023.ae2wtlib.api.AE2wtlibComponents", "CURRENT_TERMINAL"));
        if (terminalName != null) {
            return terminalName;
        }

        for (Object terminal : getWirelessTerminals()) {
            Object componentType = invokeNoArg(terminal, "componentType");
            if (componentType instanceof DataComponentType<?> type && getDataComponentValue(stack, type) != null) {
                terminalName = getTerminalName(terminal);
                if (terminalName != null) {
                    return terminalName;
                }
            }
        }
        return null;
    }

    private static Iterable<?> getWirelessTerminals() {
        try {
            Class<?> definitionClass = Class.forName("de.mari_023.ae2wtlib.api.registration.WTDefinition");
            Object terminals = definitionClass.getMethod("wirelessTerminals").invoke(null);
            if (terminals instanceof Iterable<?> iterable) {
                return iterable;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return Collections.emptyList();
    }

    @Nullable
    private static Object getDataComponentValue(ItemStack stack, String ownerClassName, String fieldName) {
        try {
            Object componentType = Class.forName(ownerClassName).getField(fieldName).get(null);
            if (componentType instanceof DataComponentType<?> type) {
                return getDataComponentValue(stack, type);
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    @Nullable
    private static <T> T getDataComponentValue(ItemStack stack, DataComponentType<T> type) {
        return stack.get(type);
    }

    @Nullable
    private static String getTerminalName(@Nullable Object terminalDefinition) {
        Object name = invokeNoArg(terminalDefinition, "terminalName");
        return name instanceof String string ? string : null;
    }

    @Nullable
    private static Object invokeNoArg(@Nullable Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
