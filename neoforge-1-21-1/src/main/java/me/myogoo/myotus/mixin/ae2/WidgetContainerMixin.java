package me.myogoo.myotus.mixin.ae2;

import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.WidgetContainer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.myogoo.myotus.client.TerminalModuleWidgets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WidgetContainer.class, remap = false)
public class WidgetContainerMixin implements TerminalModuleWidgets.ContainerAccess {
    @Unique
    private TerminalModuleWidgets myotus$terminalModules;

    @Override
    public void myotus$setModuleWidgets(TerminalModuleWidgets modules) {
        myotus$terminalModules = modules;
    }

    @WrapOperation(method = "onMouseDown(Lappeng/client/Point;I)Z", at = @At(value = "INVOKE",
            target = "Lappeng/client/gui/ICompositeWidget;onMouseDown(Lappeng/client/Point;I)Z"),
            require = 1, expect = 1)
    private boolean myotus$recordModuleClick(ICompositeWidget widget, Point mouse, int button,
            Operation<Boolean> original) {
        if (myotus$terminalModules != null) {
            myotus$terminalModules.recordDispatchedClick(widget, mouse);
        }
        return original.call(widget, mouse, button);
    }
}
