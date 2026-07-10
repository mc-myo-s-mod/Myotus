package me.myogoo.myotus.client.gui.widgets.button;

import com.mojang.blaze3d.platform.InputConstants;
import org.junit.jupiter.api.Test;

import static me.myogoo.myotus.client.gui.widgets.button.MyoCycleButton.MouseButton.LEFT;
import static me.myogoo.myotus.client.gui.widgets.button.MyoCycleButton.MouseButton.RIGHT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyoCycleButtonTest {
    @Test
    void distinguishesRawAndAe2ForwardedMouseButtons() {
        assertEquals(LEFT, MyoCycleButton.resolveMouseButton(InputConstants.MOUSE_BUTTON_LEFT, false));
        assertEquals(RIGHT, MyoCycleButton.resolveMouseButton(InputConstants.MOUSE_BUTTON_RIGHT, false));
        assertEquals(RIGHT, MyoCycleButton.resolveMouseButton(InputConstants.MOUSE_BUTTON_LEFT, true));
        assertNull(MyoCycleButton.resolveMouseButton(2, false));
    }
}
