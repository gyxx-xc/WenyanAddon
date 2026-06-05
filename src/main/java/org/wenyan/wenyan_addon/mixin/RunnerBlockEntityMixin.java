package org.wenyan.wenyan_addon.mixin;

import indi.wenyan.content.block.runner.RunnerBlockEntity;
import indi.wenyan.judou.api.values.WenyanPackage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wenyan.wenyan_addon.Capabilities;

@Mixin(value = RunnerBlockEntity.class, remap = false)
public abstract class RunnerBlockEntityMixin {
    @Inject(method = "initEnvironment", at = @At("RETURN"))
    private void wenyanAddon$injectDyeGlobals(CallbackInfoReturnable<WenyanPackage> cir) {
        Capabilities.injectDyeGlobals(cir.getReturnValue());
    }
}
