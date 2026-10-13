package me.myogoo.myotus.client.gui.widgets.button;

import net.minecraft.client.gui.GuiGraphics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class CustomImageButtonTest {
    @Test
    void fullSizeIsRestoredAfterRenderingHalfSize() {
        var button = new CustomImageButton(ignored -> {});
        button.setDisableBackground(true);
        var graphics = mock(GuiGraphics.class);
        for (boolean halfSize : new boolean[] {false, true, false}) {
            button.setHalfSize(halfSize);
            button.renderWidget(graphics, 0, 0, 0);
            int expected = halfSize ? 8 : 16;
            assertEquals(expected, button.getWidth());
            assertEquals(expected, button.getHeight());
            assertEquals(expected, button.getTooltipArea().getWidth());
            assertEquals(expected, button.getTooltipArea().getHeight());
        }
    }
}
