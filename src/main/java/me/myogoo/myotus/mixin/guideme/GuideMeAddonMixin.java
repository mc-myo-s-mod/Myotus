package me.myogoo.myotus.mixin.guideme;

import appeng.core.AppEngClient;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import guideme.Guide;
import guideme.GuideBuilder;
import guideme.compiler.TagCompiler;
import me.myogoo.myotus.integration.guideme.ConditionTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AppEngClient.class, remap = false)
public class GuideMeAddonMixin {
    @WrapOperation(method = "createGuide", at = @At(value = "INVOKE", target = "Lguideme/GuideBuilder;build()Lguideme/Guide;"), remap = false)
    private Guide et$createGuide(GuideBuilder instance, Operation<Guide> original) {
        instance.extension(TagCompiler.EXTENSION_POINT, new ConditionTag());
        return original.call(instance);
    }
}
