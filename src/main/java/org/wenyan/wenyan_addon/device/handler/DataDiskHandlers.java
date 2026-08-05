package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.StorageRuneBlockEntity;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.storage.DataDiskStorage;
import org.wenyan.wenyan_addon.storage.WorldSnapshotMapper;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


public class DataDiskHandlers {
    public static final ArgsSpecBuilder.Step<?> storageRuneArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy().double_().range(0, 16).dummy();
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> STORAGE_RUNE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("将指定范围内的物品收入存储符文")
            .handler(ChineseUtils.bracketOf("收纳"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    var args = storageRuneArgsSpec.resolve(request);
                    BlockPos pos = new BlockPos(
                            (int) (bp.getX() + (double) args.get(0)),
                            (int) (bp.getY() + (double) args.get(1)),
                            (int) (bp.getZ() + (double) args.get(2))
                    );
                    double radius = Math.clamp(args.get(3), 0.0, 16.0);
                    int absorbed = BlockHandlerHelper.absorbItems(ctx.level(), storage, pos, radius);
                    return new WenyanDouble(absorbed);
                }
                return new WenyanDouble(0);
            }))
            .description("从存储符文中吐出物品到指定位置")
            .handler(ChineseUtils.bracketOf("吐出"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    var args = storageRuneArgsSpec.resolve(request);
                    BlockPos pos = new BlockPos(
                            (int) (bp.getX() + (double) args.get(0)),
                            (int) (bp.getY() + (double) args.get(1)),
                            (int) (bp.getZ() + (double) args.get(2))
                    );
                    int count = (int) Math.clamp(args.get(3), 0.0, 2304.0);
                    ItemStack extracted = storage.extractAny(count);
                    if (!extracted.isEmpty()) {
                        ctx.level().addFreshEntity(new ItemEntity(ctx.level(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, extracted));
                    }
                    return new WenyanDouble(extracted.getCount());
                }
                return new WenyanDouble(0);
            }))
            .description("查询存储符文中的物品储量")
            .handler(ChineseUtils.bracketOf("藏量"), BlockHandlerHelper.wrap((ctx, _) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    return new WenyanDouble(storage.getStoredCount());
                }
                return new WenyanDouble(0);
            }))
            .description("读取指定位置附近实体的数据为图")
            .handler(ChineseUtils.bracketOf("讀實"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)) {
                    return WenyanNull.NULL;
                }
                var args = storageRuneArgsSpec.resolve(request);
                Vec3 center = new Vec3(
                        bp.getX() + (double) args.get(0),
                        bp.getY() + (double) args.get(1),
                        bp.getZ() + (double) args.get(2)
                );
                double radius = Math.clamp(args.get(3), 0.0, 16.0);
                Entity target = serverLevel.getEntities(null, new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius)))
                        .stream()
                        .filter(entity -> !(entity instanceof ItemEntity))
                        .min(Comparator.comparingDouble(a -> a.distanceToSqr(center)))
                        .orElse(null);
                return target == null ? WenyanNull.NULL : WorldSnapshotMapper.entity(target);
            }))
            .description("读取指定位置方块和方块实体的数据为图")
            .handler(ChineseUtils.bracketOf("讀方"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)) {
                    return WenyanNull.NULL;
                }
                var args = BlockHandlerHelper.singleVec3ArgsSpec.resolve(request);
                return WorldSnapshotMapper.block(serverLevel, new BlockPos(
                        (int) (bp.getX() + (double) args.get(0)),
                        (int) (bp.getY() + (double) args.get(1)),
                        (int) (bp.getZ() + (double) args.get(2))
                ));
            }))
            .description("列出已插入数据磁盘的UUID")
            .handler(ChineseUtils.bracketOf("盤列"), BlockHandlerHelper.wrap((ctx, _) -> {
                WenyanList result = new WenyanList();
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    for (int slot = 0; slot < storage.getDiskSlots(); slot++) {
                        ItemStack disk = storage.getDisk(slot);
                        if (!disk.isEmpty()) {
                            result.add(new WenyanString(DataDiskStorage.getOrCreateDiskId(disk).toString()));
                            storage.setChanged();
                        }
                    }
                }
                return result;
            }))
            .description("读取数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("盤讀"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return WenyanNull.NULL;
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                return DataDiskStorage.readKey(serverLevel, disk.get(), request.args().get(1).as(WenyanString.TYPE).value());
            }))
            .description("写入数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("盤寫"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return new WenyanDouble(0);
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 3) {
                    return new WenyanDouble(0);
                }
                String key = request.args().get(1).as(WenyanString.TYPE).value();
                return new WenyanDouble(DataDiskStorage.writeKey(serverLevel, disk.get(), key, request.args().get(2)) ? 1 : 0);
            }))
            .description("删除数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("盤刪"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return WenyanNull.NULL;
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                return DataDiskStorage.deleteKey(serverLevel, disk.get(), request.args().get(1).as(WenyanString.TYPE).value());
            }))
            .description("列出数据磁盘中的键")
            .handler(ChineseUtils.bracketOf("盤鍵"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return new WenyanList();
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty()) {
                    return new WenyanList();
                }
                return DataDiskStorage.read(serverLevel, disk.get()).getAttribute("鍵");
            }))
            .build();
    private static Optional<UUID> diskIdAt(StorageRuneBlockEntity storage, BlockRequest request) throws WenyanException.WenyanTypeException {
        if (request.args().isEmpty()) {
            return Optional.empty();
        }
        int slot = (int) request.args().getFirst().as(WenyanDouble.TYPE).value() - 1;
        ItemStack disk = storage.getDisk(slot);
        if (disk.isEmpty()) {
            return Optional.empty();
        }
        UUID diskId = DataDiskStorage.getOrCreateDiskId(disk);
        storage.setChanged();
        return Optional.of(diskId);
    }
}
