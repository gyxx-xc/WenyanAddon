package org.wenyan.wenyan_addon.qi.liquid;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;

/**
 * 灵液纯化方块：放入灵液水瓶后自动纯化为灵气恢复药水。
 */
public class QiLiquidPurifierBlock extends Block implements EntityBlock {
    public QiLiquidPurifierBlock(Properties properties) {
        super(properties
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(2.0f)
                .sound(SoundType.GLASS));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new QiLiquidPurifierBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof QiLiquidPurifierBlockEntity purifier)) {
            return InteractionResult.PASS;
        }
        // 放入灵液水瓶
        if (!stack.isEmpty() && stack.is(org.wenyan.wenyan_addon.WenyanAddon.QI_LIQUID_BOTTLE_ITEM.get())) {
            if (purifier.start(stack)) {
                stack.shrink(1);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(Component.literal("灵液纯化开始"));
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (purifier.isProcessing() && player instanceof ServerPlayer serverPlayer) {
            ElementAttribute attr = purifier.pendingAttribute();
            serverPlayer.sendSystemMessage(Component.literal(
                    "正在纯化" + (attr != null ? attr.displayName() : "") + "灵液，请稍候"));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level, @NonNull BlockState state,
                                                                             @NonNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (entityLevel, pos, blockState, blockEntity) -> {
            if (blockEntity instanceof QiLiquidPurifierBlockEntity purifier) {
                purifier.tick((ServerLevel) entityLevel);
            }
        };
    }
}
