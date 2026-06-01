package me.myogoo.myotus.api;

import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;

final class TerminalUpgradeFacade {
    private TerminalUpgradeFacade() {
    }

    static List<ItemStack> getInstalledUpgrades(MEStorageMenu menu) {
        return TerminalUpgradeHelper.getInstalledUpgrades(menu);
    }

    static Set<Item> getInstalledUpgradeItems(MEStorageMenu menu) {
        return TerminalUpgradeHelper.getInstalledUpgradeItems(menu);
    }

    static List<ItemStack> getAvailableUpgradeCards(MEStorageMenu menu) {
        return TerminalUpgradeHelper.getAvailableUpgradeCards(menu);
    }

    static List<Component> getAvailableUpgradeTooltip(MEStorageMenu menu) {
        return TerminalUpgradeHelper.getAvailableUpgradeTooltip(menu);
    }

    static boolean hasUpgrade(MEStorageMenu menu, Item upgradeItem) {
        return TerminalUpgradeHelper.hasUpgrade(menu, upgradeItem);
    }

    static boolean hasUpgrade(MEStorageMenu menu, ResourceLocation itemId) {
        return TerminalUpgradeHelper.hasUpgrade(menu, itemId);
    }

    static int countUpgrade(MEStorageMenu menu, Item upgradeItem) {
        return TerminalUpgradeHelper.countUpgrade(menu, upgradeItem);
    }

    static int countUpgrade(MEStorageMenu menu, ResourceLocation itemId) {
        return TerminalUpgradeHelper.countUpgrade(menu, itemId);
    }

    static boolean canInsertUpgrade(Iterable<ItemStack> installedStacks, int slot, ItemStack stack) {
        return TerminalUpgradeHelper.canInsertUpgrade(installedStacks, slot, stack);
    }
}
