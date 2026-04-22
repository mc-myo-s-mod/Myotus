package me.myogoo.myotus.mixin.ae2;

import java.util.List;

import appeng.parts.reporting.AbstractTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import me.myogoo.myotus.menu.PlayerUpgradeContainer;
import me.myogoo.myotus.menu.TerminalUpgradeHost;
import me.myogoo.myotus.menu.TerminalUpgradeSlotFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractTerminalPart.class, remap = false)
public abstract class AbstractTerminalPartMixin implements TerminalUpgradeHost {

    @Unique
    private static final String MYOTUS_UPGRADES_NBT = "myotusUpgrades";

    @Unique
    private AppEngInternalInventory myotus$upgradeInventory;

    @Override
    public AppEngInternalInventory myotus$getUpgradeInventory() {
        if (myotus$upgradeInventory == null) {
            myotus$upgradeInventory = new AppEngInternalInventory(
                    (InternalInventoryHost) (Object) this,
                    PlayerUpgradeContainer.SIZE,
                    1,
                    TerminalUpgradeSlotFilter.INSTANCE);
        }
        return myotus$upgradeInventory;
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    private void myotus$readUpgradeInventory(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        myotus$getUpgradeInventory().readFromNBT(data, MYOTUS_UPGRADES_NBT, registries);
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    private void myotus$writeUpgradeInventory(CompoundTag data, HolderLookup.Provider registries, CallbackInfo ci) {
        myotus$getUpgradeInventory().writeToNBT(data, MYOTUS_UPGRADES_NBT, registries);
    }

    @Inject(method = "addAdditionalDrops", at = @At("TAIL"))
    private void myotus$addUpgradeDrops(List<ItemStack> drops, boolean wrenched, CallbackInfo ci) {
        for (ItemStack stack : myotus$getUpgradeInventory()) {
            if (!stack.isEmpty()) {
                drops.add(stack.copy());
            }
        }
    }

    @Inject(method = "clearContent", at = @At("TAIL"),remap = true)
    private void myotus$clearUpgradeInventory(CallbackInfo ci) {
        myotus$getUpgradeInventory().clear();
    }
}
