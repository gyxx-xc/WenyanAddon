package org.wenyan.wenyan_addon.mixin;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.block.runner.RunnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
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

    @Redirect(method = "lambda$tick$0", at = @At(value = "NEW", target = "Lindi/wenyan/content/block/runner/BlockRequest$BlockContext;"))
    private BlockRequest.BlockContext wenyanAddon$createContext(Level level, BlockPos pos, BlockState state) {
        BlockRequest.BlockContext context = new BlockRequest.BlockContext(level, pos, state);
        ((BlockContextCasterAccessor) (Object) context).setCaster(wenyanAddonCaster);
        return context;
    }
}
