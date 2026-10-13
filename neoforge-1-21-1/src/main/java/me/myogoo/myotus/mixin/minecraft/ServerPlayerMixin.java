package me.myogoo.myotus.mixin.minecraft;

import appeng.menu.me.common.MEStorageMenu;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.myogoo.myotus.menu.TerminalModuleSlots;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @ModifyExpressionValue(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;Ljava/util/function/Consumer;)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private AbstractContainerMenu myotus$syncModulesBeforeTerminalOpen(AbstractContainerMenu menu) {
        if (menu instanceof MEStorageMenu) {
            TerminalModuleSlots.beforeMenuOpen((ServerPlayer) (Object) this);
        }
        return menu;
    }
}
