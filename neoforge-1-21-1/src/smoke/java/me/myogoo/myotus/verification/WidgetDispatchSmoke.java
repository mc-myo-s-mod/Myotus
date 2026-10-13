package me.myogoo.myotus.verification;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import me.myogoo.myotus.Myotus;
import me.myogoo.myotus.api.ITerminalCard;
import me.myogoo.myotus.client.TerminalModuleWidgets;
import me.myogoo.myotus.mixin.ae2.WidgetContainerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** Opt-in client regression; excluded from normal builds. */
@EventBusSubscriber(modid = Myotus.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class WidgetDispatchSmoke {
    private static Item firstCard;
    private static Item secondCard;

    @SubscribeEvent
    public static void registerCards(RegisterEvent event) {
        event.register(Registries.ITEM, helper -> {
            firstCard = new TestCard();
            secondCard = new TestCard();
            helper.register(Myotus.makeId("widget_smoke_first"), firstCard);
            helper.register(Myotus.makeId("widget_smoke_second"), secondCard);
        });
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void runChecks() {
        TerminalModuleWidgets.register(firstCard, (screen, stack) -> new TestWidget());
        TerminalModuleWidgets.register(secondCard, (screen, stack) -> new TestWidget());
        var fixture = new Fixture();
        var first = fixture.module(firstCard);
        var second = fixture.module(secondCard);
        var mouse = new Point(5, 5);
        first.onClick = () -> check(fixture.modules.getLastClickedModuleCard() == firstCard,
                "Selection must be available before the widget callback");
        fixture.before.handled = true;
        check(fixture.container.onMouseDown(mouse, 0), "Earlier nonmodule must consume");
        check(first.calls == 0 && second.calls == 0, "Consumed click must not reach modules");
        check(fixture.modules.getLastClickedModuleCard() == null, "Consumed click must not select a module");

        fixture.before.handled = false;
        second.handled = true;
        check(fixture.container.onMouseDown(mouse, 0), "Second overlapping module must consume");
        check(fixture.modules.getLastClickedModuleCard() == secondCard, "Last dispatched module must be selected");
        check(first.calls == 1 && second.calls == 1, "Each receiver must be called exactly once");

        first.handled = true;
        check(fixture.container.onMouseDown(mouse, 0), "First module must short-circuit");
        check(fixture.modules.getLastClickedModuleCard() == firstCard && second.calls == 1,
                "Later module must not receive a consumed click");
        first.handled = false;
        second.handled = false;
        check(!fixture.container.onMouseDown(mouse, 0), "Unconsumed input must remain available to native children");
        check(fixture.modules.getLastClickedModuleCard() == secondCard, "Unconsumed modules must still be selectable");

        first.allMouseEvents = true;
        first.onClick = () -> {};
        check(!fixture.container.onMouseDown(new Point(100, 100), 0), "Outside capture must preserve return value");
        check(fixture.modules.getLastClickedModuleCard() == secondCard, "Outside capture must not change selection");
        second.visible = false;
        int calls = second.calls;
        fixture.container.onMouseDown(mouse, 0);
        check(second.calls == calls && fixture.modules.getLastClickedModuleCard() == firstCard,
                "Hidden widgets must not receive input or selection");
        fixture.update(secondCard);
        check(fixture.modules.getLastClickedModuleCard() == null, "Removing the selected card must clear selection");
        var other = new Fixture();
        other.container.onMouseDown(mouse, 0);
        check(other.modules.getLastClickedModuleCard() == secondCard
                && fixture.modules.getLastClickedModuleCard() == null, "Screen selection must stay isolated");
        Myotus.LOGGER.info("MYOTUS_WIDGET_DISPATCH_SMOKE_PASS");
    }

    @EventBusSubscriber(modid = Myotus.MODID, value = Dist.CLIENT)
    public static final class Client {
        private static boolean checked;

        @SubscribeEvent
        public static void tick(ClientTickEvent.Post event) {
            var client = Minecraft.getInstance();
            if (checked || !Boolean.getBoolean("myotus.widgetSmoke")
                    || !(client.screen instanceof TitleScreen) || client.getOverlay() != null) return;
            checked = true;
            try {
                runChecks();
                client.stop();
            } catch (Throwable failure) {
                Myotus.LOGGER.error("MYOTUS_WIDGET_DISPATCH_SMOKE_FAILED", failure);
                System.exit(1);
            }
        }
    }

    private static final class Fixture {
        private final TerminalModuleWidgets modules = new TerminalModuleWidgets();
        private final WidgetContainer container = new WidgetContainer(new ScreenStyle());
        private final TestWidget before = new TestWidget();
        private final Map<String, ICompositeWidget> widgets;

        private Fixture() {
            check(container instanceof TerminalModuleWidgets.ContainerAccess, "WidgetContainer Mixin was not applied");
            ((TerminalModuleWidgets.ContainerAccess) container).myotus$setModuleWidgets(modules);
            widgets = ((WidgetContainerAccessor) container).myotus$getCompositeWidgets();
            widgets.put("nonmodule", before);
            update(firstCard, secondCard);
        }

        private void update(Item... cards) {
            modules.update(null, java.util.Arrays.stream(cards).map(ItemStack::new).toList(),
                    widgets, new LinkedHashMap<>());
        }

        private TestWidget module(Item card) {
            return (TestWidget) widgets.values().stream().skip(card == firstCard ? 1 : 2).findFirst().orElseThrow();
        }
    }

    private static final class TestCard extends Item implements ITerminalCard {
        private TestCard() { super(new Item.Properties().stacksTo(1)); }
    }

    private static final class TestWidget implements ICompositeWidget {
        private boolean visible = true;
        private boolean handled;
        private boolean allMouseEvents;
        private int calls;
        private Runnable onClick = () -> {};
        private Rect2i bounds = new Rect2i(0, 0, 30, 30);

        @Override public void setPosition(Point position) {
            bounds = new Rect2i(position.getX(), position.getY(), bounds.getWidth(), bounds.getHeight());
        }
        @Override public void setSize(int width, int height) {
            bounds = new Rect2i(bounds.getX(), bounds.getY(), width, height);
        }
        @Override public Rect2i getBounds() { return bounds; }
        @Override public boolean isVisible() { return visible; }
        @Override public boolean wantsAllMouseDownEvents() { return allMouseEvents; }
        @Override public boolean onMouseDown(Point mouse, int button) {
            calls++;
            onClick.run();
            return handled;
        }
    }
}
