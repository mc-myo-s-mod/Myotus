package me.myogoo.myotus.api.terminal;

import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import com.mojang.datafixers.util.Pair;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

/** A live, guarded view of a native slot; equipment and inventory remain owned by its delegate. */
public class TerminalModuleSlot extends Slot {
    private final MEStorageMenu menu;
    private final Slot delegate;
    private final Item moduleCard;

    public TerminalModuleSlot(MEStorageMenu menu, Slot delegate, Item moduleCard) {
        super(delegate.container, delegate.getContainerSlot(), -10000, -10000);
        this.menu = Objects.requireNonNull(menu, "menu");
        this.delegate = delegate;
        this.moduleCard = Objects.requireNonNull(moduleCard, "moduleCard");
    }

    public final MEStorageMenu getMenu() { return menu; }
    public final Slot getDelegate() { return delegate; }
    public final Item getModuleCard() { return moduleCard; }

    @Override
    public boolean isActive() {
        return TerminalUpgradeHelper.hasUpgrade(menu, moduleCard) && delegate.isActive();
    }

    /** Separate from vanilla allowModification, which rejects placement into some empty armor slots. */
    public boolean isInteractionAllowed(Player player) {
        if (player != menu.getPlayer() || !isActive()) {
            return false;
        }
        if (delegate.container == player.getInventory()
                && menu.isPlayerInventorySlotLocked(delegate.getContainerSlot())) {
            return false;
        }
        ItemStack stack = getItem();
        return !(menu.getHost() instanceof ItemMenuHost<?> host && !stack.isEmpty()
                && stack == host.getItemStack());
    }

    @Override
    public boolean allowModification(Player player) {
        return isInteractionAllowed(player) && delegate.allowModification(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isInteractionAllowed(menu.getPlayer()) && delegate.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return isInteractionAllowed(player) && delegate.mayPickup(player);
    }

    @Override
    public ItemStack getItem() { return delegate.getItem(); }

    @Override
    public boolean hasItem() { return delegate.hasItem(); }

    // Authoritative contents synchronization must also work while the module is absent.
    @Override
    public void set(ItemStack stack) { delegate.set(stack); }

    @Override
    public void setChanged() { delegate.setChanged(); }

    @Override
    public void setByPlayer(ItemStack stack) {
        if (isInteractionAllowed(menu.getPlayer())) delegate.setByPlayer(stack);
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack previous) {
        if (isInteractionAllowed(menu.getPlayer())) delegate.setByPlayer(stack, previous);
    }

    @Override
    public ItemStack remove(int amount) {
        return mayPickup(menu.getPlayer()) ? delegate.remove(amount) : ItemStack.EMPTY;
    }

    @Override
    public Optional<ItemStack> tryRemove(int amount, int limit, Player player) {
        return mayPickup(player) ? delegate.tryRemove(amount, limit, player) : Optional.empty();
    }

    @Override
    public ItemStack safeTake(int amount, int limit, Player player) {
        return mayPickup(player) ? delegate.safeTake(amount, limit, player) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack) {
        return mayPlace(stack) ? delegate.safeInsert(stack) : stack;
    }

    @Override
    public ItemStack safeInsert(ItemStack stack, int amount) {
        return mayPlace(stack) ? delegate.safeInsert(stack, amount) : stack;
    }

    @Override
    public void onTake(Player player, ItemStack stack) { delegate.onTake(player, stack); }

    @Override
    public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
        delegate.onQuickCraft(oldStack, newStack);
    }

    @Override
    public int getMaxStackSize() { return delegate.getMaxStackSize(); }

    @Override
    public int getMaxStackSize(ItemStack stack) { return delegate.getMaxStackSize(stack); }

    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return delegate.getNoItemIcon(); }

    @Override
    public boolean isHighlightable() { return delegate.isHighlightable(); }

    @Override
    public boolean isFake() { return delegate.isFake(); }
}
