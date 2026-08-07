package org.wenyan.wenyan_addon.mixin;

import indi.wenyan.content.block.runner.BlockRequest;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;

@Mixin(BlockRequest.BlockContext.class)
public abstract class BlockContextMixin implements BlockContextCasterAccessor {
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
}
