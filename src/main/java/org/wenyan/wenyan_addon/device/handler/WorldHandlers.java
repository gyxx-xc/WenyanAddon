package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ResolvedArgs;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

@SuppressWarnings("resource")
public final class WorldHandlers {
    private WorldHandlers() {}

    private static BlockPos offsetPos(BlockPos bp, ResolvedArgs args, int offset) {
        return new BlockPos(
                (int) (bp.getX() + (double) args.get(offset)),
                (int) (bp.getY() + (double) args.get(offset + 1)),
                (int) (bp.getZ() + (double) args.get(offset + 2))
        );
    }

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> ELEMENTAL_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("水源"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                ctx.level().setBlock(offsetPos(bp, args, 0), Blocks.WATER.defaultBlockState(), 3);
            }))
            .handler(ChineseUtils.bracketOf("熔岩"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                ctx.level().setBlock(offsetPos(bp, args, 0), Blocks.LAVA.defaultBlockState(), 3);
            }))
            .handler(ChineseUtils.bracketOf("除流"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                if (!ctx.level().getBlockState(pos).getFluidState().isEmpty()) {
                    ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }))
            .handler(ChineseUtils.bracketOf("冻水成冰"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                if (ctx.level().getBlockState(pos).is(Blocks.WATER)) {
                    ctx.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                }
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> WORLD_INTERACTION_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("化生"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BoneMealItem.applyBonemeal(ItemStack.EMPTY, ctx.level(), offsetPos(bp, args, 0), null);
            }))
            .handler(ChineseUtils.bracketOf("燃"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos above = offsetPos(bp, args, 0).above();
                if (ctx.level().getBlockState(above).isAir()) {
                    ctx.level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                }
            }))
            .handler(ChineseUtils.bracketOf("滅"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                if (ctx.level().getBlockState(pos).is(Blocks.FIRE)) {
                    ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                for (Entity entity : ctx.level().getEntities(null, new AABB(pos).inflate(3.0))) {
                    entity.clearFire();
                }
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> READ_WRITE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("讀示"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockEntity be = ctx.level().getBlockEntity(offsetPos(bp, args, 0));
                if (be instanceof SignBlockEntity sign) {
                    StringBuilder sb = new StringBuilder();
                    for (Component msg : sign.getFrontText().getMessages(false)) {
                        sb.append(msg.getString());
                    }
                    return new WenyanString(sb.toString());
                }
                return WenyanNull.NULL;
            }))
            .handler(ChineseUtils.bracketOf("書示"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy()
                        .string_().string_().string_().string_()
                        .resolve(request);
                BlockPos pos = offsetPos(bp, args, 0);
                BlockEntity be = ctx.level().getBlockEntity(pos);
                if (be instanceof SignBlockEntity sign) {
                    var text = sign.getFrontText()
                            .setMessage(0, Component.literal(args.get(3)))
                            .setMessage(1, Component.literal(args.get(4)))
                            .setMessage(2, Component.literal(args.get(5)))
                            .setMessage(3, Component.literal(args.get(6)));
                    sign.setText(text, true);
                }
            }))
            .handler(ChineseUtils.bracketOf("讀講臺"), BlockHandlerHelper.wrap((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                BlockEntity be = ctx.level().getBlockEntity(offsetPos(bp, args, 0));
                if (be instanceof LecternBlockEntity lectern) {
                    ItemStack book = lectern.getBook();
                    if (!book.isEmpty()) {
                        WrittenBookContent content = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
                        if (content != null) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(content.title().get(false)).append("\n");
                            for (var page : content.pages()) {
                                sb.append(page.get(false));
                            }
                            return new WenyanString(sb.toString());
                        }
                    }
                }
                return WenyanNull.NULL;
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NOTE_BLOCK_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("奏乐"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = WenyanArgsResolver.build().double_().range(0, 24).resolve(request);
                int note = (int) Math.clamp((double) args.get(0), 0, 24);
                float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                ctx.level().playSound(null, bp, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 3.0F, pitch);
            }))
            .build();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PARTICLE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .handler(ChineseUtils.bracketOf("放塵"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = WenyanArgsResolver.build()
                        .double_().range(0, 255)
                        .double_().range(0, 255)
                        .double_().range(0, 255)
                        .double_().range(1, 20)
                        .resolve(request);
                int color = 0xFF000000 | (((int) args.get(0) & 0xFF) << 16) | (((int) args.get(1) & 0xFF) << 8) | ((int) args.get(2) & 0xFF);
                DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                if (ctx.level() instanceof ServerLevel server) {
                    for (int i = 0; i < (int) args.get(3); i++) {
                        double sx = bp.getX() + server.getRandom().nextGaussian() * 0.5;
                        double sy = bp.getY() + server.getRandom().nextGaussian() * 0.5;
                        double sz = bp.getZ() + server.getRandom().nextGaussian() * 0.5;
                        server.sendParticles(dust, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }))
            .build();
}
