package me.myogoo.myotus.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalModuleCard;
import me.myogoo.myotus.api.client.ITerminalModuleRecipeTransfer;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import me.myogoo.myotus.util.MyoLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Explicit module routing only; callers retain all ordinary viewer transfer behavior. */
public final class TerminalModuleRecipeTransfers {
    private static final Map<Item, ITerminalModuleRecipeTransfer> TARGETS = new LinkedHashMap<>();

    private TerminalModuleRecipeTransfers() {}

    public static void register(Item card, ITerminalModuleRecipeTransfer transfer) {
        Objects.requireNonNull(card, "card");
        Objects.requireNonNull(transfer, "transfer");
        if (!(card instanceof ITerminalModuleCard)) {
            throw new IllegalArgumentException("Recipe transfer targets require an ITerminalModuleCard item");
        }
        if (TARGETS.putIfAbsent(card, transfer) != null) {
            throw new IllegalArgumentException("A recipe transfer target is already registered for this card");
        }
    }

    /** Resolves afresh for every preview/click. A dry run never invokes a target's transfer method. */
    public static Result route(AEBaseScreen<?> screen, @Nullable RecipeHolder<?> recipe,
            boolean maxTransfer, boolean doTransfer) {
        if (screen instanceof AESubScreen<?, ?> || !(screen instanceof TerminalModuleScreen bridge)
                || !(screen.getMenu() instanceof MEStorageMenu menu)) {
            return failed("unavailable");
        }
        var player = Minecraft.getInstance().player;
        if (player == null || player.containerMenu != menu || !player.isAlive() || player.isSpectator()) {
            return failed("unavailable");
        }
        if (recipe == null) return failed("server_recipe_required");
        try {
            return route(menu, bridge.myotus$getModuleWidgets(), recipe, maxTransfer, doTransfer);
        } catch (RuntimeException exception) {
            MyoLogger.error("Terminal module recipe transfer failed for {}", recipe.id(), exception);
            return failed("unavailable");
        }
    }

    private static Result route(MEStorageMenu menu, TerminalModuleWidgets modules, RecipeHolder<?> recipe,
            boolean maxTransfer, boolean doTransfer) {
        var candidates = TerminalUpgradeHelper.getInstalledUpgradeItems(menu).stream()
                .filter(card -> card instanceof ITerminalModuleCard module && module.isAvailable())
                .filter(card -> TARGETS.containsKey(card) && TARGETS.get(card).supports(menu, recipe))
                .toList();
        if (candidates.isEmpty()) return failed("no_target");
        Item selected = modules.getLastClickedModuleCard();
        Item card = candidates.contains(selected) ? selected : candidates.size() == 1 ? candidates.getFirst() : null;
        if (card == null) return failed("select_module");
        var target = TARGETS.get(card);
        Component error = target.getError(menu, recipe);
        if (error != null || !doTransfer) return new Result(card, error, false);
        boolean sent = target.transfer(menu, recipe, maxTransfer);
        return new Result(card, sent ? null : Component.translatable("myotus.recipe_transfer.unavailable"), sent);
    }

    private static Result failed(String key) {
        return new Result(null, Component.translatable("myotus.recipe_transfer." + key), false);
    }

    public record Result(@Nullable Item target, @Nullable Component error, boolean sent) {
        public boolean canTransfer() { return target != null && error == null; }

        public Component tooltip() {
            return error != null ? error : Component.translatable("myotus.recipe_transfer.target", target.getDescription());
        }
    }
}
