package me.myogoo.myotus.mixin.ae2;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseMenu.class, remap = false)
public interface AEBaseMenuAccessor {
    @Invoker("addSlot")
    Slot myotus$addModuleSlot(Slot slot, SlotSemantic semantic);
}
