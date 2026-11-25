package me.myogoo.myotus.mixin.ae2;

import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = WidgetContainer.class, remap = false)
public interface WidgetContainerAccessor {
    @Mutable
    @Accessor("style")
    void myotus$setStyle(ScreenStyle style);

    @Accessor("widgets")
    Map<String, AbstractWidget> myotus$getWidgets();

    @Accessor("compositeWidgets")
    Map<String, ICompositeWidget> myotus$getCompositeWidgets();
}
