package org.wenyan.wenyan_addon.mixin;

import indi.wenyan.content.block.runner.RunnerBlockEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.wenyan.wenyan_addon.mixin_util.CasterContext;
import org.wenyan.wenyan_addon.mixin_util.RunnerBlockEntityCasterAccessor;

@Mixin(RunnerBlockEntity.class)
public abstract class RunnerBlockEntityMixin implements RunnerBlockEntityCasterAccessor {
    @Unique
    private ServerPlayer wenyanAddonCaster;

    @Override
    public ServerPlayer getCaster() {
        return wenyanAddonCaster;
    }

    @Override
    public void setCaster(ServerPlayer caster) {
        this.wenyanAddonCaster = caster;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void wenyanAddon$enterTick(CallbackInfo ci) {
        CasterContext.set(wenyanAddonCaster);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void wenyanAddon$exitTick(CallbackInfo ci) {
        CasterContext.clear();
    }
}
