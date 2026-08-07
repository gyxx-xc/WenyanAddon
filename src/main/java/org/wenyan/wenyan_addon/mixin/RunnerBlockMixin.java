package org.wenyan.wenyan_addon.mixin;

import indi.wenyan.content.block.runner.RunnerBlock;
import indi.wenyan.content.block.runner.RunnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.wenyan.wenyan_addon.mixin_util.RunnerBlockEntityCasterAccessor;

@Mixin(RunnerBlock.class)
public abstract class RunnerBlockMixin {
    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void wenyanAddon$setCaster(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof ServerPlayer sp
                && level.getBlockEntity(pos) instanceof RunnerBlockEntity runner) {
            ((RunnerBlockEntityCasterAccessor) runner).setCaster(sp);
        }
    }
}
