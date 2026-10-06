package me.myogoo.myotus.client;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.style.WidgetStyle;
import appeng.menu.me.common.MEStorageMenu;
import me.myogoo.myotus.api.ITerminalModuleCard;
import me.myogoo.myotus.api.client.AbstractTerminalModuleWidget;
import me.myogoo.myotus.api.client.ITerminalModuleRecipeTransfer;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TerminalModuleRecipeTransfersTest {
    private MockedStatic<Minecraft> minecraft;
    private Minecraft client;
    private LocalPlayer player;

    @BeforeEach
    void clientPlayer() {
        client = mock(Minecraft.class);
        player = mock(LocalPlayer.class);
        client.player = player;
        when(player.isAlive()).thenReturn(true);
        minecraft = mockStatic(Minecraft.class);
        minecraft.when(Minecraft::getInstance).thenReturn(client);
    }

    @AfterEach
    void releaseClient() {
        minecraft.close();
    }

    @Test
    void rejectsInvalidAndDuplicateRegistrations() {
        var card = card();
        var target = mock(ITerminalModuleRecipeTransfer.class);
        assertThrows(NullPointerException.class, () -> TerminalModuleRecipeTransfers.register(null, target));
        assertThrows(NullPointerException.class, () -> TerminalModuleRecipeTransfers.register(card, null));
        assertThrows(IllegalArgumentException.class,
                () -> TerminalModuleRecipeTransfers.register(mock(Item.class), target));
        TerminalModuleRecipeTransfers.register(card, target);
        assertThrows(IllegalArgumentException.class, () -> TerminalModuleRecipeTransfers.register(card, target));
    }

    @Test
    void soleCompatibleTargetIsAutomaticAndDryRunNeverTransfers() {
        var fixture = new Fixture();
        var card = card();
        var target = fixture.target(card);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(card));
            var preview = fixture.route(false);
            assertSame(card, preview.target());
            assertTrue(preview.canTransfer());
            assertFalse(preview.sent());
            verify(target, never()).transfer(any(), any(), anyBoolean());
            var sent = fixture.route(true);
            assertTrue(sent.sent());
            verify(target).transfer(fixture.menu, fixture.recipe, true);
        }
    }

    @Test
    void missingUnavailableAndIncompatibleTargetsAndMissingRecipeNeverTransfer() {
        var fixture = new Fixture();
        var card = card();
        var target = fixture.target(card);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of());
            assertFalse(fixture.route(true).canTransfer());
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(card));
            when(((ITerminalModuleCard) card).isAvailable()).thenReturn(false);
            assertFalse(fixture.route(true).canTransfer());
            when(((ITerminalModuleCard) card).isAvailable()).thenReturn(true);
            when(target.supports(fixture.menu, fixture.recipe)).thenReturn(false);
            assertFalse(fixture.route(true).canTransfer());
            when(target.supports(fixture.menu, fixture.recipe)).thenReturn(true);
            assertFalse(TerminalModuleRecipeTransfers.route(fixture.screen, null, true, true).canTransfer());
            verify(target, never()).transfer(any(), any(), anyBoolean());
        }
    }

    @Test
    void multipleTargetsRequireSelectionAndRemovalClearsItEvenAfterReinstallation() {
        var fixture = new Fixture();
        var first = card();
        var second = card();
        var firstTarget = fixture.target(first);
        var secondTarget = fixture.target(second);
        fixture.panel(first, 0);
        fixture.panel(second, 40);
        fixture.update(first, second);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(first, second));
            var ambiguous = fixture.route(true);
            assertNull(ambiguous.target());
            assertEquals(Component.translatable("myotus.recipe_transfer.select_module"), ambiguous.error());
            fixture.modules.onMouseDown(new Point(5, 5), 0);
            assertSame(first, fixture.route(false).target());
            fixture.update(first, second);
            assertSame(first, fixture.modules.getLastClickedModuleCard());
            fixture.modules.onMouseDown(new Point(45, 5), 0);
            assertSame(second, fixture.route(false).target());

            // Removing the card is rejected before the screen has reconciled its widgets.
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(first));
            assertSame(first, fixture.route(false).target());
            fixture.update(first);
            assertNull(fixture.modules.getLastClickedModuleCard());
            fixture.update(first, second);
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(first, second));
            assertFalse(fixture.route(true).canTransfer());
            verify(firstTarget, never()).transfer(any(), any(), anyBoolean());
            verify(secondTarget, never()).transfer(any(), any(), anyBoolean());
        }
    }

    @Test
    void incompatibleSelectionFallsBackOnlyToSoleCompatibleTargetAndErrorsDoNotRetarget() {
        var fixture = new Fixture();
        var first = card();
        var second = card();
        var firstTarget = fixture.target(first);
        var secondTarget = fixture.target(second);
        fixture.panel(first, 0);
        fixture.panel(second, 40);
        fixture.update(first, second);
        fixture.modules.onMouseDown(new Point(5, 5), 0);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(first, second));
            when(firstTarget.supports(fixture.menu, fixture.recipe)).thenReturn(false);
            assertSame(second, fixture.route(false).target());
            when(firstTarget.supports(fixture.menu, fixture.recipe)).thenReturn(true);
            var error = Component.literal("Missing ingredients");
            when(firstTarget.getError(fixture.menu, fixture.recipe)).thenReturn(error);
            var rejected = fixture.route(true);
            assertSame(first, rejected.target());
            assertSame(error, rejected.error());
            assertFalse(rejected.sent());
            verify(firstTarget, never()).transfer(any(), any(), anyBoolean());
            verify(secondTarget, never()).transfer(any(), any(), anyBoolean());
            when(firstTarget.getError(fixture.menu, fixture.recipe)).thenReturn(null);
            when(firstTarget.transfer(fixture.menu, fixture.recipe, true)).thenReturn(false);
            assertFalse(fixture.route(true).canTransfer());
        }
    }

    private static Item card() {
        var card = mock(Item.class, withSettings().extraInterfaces(ITerminalModuleCard.class));
        when(((ITerminalModuleCard) card).isAvailable()).thenReturn(true);
        return card;
    }

    @Test
    void staleMissingDeadAndSpectatorPlayerNeverReachesAddonCallbacks() {
        var fixture = new Fixture();
        var card = card();
        var target = fixture.target(card);
        try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
            helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(card));
            client.player = null;
            assertFalse(fixture.route(true).canTransfer());
            client.player = player;
            player.containerMenu = mock(MEStorageMenu.class);
            assertFalse(fixture.route(true).canTransfer());
            player.containerMenu = fixture.menu;
            when(player.isAlive()).thenReturn(false);
            assertFalse(fixture.route(true).canTransfer());
            when(player.isAlive()).thenReturn(true);
            when(player.isSpectator()).thenReturn(true);
            assertFalse(fixture.route(true).canTransfer());
            verifyNoInteractions(target);
        }
    }

    @Test
    void addonExceptionsBecomeUnavailableWithoutEscapingTheGui() {
        for (String phase : List.of("supports", "check", "transfer")) {
            var fixture = new Fixture();
            var card = card();
            var target = fixture.target(card);
            var failure = new IllegalStateException("Intentional test failure in " + phase);
            switch (phase) {
                case "supports" -> when(target.supports(fixture.menu, fixture.recipe)).thenThrow(failure);
                case "check" -> when(target.getError(fixture.menu, fixture.recipe)).thenThrow(failure);
                case "transfer" -> when(target.transfer(fixture.menu, fixture.recipe, true)).thenThrow(failure);
            }
            try (var helper = mockStatic(TerminalUpgradeHelper.class)) {
                helper.when(() -> TerminalUpgradeHelper.getInstalledUpgradeItems(fixture.menu)).thenReturn(Set.of(card));
                var result = assertDoesNotThrow(() -> fixture.route(true));
                assertFalse(result.canTransfer());
                assertFalse(result.sent());
                assertEquals(Component.translatable("myotus.recipe_transfer.unavailable"), result.error());
                if (!phase.equals("transfer")) verify(target, never()).transfer(any(), any(), anyBoolean());
            }
        }
    }

    private static final class Fixture {
        private final MEStorageMenu menu = mock(MEStorageMenu.class);
        private final RecipeHolder<?> recipe = mock(RecipeHolder.class);
        private final TerminalModuleWidgets modules = new TerminalModuleWidgets();
        private final AEBaseScreen<MEStorageMenu> screen;
        private final Map<String, ICompositeWidget> widgets = new LinkedHashMap<>();
        private final Map<String, WidgetStyle> styles = new LinkedHashMap<>();

        @SuppressWarnings("unchecked")
        private Fixture() {
            Minecraft.getInstance().player.containerMenu = menu;
            screen = mock(AEBaseScreen.class, withSettings().extraInterfaces(TerminalModuleScreen.class));
            when(screen.getMenu()).thenReturn(menu);
            when(((TerminalModuleScreen) screen).myotus$getModuleWidgets()).thenReturn(modules);
        }

        private ITerminalModuleRecipeTransfer target(Item card) {
            var target = mock(ITerminalModuleRecipeTransfer.class);
            when(target.supports(menu, recipe)).thenReturn(true);
            when(target.transfer(menu, recipe, true)).thenReturn(true);
            TerminalModuleRecipeTransfers.register(card, target);
            return target;
        }

        private void panel(Item card, int x) {
            var panel = mock(AbstractTerminalModuleWidget.class);
            when(panel.getBounds()).thenReturn(new Rect2i(x, 0, 30, 30));
            when(panel.isVisible()).thenReturn(true);
            TerminalModuleWidgets.register(card, (screen, stack) -> panel);
        }

        private void update(Item... cards) {
            List<ItemStack> installed = java.util.Arrays.stream(cards).map(card -> {
                var stack = mock(ItemStack.class);
                when(stack.getItem()).thenReturn(card);
                when(stack.copy()).thenReturn(stack);
                return stack;
            }).toList();
            modules.update(screen, installed, widgets, styles);
        }

        private TerminalModuleRecipeTransfers.Result route(boolean transfer) {
            return TerminalModuleRecipeTransfers.route(screen, recipe, true, transfer);
        }
    }
}
