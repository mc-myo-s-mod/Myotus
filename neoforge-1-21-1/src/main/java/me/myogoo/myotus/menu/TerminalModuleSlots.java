package me.myogoo.myotus.menu;

import appeng.menu.SlotSemantic;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalModuleCard;
import me.myogoo.myotus.api.terminal.ITerminalModuleProvider;
import me.myogoo.myotus.api.terminal.TerminalModuleSlot;
import me.myogoo.myotus.mixin.ae2.AEBaseMenuAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Common registration and permanent native slot groups for terminal module cards. */
public final class TerminalModuleSlots {
    private static final Map<Item, Registration> REGISTRATIONS = new LinkedHashMap<>();
    private static List<Registration> frozenRegistrations;

    private record Registration(Item card, SlotSemantic semantic, ITerminalModuleProvider provider) {}

    public static final class State {
        private boolean initialized;
        private boolean initializing;
        private final Map<Item, List<Slot>> slots = new IdentityHashMap<>();
        private final Map<Slot, ITerminalModuleProvider> providers = new IdentityHashMap<>();
    }

    private TerminalModuleSlots() {}

    public static void register(Item card, SlotSemantic semantic, ITerminalModuleProvider provider) {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(semantic, "semantic");
        Objects.requireNonNull(provider, "provider");
        if (!(card instanceof ITerminalModuleCard)) {
            throw new IllegalArgumentException("Slot providers require an ITerminalModuleCard item");
        }
        if (BuiltInRegistries.ITEM.getResourceKey(card).isEmpty()) {
            throw new IllegalArgumentException("Register module slots after their item has been registered");
        }
        if (frozenRegistrations != null) {
            throw new IllegalStateException("Register terminal modules before the first terminal opens");
        }
        if (REGISTRATIONS.containsKey(card)
                || REGISTRATIONS.values().stream().anyMatch(entry -> entry.semantic().equals(semantic))) {
            throw new IllegalArgumentException("Terminal module card or slot semantic is already registered");
        }
        REGISTRATIONS.put(card, new Registration(card, semantic, provider));
    }

    private static List<Registration> registrations() {
        if (frozenRegistrations == null) {
            return REGISTRATIONS.values().stream()
                    .sorted(Comparator.comparing(entry -> Objects.requireNonNull(
                            BuiltInRegistries.ITEM.getKey(entry.card()), "registered module item id").toString()))
                    .toList();
        }
        return frozenRegistrations;
    }

    private static State state(MEStorageMenu menu) {
        return ((TerminalModuleMenu) menu).myotus$getModuleSlots();
    }

    /** Runs after the concrete constructor, before contents synchronization, on both sides. */
    public static void initialize(MEStorageMenu menu) {
        State state = state(menu);
        if (state.initialized) return;
        if (state.initializing) throw new IllegalStateException("A module factory reentered menu initialization");
        state.initializing = true;
        try {
            initialize(menu, state);
        } finally {
            state.initializing = false;
        }
    }

    private static void initialize(MEStorageMenu menu, State state) {
        if (frozenRegistrations == null) frozenRegistrations = registrations();
        Map<Slot, Boolean> used = new IdentityHashMap<>();
        menu.slots.forEach(slot -> used.put(slot, true));
        Map<Registration, List<Slot>> prepared = new LinkedHashMap<>();
        for (Registration registration : registrations()) {
            if (!menu.getSlots(registration.semantic()).isEmpty()) {
                throw new IllegalArgumentException("The module slot semantic is already used by this menu");
            }
            List<Slot> managed = new ArrayList<>();
            for (Slot nativeSlot : Objects.requireNonNull(registration.provider().createSlots(menu), "module slots")) {
                Objects.requireNonNull(nativeSlot, "module slot");
                if (used.put(nativeSlot, true) != null) {
                    throw new IllegalArgumentException("A module factory reused a slot in this menu");
                }
                TerminalModuleSlot slot;
                if (nativeSlot instanceof TerminalModuleSlot guarded) {
                    if (guarded.getMenu() != menu || guarded.getModuleCard() != registration.card()
                            || used.put(guarded.getDelegate(), true) != null) {
                        throw new IllegalArgumentException("Module slot owner, card or delegate does not match");
                    }
                    slot = guarded;
                } else {
                    slot = new TerminalModuleSlot(menu, nativeSlot, registration.card());
                }
                managed.add(slot);
            }
            prepared.put(registration, List.copyOf(managed));
        }
        prepared.forEach((registration, slots) -> {
            state.slots.put(registration.card(), slots);
            for (Slot slot : slots) {
                ((AEBaseMenuAccessor) menu).myotus$addModuleSlot(slot, registration.semantic());
                state.providers.put(slot, registration.provider());
            }
        });
        state.initialized = true;
    }

    public static List<Slot> slots(MEStorageMenu menu, Item card) {
        return state(menu).slots.getOrDefault(card, List.of());
    }

    public static boolean isModuleSlot(MEStorageMenu menu, Slot slot) {
        return slot instanceof TerminalModuleSlot guarded && guarded.getMenu() == menu;
    }

    public static Slot nativeSlot(Slot slot) {
        return slot instanceof TerminalModuleSlot guarded ? guarded.getDelegate() : slot;
    }

    public static boolean isInteractionAllowed(MEStorageMenu menu, Slot slot, Player player) {
        return isModuleSlot(menu, slot) && ((TerminalModuleSlot) slot).isInteractionAllowed(player);
    }

    public static void beforeMenuOpen(ServerPlayer player) {
        registrations().forEach(entry -> entry.provider().beforeMenuOpen(player));
    }

    public static boolean isLayoutValid(MEStorageMenu menu) {
        return !state(menu).initialized
                || registrations().stream().allMatch(entry -> entry.provider().isLayoutValid(menu));
    }

    public static ItemStack getCloneStack(MEStorageMenu menu, Slot slot) {
        if (!isInteractionAllowed(menu, slot, menu.getPlayer()) || !slot.mayPickup(menu.getPlayer())) {
            return ItemStack.EMPTY;
        }
        ITerminalModuleProvider provider = state(menu).providers.get(slot);
        if (provider != null) {
            return Objects.requireNonNull(provider.getCloneStack(((TerminalModuleSlot) slot).getDelegate()),
                    "module clone stack");
        }
        ItemStack stack = slot.getItem();
        return stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(stack.getMaxStackSize());
    }
}
