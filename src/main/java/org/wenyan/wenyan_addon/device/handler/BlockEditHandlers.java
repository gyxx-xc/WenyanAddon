package org.wenyan.wenyan_addon.device.handler;

import com.mojang.logging.LogUtils;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ResolvedArgs;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

public final class BlockEditHandlers {
    private BlockEditHandlers() {}

    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("SameParameterValue")
    private static BlockPos offsetPos(BlockPos bp, ResolvedArgs args, int offset) {
        return new BlockPos(
                (int) (bp.getX() + (double) args.get(offset)),
                (int) (bp.getY() + (double) args.get(offset + 1)),
                (int) (bp.getZ() + (double) args.get(offset + 2))
        );
    }

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> BLOCK_EDIT_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("置"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                String blockId = args.get(3);
                try {
                    Identifier id = Identifier.parse(blockId);
                    if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        return WenyanValues.of(0);
                    }
                    Block block = BuiltInRegistries.BLOCK.getValue(id);
                    boolean result = ctx.level().setBlock(pos, block.defaultBlockState(), 3);
                    return WenyanValues.of(result ? 1 : 0);
                } catch (Exception e) {
                    return WenyanValues.of(0);
                }
            }))
            .handler(ChineseUtils.bracketOf("毀"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                boolean result = ctx.level().destroyBlock(pos, true);
                return WenyanValues.of(result ? 1 : 0);
            }))
            .handler(ChineseUtils.bracketOf("替"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                String toBlockId = args.get(3);
                try {
                    Identifier id = Identifier.parse(toBlockId);
                    if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                        return WenyanValues.of(0);
                    }
                    Block targetBlock = BuiltInRegistries.BLOCK.getValue(id);
                    BlockState oldState = ctx.level().getBlockState(pos);
                    BlockState newState;
                    try {
                        newState = targetBlock.withPropertiesOf(oldState);
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Property mismatch when replacing block at {}: {}", pos, e.getMessage());
                        newState = targetBlock.defaultBlockState();
                    }
                    boolean result = ctx.level().setBlock(pos, newState, 3);
                    return WenyanValues.of(result ? 1 : 0);
                } catch (Exception e) {
                    return WenyanValues.of(0);
                }
            }))
            .build();
}
