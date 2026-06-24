package me.myogoo.myotus.mixin.curios;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.common.inventory.CurioSlot;

@Pseudo
@Mixin(targets = "top.theillusivec4.curios.common.inventory.container.CuriosContainer", remap = false)
public abstract class CuriosContainerMixin extends AbstractContainerMenu {
    @Unique
    private static final String MYOTUS_WIRELESS_TERMINAL_SLOT = "myotus_wireless_terminal";
    @Unique
    private static final ResourceLocation AE2_WIRELESS_CRAFTING_TERMINAL =
            ResourceLocation.fromNamespaceAndPath("ae2", "wireless_crafting_terminal");
    @Unique
    private static final ResourceLocation AE2WTLIB_WIRELESS_PATTERN_ENCODING_TERMINAL =
            ResourceLocation.fromNamespaceAndPath("ae2wtlib", "wireless_pattern_encoding_terminal");
    @Unique
    private static final ResourceLocation AE2WTLIB_WIRELESS_PATTERN_ACCESS_TERMINAL =
            ResourceLocation.fromNamespaceAndPath("ae2wtlib", "wireless_pattern_access_terminal");
    @Unique
    private static final ResourceLocation AE2WTLIB_WIRELESS_UNIVERSAL_TERMINAL =
            ResourceLocation.fromNamespaceAndPath("ae2wtlib", "wireless_universal_terminal");

    protected CuriosContainerMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "m_7648_", at = @At("HEAD"), cancellable = true, remap = false)
    private void myotus$quickMoveWirelessTerminalToMyotusSlot(Player player, int index,
            CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= this.slots.size()) {
            return;
        }

        Slot sourceSlot = this.slots.get(index);
        ItemStack sourceStack = sourceSlot.getItem();
        if (sourceStack.isEmpty() || !myotus$isWirelessTerminal(sourceStack) || myotus$isMyotusWirelessTerminalSlot(sourceSlot)) {
            return;
        }

        ItemStack original = sourceStack.copy();
        if (myotus$moveOneStackToEmptyMyotusWirelessTerminalSlot(sourceSlot, sourceStack)) {
            cir.setReturnValue(original);
        }
    }

    @Unique
    private boolean myotus$moveOneStackToEmptyMyotusWirelessTerminalSlot(Slot sourceSlot, ItemStack sourceStack) {
        for (Slot targetSlot : this.slots) {
            if (!myotus$isMyotusWirelessTerminalSlot(targetSlot) || targetSlot.hasItem() || !targetSlot.mayPlace(sourceStack)) {
                continue;
            }

            int moveCount = Math.min(sourceStack.getCount(), targetSlot.getMaxStackSize(sourceStack));
            if (moveCount <= 0) {
                continue;
            }

            ItemStack movedStack = sourceStack.copy();
            movedStack.setCount(moveCount);
            targetSlot.set(movedStack);
            targetSlot.setChanged();

            sourceStack.shrink(moveCount);
            if (sourceStack.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            }
            sourceSlot.setChanged();
            return true;
        }
        return false;
    }

    @Unique
    private static boolean myotus$isMyotusWirelessTerminalSlot(Slot slot) {
        return slot instanceof CurioSlot curioSlot
                && MYOTUS_WIRELESS_TERMINAL_SLOT.equals(curioSlot.getIdentifier());
    }

    @Unique
    private static boolean myotus$isWirelessTerminal(ItemStack stack) {
        ResourceLocation itemId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
        return AE2_WIRELESS_CRAFTING_TERMINAL.equals(itemId)
                || AE2WTLIB_WIRELESS_PATTERN_ENCODING_TERMINAL.equals(itemId)
                || AE2WTLIB_WIRELESS_PATTERN_ACCESS_TERMINAL.equals(itemId)
                || AE2WTLIB_WIRELESS_UNIVERSAL_TERMINAL.equals(itemId);
    }
}
