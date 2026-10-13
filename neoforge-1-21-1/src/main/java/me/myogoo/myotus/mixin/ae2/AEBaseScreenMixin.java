package me.myogoo.myotus.mixin.ae2;

import appeng.client.Point;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.Tooltip;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.me.common.MEStorageMenu;
import com.mojang.blaze3d.platform.InputConstants;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.myogoo.myotus.api.annotation.mods.AE2WTLib;
import me.myogoo.myotus.client.KeyBindings;
import me.myogoo.myotus.client.TerminalModuleWidgets;
import me.myogoo.myotus.client.TerminalModuleScreen;
import me.myogoo.myotus.api.client.AbstractTerminalModuleWidget;
import me.myogoo.myotus.client.TerminalUpgradePanel;
import me.myogoo.myotus.client.gui.widgets.SidePanelToggleButton;
import me.myogoo.myotus.client.integration.AE2WTLibClientCompat;
import me.myogoo.myotus.config.MyotusConfig;
import me.myogoo.myotus.menu.TerminalUpgradeHelper;
import me.myogoo.myotus.menu.TerminalModuleSlots;
import me.myogoo.myotus.util.mod.ModIntegrationManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = AEBaseScreen.class, remap = false)
public abstract class AEBaseScreenMixin extends AbstractContainerScreen<AEBaseMenu> implements TerminalModuleScreen {
    @Shadow
    @Final
    protected WidgetContainer widgets;
    @Shadow
    @Final
    protected ScreenStyle style;
    @Shadow
    protected abstract <B extends Button> B addToLeftToolbar(B button);
    @Shadow
    public abstract void renderSlot(GuiGraphics graphics, Slot slot);
    @Shadow
    protected abstract void renderSlotHighlight(GuiGraphics graphics, Slot slot, int mouseX, int mouseY,
            float partialTick);
    @Unique
    private final TerminalModuleWidgets myotus$terminalModules = new TerminalModuleWidgets();

    @Override
    public TerminalModuleWidgets myotus$getModuleWidgets() {
        return myotus$terminalModules;
    }
    @Unique
    private TerminalUpgradePanel myotus$floatingSubScreen;
    @Unique
    private SidePanelToggleButton myotus$toggleButton;
    @Unique
    private boolean myotus$ae2WtlibScreen;
    @Unique
    private boolean myotus$renderingModuleSlots;

    protected AEBaseScreenMixin(AEBaseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void myotus$addFloatingSubScreen(AEBaseMenu menu, Inventory inventory, Component title,
            ScreenStyle style, CallbackInfo ci) {
        if (!(menu instanceof MEStorageMenu storageMenu) || (Object) this instanceof AESubScreen<?, ?>) {
            return;
        }

        TerminalModuleSlots.initialize(storageMenu);

        WidgetStyle panelStyle = new WidgetStyle();
        panelStyle.setRight(-3);
        ((ScreenStyleAccessor) (Object) style).myotus$getWidgets()
                .put(TerminalUpgradePanel.WIDGET_ID, panelStyle);
        myotus$ae2WtlibScreen = ModIntegrationManager.isLoaded(AE2WTLib.class)
                && AE2WTLibClientCompat.isMenuHost(storageMenu.getHost());
        myotus$floatingSubScreen = new TerminalUpgradePanel(storageMenu, imageWidth, myotus$ae2WtlibScreen);
        myotus$floatingSubScreen.setVisible(MyotusConfig.CLIENT.openSidePanel.get());
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void myotus$initializeModules(CallbackInfo ci) {
        myotus$updateModules();
        if (myotus$floatingSubScreen != null) {
            // Subclass constructors have now added all native and wireless-terminal buttons.
            if (myotus$toggleButton == null) {
                myotus$toggleButton = addToLeftToolbar(new SidePanelToggleButton(myotus$floatingSubScreen));
            }
            var composites = ((WidgetContainerAccessor) widgets).myotus$getCompositeWidgets();
            if (!composites.containsKey(TerminalUpgradePanel.WIDGET_ID)) {
                widgets.add(TerminalUpgradePanel.WIDGET_ID, myotus$floatingSubScreen);
            }
            myotus$floatingSubScreen.setVisible(MyotusConfig.CLIENT.openSidePanel.get());
            myotus$toggleButton.visible = MyotusConfig.CLIENT.showUpgradePanelButton.get();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void myotus$updateInstalledModules(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        // Rebuild outside widget iteration, clearing children contributed by removed composites too.
        if (myotus$updateModules()) {
            rebuildWidgets();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;updateBeforeRender()V", shift = At.Shift.AFTER))
    private void myotus$layoutModules(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
            CallbackInfo ci) {
        myotus$positionPanel();
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;onMouseDown(Lappeng/client/Point;I)Z"))
    private boolean myotus$clickModule(WidgetContainer container, Point mouse, int button,
            Operation<Boolean> original) {
        Boolean result = myotus$terminalModules.onMouseDown(mouse, button);
        return result != null ? result : original.call(container, mouse, button);
    }

    @WrapOperation(method = "mouseDragged", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;onMouseDrag(Lappeng/client/Point;I)Z"))
    private boolean myotus$dragModule(WidgetContainer container, Point mouse, int button,
            Operation<Boolean> original) {
        Boolean result = myotus$terminalModules.onMouseDrag(mouse, button);
        return result != null ? result : original.call(container, mouse, button);
    }

    @WrapOperation(method = "mouseReleased", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;onMouseUp(Lappeng/client/Point;I)Z"))
    private boolean myotus$releaseModule(WidgetContainer container, Point mouse, int button,
            Operation<Boolean> original) {
        Boolean result = myotus$terminalModules.onMouseUp(mouse, button);
        return result != null ? result : original.call(container, mouse, button);
    }

    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;onMouseWheel(Lappeng/client/Point;D)Z"))
    private boolean myotus$scrollModule(WidgetContainer container, Point mouse, double delta,
            Operation<Boolean> original) {
        Boolean result = myotus$terminalModules.onMouseWheel(mouse, delta);
        return result != null ? result : original.call(container, mouse, delta);
    }

    @WrapOperation(method = "renderTooltips", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/WidgetContainer;getTooltip(II)Lappeng/client/gui/Tooltip;"))
    private Tooltip myotus$moduleTooltip(WidgetContainer container, int mouseX, int mouseY,
            Operation<Tooltip> original) {
        var panel = myotus$terminalModules.getModuleAt(new Point(mouseX, mouseY));
        return panel != null ? panel.getTooltip(mouseX, mouseY) : original.call(container, mouseX, mouseY);
    }

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void myotus$onlyHoverFrontModule(Slot slot, double mouseX, double mouseY,
            CallbackInfoReturnable<Boolean> cir) {
        Point mouse = new Point((int) Math.round(mouseX - leftPos), (int) Math.round(mouseY - topPos));
        if (!myotus$terminalModules.isSlotAccessible(slot, mouse)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void myotus$deferModuleSlots(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (!myotus$renderingModuleSlots && myotus$terminalModules.ownsSlot(slot)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void myotus$renderModuleLayers(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        var panels = myotus$terminalModules.getModuleWidgets();
        float layerHeight = 100f / Math.max(1, panels.size());
        Point mouse = new Point(mouseX - leftPos, mouseY - topPos);
        myotus$renderingModuleSlots = true;
        try {
            for (int i = 0; i < panels.size(); i++) {
                var panel = panels.get(i);
                if (!panel.isVisible()) {
                    continue;
                }
                graphics.pose().pushPose();
                try {
                    // All module layers stay below carried items and tooltips.
                    graphics.pose().translate(0, 0, 100 + i * layerHeight);
                    panel.render(graphics, mouse, layerHeight, slot -> renderSlot(graphics, slot), slot -> {
                        if (slot == hoveredSlot) {
                            renderSlotHighlight(graphics, slot, mouseX, mouseY, 0);
                        }
                    });
                } finally {
                    graphics.pose().popPose();
                }
            }
        } finally {
            myotus$renderingModuleSlots = false;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // A drag can start after the last render, leaving a stale slot for hotbar/drop shortcuts.
        if (hoveredSlot != null) {
            var window = minecraft.getWindow();
            Point mouse = new Point(
                    (int) Math.round(minecraft.mouseHandler.xpos() * width / window.getScreenWidth() - leftPos),
                    (int) Math.round(minecraft.mouseHandler.ypos() * height / window.getScreenHeight() - topPos));
            if (!myotus$terminalModules.isSlotAccessible(hoveredSlot, mouse)) {
                hoveredSlot = null;
            }
        }
        if (myotus$toggleButton != null
                && KeyBindings.TOGGLE_UPGRADE_TERMINAL_PANEL.isActiveAndMatches(
                        InputConstants.getKey(keyCode, scanCode))) {
            myotus$toggleButton.run();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Unique
    private void myotus$positionPanel() {
        if (myotus$floatingSubScreen == null) {
            return;
        }

        var composites = ((WidgetContainerAccessor) widgets).myotus$getCompositeWidgets();
        int panelX = imageWidth - 3;
        var upgrades = composites.get("upgrades");
        var scrollingUpgrades = composites.get("scrollingUpgrades");
        if (myotus$ae2WtlibScreen && upgrades != null && upgrades.isVisible()) {
            var bounds = upgrades.getBounds();
            panelX = bounds.getX() + bounds.getWidth();
        }
        if (scrollingUpgrades != null && scrollingUpgrades.isVisible()) {
            var bounds = scrollingUpgrades.getBounds();
            panelX = bounds.getX() + bounds.getWidth();
        }
        myotus$floatingSubScreen.setPosition(new Point(panelX, 0));
        var panels = composites.values().stream().filter(AbstractTerminalModuleWidget.class::isInstance)
                .map(AbstractTerminalModuleWidget.class::cast).toList();
        if (panels.isEmpty()) {
            return;
        }
        var inventorySlots = menu.getSlots(SlotSemantics.PLAYER_INVENTORY);
        int inventoryY = inventorySlots.isEmpty() ? imageHeight - 84 : inventorySlots.getFirst().y;
        int totalWidth = panels.stream().mapToInt(panel -> panel.getBounds().getWidth() + 2).sum() - 2;
        boolean vertical = totalWidth > leftPos - 24;
        int groupWidth = vertical ? panels.stream().mapToInt(panel -> panel.getBounds().getWidth()).max().orElse(0)
                : totalWidth;
        int groupHeight = vertical ? panels.stream().mapToInt(panel -> panel.getBounds().getHeight() + 2).sum() - 2
                : panels.stream().mapToInt(panel -> panel.getBounds().getHeight()).max().orElse(0);
        int moduleX = -24 - groupWidth;
        int moduleY = Math.clamp(inventoryY - 6, -topPos, Math.max(-topPos, height - topPos - groupHeight));
        Rect2i viewport = new Rect2i(-leftPos, -topPos, width, height);
        Rect2i terminal = new Rect2i(0, 0, imageWidth, imageHeight);
        var obstacles = List.of("verticalToolbar", "upgrades", "scrollingUpgrades", "upgradeScrollbar", "toolbox",
                        TerminalUpgradePanel.WIDGET_ID).stream()
                .map(composites::get).filter(widget -> widget != null && widget.isVisible()).toList();
        for (var panel : panels) {
            panel.layout(new Point(moduleX, moduleY), viewport, candidate ->
                    !myotus$overlaps(candidate, terminal) && obstacles.stream()
                            .noneMatch(other -> myotus$overlaps(candidate, other.getBounds())));
            if (vertical) {
                moduleY += panel.getBounds().getHeight() + 2;
            } else {
                moduleX += panel.getBounds().getWidth() + 2;
            }
        }
        // Take peer bounds only after every panel has its current layout, including after a resize.
        for (var panel : panels) {
            var targets = new ArrayList<Rect2i>();
            targets.add(terminal);
            obstacles.forEach(obstacle -> targets.add(obstacle.getBounds()));
            panels.stream().filter(other -> other != panel && other.isVisible())
                    .forEach(other -> targets.add(other.getBounds()));
            panel.setSnapTargets(targets);
        }
    }

    @Unique
    private static boolean myotus$overlaps(Rect2i first, Rect2i second) {
        return second.getWidth() > 0 && second.getHeight() > 0
                && first.getX() < second.getX() + second.getWidth() && first.getX() + first.getWidth() > second.getX()
                && first.getY() < second.getY() + second.getHeight() && first.getY() + first.getHeight() > second.getY();
    }

    @Unique
    private boolean myotus$updateModules() {
        if (!(menu instanceof MEStorageMenu storageMenu)) {
            return false;
        }
        ((TerminalModuleWidgets.ContainerAccess) widgets).myotus$setModuleWidgets(myotus$terminalModules);
        return myotus$terminalModules.update((AEBaseScreen<?>) (Object) this,
                TerminalUpgradeHelper.getInstalledUpgrades(storageMenu),
                ((WidgetContainerAccessor) widgets).myotus$getCompositeWidgets(),
                ((ScreenStyleAccessor) (Object) style).myotus$getWidgets());
    }
}
