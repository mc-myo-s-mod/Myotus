package me.myogoo.myotus.mixin.ae2wtlib;

import me.myogoo.myotus.menu.TerminalUpgradeStorageKey;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Pseudo
@Mixin(targets = "de.mari_023.ae2wtlib.wut.recipe.Common", remap = false)
public abstract class WUTRecipeCommonMixin {

    @Unique
    private static final ThreadLocal<UUID> myotus$originalWutStorageUuid = new ThreadLocal<>();

    @Inject(method = "mergeTerminal", at = @At("HEAD"), require = 0, remap = false)
    private static void myotus$captureOriginalWutStorageUuid(ItemStack wut, ItemStack toMerge,
            String terminalName, CallbackInfoReturnable<ItemStack> cir) {
        myotus$originalWutStorageUuid.set(TerminalUpgradeStorageKey.getStackUuid(wut));
    }

    @Inject(method = "mergeTerminal", at = @At("RETURN"), require = 0, remap = false)
    private static void myotus$rememberMergedTerminalStorage(ItemStack wut, ItemStack toMerge,
            String terminalName, CallbackInfoReturnable<ItemStack> cir) {
        try {
            TerminalUpgradeStorageKey.rememberMergedTerminalStorage(cir.getReturnValue(), toMerge, terminalName,
                    myotus$originalWutStorageUuid.get());
        } finally {
            myotus$originalWutStorageUuid.remove();
        }
    }
}
