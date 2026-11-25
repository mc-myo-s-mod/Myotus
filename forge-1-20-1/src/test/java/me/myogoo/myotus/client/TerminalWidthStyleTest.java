package me.myogoo.myotus.client;

import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.StyleManager;
import appeng.core.AppEng;
import me.myogoo.myotus.api.client.ITerminalWidthScreen;
import me.myogoo.myotus.api.client.TerminalWidthMode;
import me.myogoo.myotus.client.gui.widgets.TerminalWidthToggleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TerminalWidthStyleTest {
    private static final String NATIVE_PATH = "/screens/terminals/terminal.json";
    private static final String WIDE_PATH = "/screens/terminals/terminal_wide.json";
    private final ResourceManager resources = mock(ResourceManager.class);

    @BeforeEach
    void loadStylesFromTheTestResourceManager() throws Exception {
        var resource = mock(Resource.class);
        when(resource.sourcePackId()).thenReturn("test");
        when(resources.getResource(any())).thenAnswer(invocation -> {
            var id = invocation.getArgument(0);
            return getClass().getResource("/assets/" + id.toString().replace(':', '/')) == null
                    ? Optional.empty() : Optional.of(resource);
        });
        when(resources.openAsReader(any())).thenAnswer(invocation -> {
            var id = invocation.getArgument(0);
            var input = getClass().getResourceAsStream("/assets/" + id.toString().replace(':', '/'));
            return new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        });
        StyleManager.initialize(resources);
    }

    @AfterEach
    void clearStyleCache() {
        StyleManager.initialize(null);
    }

    @Test
    void validDedicatedStyleOffersAToggleAndRestoresTheExactNativeStyle() throws Exception {
        var nativeStyle = loadNativeStyle();
        provideWideDocument("""
                {
                  "includes": ["terminal.json"],
                  "terminalStyle": {
                    "slotsPerRow": 13,
                    "row": { "texture": "guis/terminal.png", "textureWidth": 267, "srcRect": [0, 35, 267, 18] }
                  }
                }
                """);
        var wideStyle = TerminalWidthStyles.findWideStyle(nativeStyle, resources);
        var wide = wideStyle.orElseThrow();
        var preference = new WidthPreference();
        var button = TerminalWidthToggleButton.forWideStyle(preference, wideStyle).orElseThrow();

        assertSame(nativeStyle, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
        assertEquals(9, nativeStyle.getTerminalStyle().getSlotsPerRow());
        assertEquals(label("compact"), button.getMessage());

        button.onPress();
        assertSame(wide, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
        assertEquals(13, wide.getTerminalStyle().getSlotsPerRow());
        assertEquals(267, wide.getTerminalStyle().getScreenWidth());
        assertEquals(label("wide"), button.getMessage());

        button.onPress();
        assertSame(nativeStyle, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
        assertEquals(label("compact"), button.getMessage());

        // The preference can change while the terminal is covered by a settings screen.
        preference.setTerminalWidthMode(TerminalWidthMode.WIDE);
        assertSame(wide, TerminalWidthStyles.selectStyle(nativeStyle, wideStyle, preference.mode));
        assertEquals(label("wide"), button.getMessage());
    }

    @Test
    void missingAndInvalidStylesKeepTheNativeLayoutAndOfferNoToggle() throws Exception {
        var nativeStyle = loadNativeStyle();
        assertUnsupported(nativeStyle);

        provideWideDocument("{ broken json");
        assertUnsupported(nativeStyle);

        provideWideDocument("""
                { "includes": ["terminal.json"], "terminalStyle": { "slotsPerRow": 0 } }
                """);
        assertUnsupported(nativeStyle);
    }

    private ScreenStyle loadNativeStyle() {
        var style = StyleManager.loadStyleDoc(NATIVE_PATH);
        TerminalWidthStyles.recordPath(style, NATIVE_PATH);
        return style;
    }

    private void provideWideDocument(String json) throws Exception {
        var id = AppEng.makeId(WIDE_PATH.substring(1));
        var resource = mock(Resource.class);
        when(resource.sourcePackId()).thenReturn("test");
        doReturn(Optional.of(resource)).when(resources).getResource(id);
        doAnswer(invocation -> new BufferedReader(new StringReader(json)))
                .when(resources).openAsReader(id);
        StyleManager.initialize(resources);
    }

    private void assertUnsupported(ScreenStyle nativeStyle) {
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
