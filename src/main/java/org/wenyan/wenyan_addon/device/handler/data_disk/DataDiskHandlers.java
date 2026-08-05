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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.device.handler.data_disk.StorageRuneBlockEntity;
import org.wenyan.wenyan_addon.data_storage.DataDiskStorage;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class DataDiskHandlers {
    // 参数规范：坐标偏移（3个double）+ 半径（1个double），范围0-16
    public static final ArgsSpecBuilder.Step<?> storageRuneArgsSpec = BlockHandlerHelper.singleVec3ArgsSpec.copy().double_().range(0, 16).dummy();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> STORAGE_RUNE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()

            // ====== 1. 列出已插入数据磁盘的UUID ======
            .description("列出已插入数据磁盘的UUID")
            .handler(ChineseUtils.bracketOf("列出"), BlockHandlerHelper.wrap((ctx, _) -> {
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

            // ====== 2. 读取数据磁盘指定键 ======
            .description("读取数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("读取磁盘键"), BlockHandlerHelper.wrap((ctx, request) -> {
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

            // ====== 3. 写入数据磁盘指定键 ======
            .description("写入数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("写入磁盘键"), BlockHandlerHelper.wrap((ctx, request) -> {
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

            // ====== 4. 删除数据磁盘指定键 ======
            .description("删除数据磁盘指定键")
            .handler(ChineseUtils.bracketOf("删除磁盘键"), BlockHandlerHelper.wrap((ctx, request) -> {
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

            // ====== 5. 列出数据磁盘中的所有键 ======
            .description("列出数据磁盘中的所有键")
            .handler(ChineseUtils.bracketOf("磁盘键"), BlockHandlerHelper.wrap((ctx, request) -> {
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

            // ====== 6. 获取磁盘数量（槽位使用情况） ======
            .description("获取已插入的磁盘数量")
            .handler(ChineseUtils.bracketOf("磁盘数"), BlockHandlerHelper.wrap((ctx, _) -> {
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    int count = 0;
                    for (int slot = 0; slot < storage.getDiskSlots(); slot++) {
                        if (!storage.getDisk(slot).isEmpty()) {
                            count++;
                        }
                    }
                    return new WenyanDouble(count);
                }
                return new WenyanDouble(0);
            }))

            .build();

    // ====== 辅助方法：根据请求参数获取磁盘UUID ======
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