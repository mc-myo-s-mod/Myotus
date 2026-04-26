package me.myogoo.myotus.api.wt;

import appeng.api.config.Actionable;
import appeng.api.upgrades.Upgrades;
import appeng.items.tools.powered.WirelessTerminalItem;
import appeng.menu.ISubMenu;
import de.mari_023.ae2wtlib.AE2wtlib;
import de.mari_023.ae2wtlib.terminal.IUniversalWirelessTerminalItem;
import de.mari_023.ae2wtlib.terminal.WTMenuHost;
import de.mari_023.ae2wtlib.wut.WUTHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AddTerminalEvent {
    private AddTerminalEvent() {
    }

    @FunctionalInterface
    public interface WTMenuHostFactory {
        WTMenuHost create(Player player, @Nullable Integer inventorySlot, ItemStack stack,
                BiConsumer<Player, ISubMenu> returnToMainMenu);
    }

    @Nullable
    private static List<Consumer<AddTerminalEvent>> HANDLERS = new ArrayList<>();

    private static final List<WirelessTerminalItem> CREATIVE_TAB_TERMINALS = new ArrayList<>();

    public static synchronized void register(Consumer<AddTerminalEvent> handler) {
        if (HANDLERS == null) {
            throw new IllegalStateException(
                    "Cannot register terminal registration handler after terminal registration already happened");
        }
        HANDLERS.add(Objects.requireNonNull(handler, "handler"));
    }

    public static synchronized void run() {
        if (HANDLERS == null) {
            throw new IllegalStateException("Cannot run terminal registration handler twice");
        }

        AddTerminalEvent event = new AddTerminalEvent();
        List.copyOf(HANDLERS).forEach(handler -> handler.accept(event));
        HANDLERS = null;
    }

    @Contract(pure = true)
    public static synchronized boolean didRun() {
        return HANDLERS == null;
    }

    @Contract("_, _, _, _ -> this")
    public synchronized AddTerminalEvent addTerminal(String modId, String terminalName, WTMenuHostFactory menuHostFactory,
            MenuType<?> menuType, IUniversalWirelessTerminalItem item) {
        return addTerminal(modId,terminalName, menuHostFactory, menuType, item, true);
    }

    @Contract("_, _, _, _, _ -> this")
    public synchronized AddTerminalEvent addTerminal(String modId, String terminalName, WTMenuHostFactory menuHostFactory,
            MenuType<?> menuType, IUniversalWirelessTerminalItem item, boolean quantumBridgeCard) {
        return addTerminal(terminalName, menuHostFactory, menuType, item,
                terminalName, makeItemTranslateKey(modId, terminalName), quantumBridgeCard);
    }

    @Contract("_, _, _, _, _ -> this")
    public synchronized AddTerminalEvent addTerminal(String modId, String terminalName, WTMenuHostFactory menuHostFactory,
            MenuType<?> menuType, IUniversalWirelessTerminalItem item, String hotkeyName) {
        return addTerminal(modId,terminalName, menuHostFactory, menuType, item, hotkeyName, true);
    }

    @Contract("_, _, _, _, _, _ -> this")
    public synchronized AddTerminalEvent addTerminal(String modId, String terminalName, WTMenuHostFactory menuHostFactory,
            MenuType<?> menuType, IUniversalWirelessTerminalItem item, String hotkeyName, boolean quantumBridgeCard) {
        return addTerminal(terminalName, menuHostFactory, menuType, item, hotkeyName, makeItemTranslateKey(modId, terminalName),
                quantumBridgeCard);
    }

    @Contract("_, _, _, _, _, _ -> this")
    public synchronized AddTerminalEvent addTerminal(String terminalName, WTMenuHostFactory menuHostFactory,
            MenuType<?> menuType, IUniversalWirelessTerminalItem item, String hotkeyName, String translationKey) {
        return addTerminal(terminalName, menuHostFactory, menuType, item, hotkeyName, translationKey, true);
    }

    @Contract("_, _, _, _, _, _, _ -> this")
    public synchronized AddTerminalEvent addTerminal(String terminalName, WTMenuHostFactory menuHostFactory,
            MenuType<?> menuType, IUniversalWirelessTerminalItem item, String hotkeyName, String translationKey,
            boolean quantumBridgeCard) {
        Objects.requireNonNull(terminalName, "terminalName");
        Objects.requireNonNull(menuHostFactory, "menuHostFactory");
        Objects.requireNonNull(menuType, "menuType");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(hotkeyName, "hotkeyName");
        Objects.requireNonNull(translationKey, "translationKey");

        if (WUTHandler.terminalNames.contains(terminalName)) {
            throw new IllegalStateException(
                    "Trying to register terminal with name " + terminalName + " but it already exists");
        }

        WUTHandler.addTerminal(terminalName, item::tryOpen,
                menuHostFactory::create,
                menuType, item, hotkeyName, translationKey);
        addToAE2WTLibCreativeTab(item);
        if (quantumBridgeCard) {
            addQuantumBridgeCardSupport(item);
        }
        return this;
    }

    public static synchronized void addCreativeTabTerminals(CreativeModeTab.Output output) {
        for (var terminalItem : List.copyOf(CREATIVE_TAB_TERMINALS)) {
            if (!ForgeRegistries.ITEMS.containsValue(terminalItem)) {
                continue;
            }

            var stack = new ItemStack(terminalItem);
            output.accept(stack.copy());
            terminalItem.injectAEPower(stack, terminalItem.getAEMaxPower(stack), Actionable.MODULATE);
            output.accept(stack);
        }
    }

    private static String makeItemTranslateKey(String modId, String terminalName) {
        return "item." + modId + "." + terminalName;
    }

    private static void addToAE2WTLibCreativeTab(IUniversalWirelessTerminalItem item) {
        if (item instanceof WirelessTerminalItem terminalItem) {
            CREATIVE_TAB_TERMINALS.add(terminalItem);
            return;
        }
        throw new IllegalArgumentException(
                "Cannot add terminal to AE2WTLib creative tab because it is not a WirelessTerminalItem");
    }

    private static void addQuantumBridgeCardSupport(IUniversalWirelessTerminalItem item) {
        if (item instanceof Item minecraftItem) {
            Upgrades.add(AE2wtlib.QUANTUM_BRIDGE_CARD, minecraftItem, 1);
            return;
        }
        throw new IllegalArgumentException(
                "Cannot add quantum bridge card support to a terminal item that is not an Item");
    }
}
