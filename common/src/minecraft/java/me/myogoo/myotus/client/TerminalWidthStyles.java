package me.myogoo.myotus.client;

import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IResizableWidget;
import appeng.client.gui.widgets.TabButton;
import appeng.client.gui.style.StyleManager;
import appeng.core.AppEng;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.mixin.ae2.ScreenStyleAccessor;
import me.myogoo.myotus.mixin.ae2.WidgetContainerAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/** Resolves dedicated wide terminal styles through AE2's resource-pack loader. */
public final class TerminalWidthStyles {
    // ScreenStyle uses Object equality on all supported AE2 versions.
    private static final Map<ScreenStyle, String> PATHS = Collections.synchronizedMap(new WeakHashMap<>());

    private TerminalWidthStyles() {
    }

    public static void recordPath(ScreenStyle style, String path) {
        PATHS.put(style, path);
    }

    public static Optional<ScreenStyle> findWideStyle(ScreenStyle nativeStyle, ResourceManager resources) {
        return TerminalWidthPaths.widePath(PATHS.get(nativeStyle))
                .flatMap(path -> loadWideStyle(nativeStyle, resources, path));
    }

    private static Optional<ScreenStyle> loadWideStyle(ScreenStyle nativeStyle, ResourceManager resources, String path) {
        try {
            if (resources.getResource(AppEng.makeId(path.substring(1))).isEmpty()) {
                return Optional.empty();
            }
            var wideStyle = StyleManager.loadStyleDoc(path);
            var terminal = wideStyle.getTerminalStyle();
            if (terminal == null || terminal.getSlotsPerRow() <= 0
                    || terminal.getSlotsPerRow() > terminal.getScreenWidth() / 18 || terminal.getRow().getSrcHeight() <= 0) {
                throw new IllegalArgumentException("Invalid terminal row geometry");
            }
            var nativeSlots = nativeStyle.getSlots();
            var wideSlots = wideStyle.getSlots();
            if (!wideSlots.keySet().equals(nativeSlots.keySet())
                    || nativeSlots.entrySet().stream().anyMatch(entry ->
                            entry.getValue().isHidden() != wideSlots.get(entry.getKey()).isHidden())) {
                throw new IllegalArgumentException("Incompatible terminal slot semantics or visibility");
            }
            wideStyle.getWidget("search");
            wideStyle.getWidget("scrollbar");
            return Optional.of(wideStyle);
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(TerminalWidthStyles.class).warn("Cannot load wide terminal style {}", path, e);
            return Optional.empty();
        }
    }

    public static ScreenStyle selectStyle(ScreenStyle nativeStyle, Optional<ScreenStyle> wideStyle, TerminalWidthMode mode) {
        return mode == TerminalWidthMode.WIDE ? wideStyle.orElse(nativeStyle) : nativeStyle;
    }
    public static boolean supportsWidgets(WidgetContainer container, ScreenStyle currentStyle, ScreenStyle wideStyle) {
        var access = (WidgetContainerAccessor) container;
        copyRuntimeWidgetStyles(currentStyle, wideStyle);
        var wideWidgets = ((ScreenStyleAccessor) (Object) wideStyle).myotus$getWidgets();
        return wideWidgets.keySet().containsAll(access.myotus$getCompositeWidgets().keySet())
                && wideWidgets.keySet().containsAll(access.myotus$getWidgets().keySet());
    }

    private static void copyRuntimeWidgetStyles(ScreenStyle previous, ScreenStyle active) {
        var previousWidgets = ((ScreenStyleAccessor) (Object) previous).myotus$getWidgets();
        var activeWidgets = ((ScreenStyleAccessor) (Object) active).myotus$getWidgets();
        previousWidgets.forEach((id, widget) -> {
            if (id.equals("myotus_terminal_upgrade_panel") || id.startsWith("myotus_terminal_module_")) {
                // These IDs include the upgrade panel before it registers during AEBaseScreen.init.
                activeWidgets.putIfAbsent(id, widget);
            }
        });
    }

    public static void resizeWidgets(WidgetContainer container, ScreenStyle previous, ScreenStyle active,
            Map<AbstractWidget, WidgetSize> nativeSizes) {
        var access = (WidgetContainerAccessor) container;
        copyRuntimeWidgetStyles(previous, active);
        access.myotus$getWidgets().forEach((id, widget) -> {
            var before = previous.getWidget(id);
            var after = active.getWidget(id);
            if (before.getWidth() == after.getWidth() && before.getHeight() == after.getHeight()
                    && before.isHideEdge() == after.isHideEdge()) {
                return;
            }
            var nativeSize = nativeSizes.computeIfAbsent(widget, WidgetSize::new);
            int widgetWidth = after.getWidth() != 0 ? after.getWidth() : nativeSize.width();
            int widgetHeight = after.getHeight() != 0 ? after.getHeight() : nativeSize.height();
            if (widget instanceof IResizableWidget resizable) {
                resizable.resize(widgetWidth, widgetHeight);
                if (after.getWidth() == 0) {
                    widget.setWidth(nativeSize.width());
                }
                if (after.getHeight() == 0) {
                    widget.setHeight(nativeSize.height());
                }
            } else {
                widget.setWidth(widgetWidth);
                widget.setHeight(widgetHeight);
            }
            if (widget instanceof TabButton tab && before.isHideEdge() != after.isHideEdge()) {
                tab.setStyle(after.isHideEdge() ? TabButton.Style.CORNER : nativeSize.edgeStyle());
            }
        });
        access.myotus$getCompositeWidgets().forEach((id, widget) -> {
            var before = previous.getWidget(id);
            var after = active.getWidget(id);
            if (before.getWidth() != after.getWidth() || before.getHeight() != after.getHeight()) {
                widget.setSize(after.getWidth(), after.getHeight());
            }
        });
    }

    public record WidgetSize(int width, int height, TabButton.Style edgeStyle) {
        WidgetSize(AbstractWidget widget) {
            this(widget.getWidth(), widget.getHeight(),
                    widget instanceof TabButton tab && tab.getStyle() != TabButton.Style.CORNER
                            ? tab.getStyle() : TabButton.Style.BOX);
        }
    }
}
