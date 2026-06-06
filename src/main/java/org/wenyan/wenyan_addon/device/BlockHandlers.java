package org.wenyan.wenyan_addon.device;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.StorageRuneBlockEntity;

@SuppressWarnings("resource")
public enum BlockHandlers {
    ;

    public static final RawHandlerPackage PROJECTILE_SPAWNER_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("箭"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 dir = BlockHandlerHelper.directionVec(args, 0);
                    Arrow arrow = new Arrow(ctx.level(), ctx.pos().getX() + 0.5, ctx.pos().getY(), ctx.pos().getZ() + 0.5, ItemStack.EMPTY, null);
                    arrow.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                    ctx.level().addFreshEntity(arrow);
                }))
                .handler(ChineseUtils.bracketOf("煙火"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 dir = BlockHandlerHelper.directionVec(args, 0);
                    FireworkRocketEntity firework = new FireworkRocketEntity(ctx.level(), ctx.pos().getX() + 0.5, ctx.pos().getY(), ctx.pos().getZ() + 0.5,
                            new ItemStack(Items.FIREWORK_ROCKET));
                    firework.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                    ctx.level().addFreshEntity(firework);
                }))
                .handler(ChineseUtils.bracketOf("雪丸"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 dir = BlockHandlerHelper.directionVec(args, 0);
                    Snowball snowball = new Snowball(ctx.level(), ctx.pos().getX() + 0.5, ctx.pos().getY(), ctx.pos().getZ() + 0.5, ItemStack.EMPTY);
                    snowball.shoot(dir.x, dir.y, dir.z, 0.6f, 10.0f);
                    ctx.level().addFreshEntity(snowball);
                }))
                .handler(ChineseUtils.bracketOf("火丸"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 dir = BlockHandlerHelper.directionVec(args, 0);
                    SmallFireball fireball = new SmallFireball(ctx.level(), ctx.pos().getX() + 0.5, ctx.pos().getY(), ctx.pos().getZ() + 0.5, dir);
                    ctx.level().addFreshEntity(fireball);
                }))
                .build();

    public static final RawHandlerPackage ELEMENTAL_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("水源"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    ctx.level().setBlock(BlockHandlerHelper.blockPos(args, 0, ctx.pos()), Blocks.WATER.defaultBlockState(), 3);
                }))
                .handler(ChineseUtils.bracketOf("熔岩"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    ctx.level().setBlock(BlockHandlerHelper.blockPos(args, 0, ctx.pos()), Blocks.LAVA.defaultBlockState(), 3);
                }))
                .handler(ChineseUtils.bracketOf("除流"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    BlockPos pos = BlockHandlerHelper.blockPos(args, 0, ctx.pos());
                    if (!ctx.level().getBlockState(pos).getFluidState().isEmpty()) {
                        ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }))
                .handler(ChineseUtils.bracketOf("冻水成冰"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    BlockPos pos = BlockHandlerHelper.blockPos(args, 0, ctx.pos());
                    if (ctx.level().getBlockState(pos).is(Blocks.WATER)) {
                        ctx.level().setBlock(pos, Blocks.ICE.defaultBlockState(), 3);
                    }
                }))
                .build();

    public static final RawHandlerPackage WORLD_INTERACTION_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("化生"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    BoneMealItem.applyBonemeal(ItemStack.EMPTY, ctx.level(), BlockHandlerHelper.blockPos(args, 0, ctx.pos()), null);
                }))
                .handler(ChineseUtils.bracketOf("燃"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    BlockPos above = BlockHandlerHelper.blockPos(args, 0, ctx.pos()).above();
                    if (ctx.level().getBlockState(above).isAir()) {
                        ctx.level().setBlock(above, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }))
                .handler(ChineseUtils.bracketOf("滅"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    BlockPos pos = BlockHandlerHelper.blockPos(args, 0, ctx.pos());
                    if (ctx.level().getBlockState(pos).is(Blocks.FIRE)) {
                        ctx.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }
                    for (Entity entity : ctx.level().getEntities(null, new AABB(pos).inflate(3.0))) {
                        entity.clearFire();
                    }
                }))
                .build();

    public static final RawHandlerPackage READ_WRITE_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("讀示"), BlockHandlerHelper.wrap((ctx, args) -> {
                    BlockEntity be = ctx.level().getBlockEntity(BlockHandlerHelper.blockPos(args, 0, ctx.pos()));
                    if (be instanceof SignBlockEntity sign) {
                        StringBuilder sb = new StringBuilder();
                        for (Component msg : sign.getFrontText().getMessages(false)) {
                            sb.append(msg.getString());
                        }
                        return new WenyanString(sb.toString());
                    }
                    return WenyanNull.NULL;
                }))
                .handler(ChineseUtils.bracketOf("書示"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    BlockPos pos = BlockHandlerHelper.blockPos(args, 0, ctx.pos());
                    String line1 = BlockHandlerHelper.argString(args, 3);
                    String line2 = BlockHandlerHelper.argString(args, 4);
                    String line3 = BlockHandlerHelper.argString(args, 5);
                    String line4 = BlockHandlerHelper.argString(args, 6);
                    BlockEntity be = ctx.level().getBlockEntity(pos);
                    if (be instanceof SignBlockEntity sign) {
                        var text = sign.getFrontText()
                                .setMessage(0, Component.literal(line1))
                                .setMessage(1, Component.literal(line2))
                                .setMessage(2, Component.literal(line3))
                                .setMessage(3, Component.literal(line4));
                        sign.setText(text, true);
                    }
                }))
                .handler(ChineseUtils.bracketOf("讀講臺"), BlockHandlerHelper.wrap((ctx, args) -> {
                    BlockEntity be = ctx.level().getBlockEntity(BlockHandlerHelper.blockPos(args, 0, ctx.pos()));
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

    public static final RawHandlerPackage NAMING_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("命名"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    String name = BlockHandlerHelper.argString(args, 3);
                    for (Entity entity : ctx.level().getEntities(null, BlockHandlerHelper.searchAABB(args, 0, ctx.pos()))) {
                        entity.setCustomName(Component.literal(name));
                    }
                }))
                .build();

    public static final RawHandlerPackage ENTITY_MANIPULATION_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("传送"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 dest = BlockHandlerHelper.targetPos(args, 3, ctx.pos());
                    for (Entity entity : ctx.level().getEntities(null, BlockHandlerHelper.searchAABB(args, 0, ctx.pos()))) {
                        entity.teleportTo(dest.x, dest.y, dest.z);
                    }
                }))
                .handler(ChineseUtils.bracketOf("閃"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 origin = BlockHandlerHelper.targetPos(args, 0, ctx.pos());
                    Vec3 delta = BlockHandlerHelper.directionVec(args, 3);
                    for (Entity entity : ctx.level().getEntities(null, BlockHandlerHelper.searchAABB(args, 0, ctx.pos()))) {
                        entity.teleportTo(origin.x + delta.x, origin.y + delta.y, origin.z + delta.z);
                    }
                }))
                .handler(ChineseUtils.bracketOf("施力"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    Vec3 force = BlockHandlerHelper.directionVec(args, 3);
                    for (Entity entity : ctx.level().getEntities(null, BlockHandlerHelper.searchAABB(args, 0, ctx.pos()))) {
                        entity.addDeltaMovement(force);
                    }
                }))
                .build();

    public static final RawHandlerPackage NOTE_BLOCK_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("奏乐"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    int note = Math.clamp(BlockHandlerHelper.argInt(args, 0), 0, 24);
                    float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                    ctx.level().playSound(null, ctx.pos(), SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 3.0F, pitch);
                }))
                .build();

    public static final RawHandlerPackage PARTICLE_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("放塵"), BlockHandlerHelper.wrapVoid((ctx, args) -> {
                    double px = ctx.pos().getX();
                    double py = ctx.pos().getY();
                    double pz = ctx.pos().getZ();
                    int r = BlockHandlerHelper.clampInt(args, 0, 0, 255);
                    int g = BlockHandlerHelper.clampInt(args, 1, 0, 255);
                    int b = BlockHandlerHelper.clampInt(args, 2, 0, 255);
                    int count = BlockHandlerHelper.clampInt(args, 3, 1, 100);
                    int color = 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
                    DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                    if (ctx.level() instanceof ServerLevel server) {
                        for (int i = 0; i < count; i++) {
                            double sx = px + server.getRandom().nextGaussian() * 0.5;
                            double sy = py + server.getRandom().nextGaussian() * 0.5;
                            double sz = pz + server.getRandom().nextGaussian() * 0.5;
                            server.sendParticles(dust, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                }))
                .build();

    public static final RawHandlerPackage STORAGE_RUNE_PACKAGE = HandlerPackageBuilder.create()
                .handler(ChineseUtils.bracketOf("收纳"), BlockHandlerHelper.wrap((ctx, args) -> {
                    if (ctx.level().getBlockEntity(ctx.pos()) instanceof StorageRuneBlockEntity storage) {
                        BlockPos pos = BlockHandlerHelper.blockPos(args, 0, ctx.pos());
                        double radius = Math.clamp(BlockHandlerHelper.argDouble(args, 3), 0.0, 16.0);
                        int absorbed = BlockHandlerHelper.absorbItems(ctx.level(), storage, pos, radius);
                        return new WenyanDouble(absorbed);
                    }
                    return new WenyanDouble(0);
                }))
                .handler(ChineseUtils.bracketOf("吐出"), BlockHandlerHelper.wrap((ctx, args) -> {
                    if (ctx.level().getBlockEntity(ctx.pos()) instanceof StorageRuneBlockEntity storage) {
                        BlockPos pos = BlockHandlerHelper.blockPos(args, 0, ctx.pos());
                        int count = (int) Math.clamp(BlockHandlerHelper.argDouble(args, 3), 0.0, 2304.0);
                        ItemStack extracted = storage.extractAny(count);
                        if (!extracted.isEmpty()) {
                            ctx.level().addFreshEntity(new ItemEntity(ctx.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extracted));
                        }
                        return new WenyanDouble(extracted.getCount());
                    }
                    return new WenyanDouble(0);
                }))
                .handler(ChineseUtils.bracketOf("藏量"), BlockHandlerHelper.wrap((ctx, args) -> {
                    if (ctx.level().getBlockEntity(ctx.pos()) instanceof StorageRuneBlockEntity storage) {
                        return new WenyanDouble(storage.getStoredCount());
                    }
                    return new WenyanDouble(0);
                }))
                .build();
}
