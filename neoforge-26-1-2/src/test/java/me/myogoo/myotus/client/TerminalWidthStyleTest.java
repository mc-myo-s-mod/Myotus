package me.myogoo.myotus.client;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.SlotPosition;
import appeng.client.gui.style.StyleManager;
import appeng.client.gui.style.TerminalStyle;
import appeng.client.gui.style.WidgetStyle;
import appeng.core.AppEng;
import me.myogoo.myotus.api.client.ITerminalWidthScreen;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.client.gui.widgets.TerminalWidthToggleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class TerminalWidthStyleTest {
    private static final String NATIVE_PATH = "/screens/terminals/terminal.json";
    private static final String WIDE_PATH = "/screens/terminals/terminal_wide.json";

    @Test
    void validDedicatedStyleOffersAToggleAndRestoresTheExactNativeStyle() {
        var resources = mock(ResourceManager.class);
        var nativeStyle = mockScreenStyle(9, 195, 18, false);
        var wideCandidate = mockScreenStyle(13, 267, 18, false);
        TerminalWidthStyles.recordPath(nativeStyle, NATIVE_PATH);

        var wideId = AppEng.makeId(WIDE_PATH.substring(1));
        when(resources.getResource(wideId)).thenReturn(Optional.of(mock(Resource.class)));

        try (var styles = mockStatic(StyleManager.class)) {
            styles.when(() -> StyleManager.loadStyleDoc(WIDE_PATH)).thenReturn(wideCandidate);

            var wideStyle = TerminalWidthStyles.findWideStyle(nativeStyle, resources);
            var wide = wideStyle.orElseThrow();
            var preference = new WidthPreference();
            var button = TerminalWidthToggleButton.forWideStyle(preference, wideStyle).orElseThrow();

            assertSame(nativeStyle, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
            assertEquals(label("compact"), button.getMessage());

            button.onPress(null);
            assertSame(wide, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
            assertEquals(label("wide"), button.getMessage());

            button.onPress(null);
            assertSame(nativeStyle, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
            assertEquals(label("compact"), button.getMessage());

            preference.setTerminalWidthMode(TerminalWidthMode.WIDE);
            assertSame(wide, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
            assertEquals(label("wide"), button.getMessage());
        }
    }

    @Test
    void missingAndInvalidStylesKeepTheNativeLayoutAndOfferNoToggle() {
        var resources = mock(ResourceManager.class);
        var nativeStyle = mockScreenStyle(9, 195, 18, false);
        TerminalWidthStyles.recordPath(nativeStyle, NATIVE_PATH);

        var wideId = AppEng.makeId(WIDE_PATH.substring(1));
        when(resources.getResource(wideId)).thenReturn(Optional.empty());
        assertUnsupported(nativeStyle, resources);

        when(resources.getResource(wideId)).thenReturn(Optional.of(mock(Resource.class)));
        try (var styles = mockStatic(StyleManager.class)) {
            styles.when(() -> StyleManager.loadStyleDoc(WIDE_PATH))
                    .thenThrow(new RuntimeException("broken json"));
            assertUnsupported(nativeStyle, resources);

            var invalidRows = mockScreenStyle(0, 195, 18, false);
            styles.when(() -> StyleManager.loadStyleDoc(WIDE_PATH)).thenReturn(invalidRows);
            assertUnsupported(nativeStyle, resources);

            var hiddenSlotMismatch = mockScreenStyle(13, 267, 18, true);
            styles.when(() -> StyleManager.loadStyleDoc(WIDE_PATH)).thenReturn(hiddenSlotMismatch);
            assertUnsupported(nativeStyle, resources);
        }
    }

    private static ScreenStyle mockScreenStyle(int slotsPerRow, int screenWidth, int rowHeight, boolean hiddenSlot) {
        var screenStyle = mock(ScreenStyle.class);
        var terminalStyle = mock(TerminalStyle.class);
        var rowBlitter = mock(Blitter.class);
        var slot = mock(SlotPosition.class);
        when(rowBlitter.getSrcHeight()).thenReturn(rowHeight);
        when(terminalStyle.getSlotsPerRow()).thenReturn(slotsPerRow);
        when(terminalStyle.getScreenWidth()).thenReturn(screenWidth);
        when(terminalStyle.getRow()).thenReturn(rowBlitter);
        when(slot.isHidden()).thenReturn(hiddenSlot);
        when(screenStyle.getTerminalStyle()).thenReturn(terminalStyle);
        when(screenStyle.getSlots()).thenReturn(Map.of("STORAGE", slot));
        when(screenStyle.getWidget("search")).thenReturn(mock(WidgetStyle.class));
        when(screenStyle.getWidget("scrollbar")).thenReturn(mock(WidgetStyle.class));
        return screenStyle;
    }

    private static void assertUnsupported(ScreenStyle nativeStyle, ResourceManager resources) {
        var wideStyle = TerminalWidthStyles.findWideStyle(nativeStyle, resources);
        var preference = new WidthPreference();
        preference.setTerminalWidthMode(TerminalWidthMode.WIDE);
        assertTrue(wideStyle.isEmpty());
        assertTrue(TerminalWidthToggleButton.forWideStyle(preference, wideStyle).isEmpty());
        assertSame(nativeStyle, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
    }

    private static Component label(String mode) {
        return Component.translatable("gui.myotus.terminal_width",
                Component.translatable("gui.myotus.terminal_width." + mode));
    }

    private static final class WidthPreference implements ITerminalWidthScreen {
        private TerminalWidthMode mode = TerminalWidthMode.COMPACT;

        @Override
        public TerminalWidthMode getTerminalWidthMode() {
            return mode;
        }

        @Override
        public void setTerminalWidthMode(TerminalWidthMode mode) {
            this.mode = mode;
        }
    }
}
