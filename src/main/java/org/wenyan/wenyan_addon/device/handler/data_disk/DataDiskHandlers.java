package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.data_storage.DataDiskStorage;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.device.handler.data_disk.StorageRuneBlockEntity;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public class DataDiskHandlers {

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> STORAGE_RUNE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()

            // ====== 1. 列出已插入数据磁盘的UUID ======
            .description("列出已插入数据磁盘的UUID")
            .handler(ChineseUtils.bracketOf("列出"), BlockHandlerHelper.wrap((ctx, _) -> {
                WenyanList result = new WenyanList();
                if (ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage) {
                    for (int slot = 0; slot < storage.getDiskSlots(); slot++) {
                        storage.ensureDiskId(slot).ifPresent(id -> result.add(new WenyanString(id.toString())));
                    }
                }
                return result;
            }))
            // ====== 2. 读取数据磁盘内容 ======
            .description("读取数据磁盘内容")
            .handler(ChineseUtils.bracketOf("读取磁盘"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return WenyanBoolean.FALSE;
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty()) {
                    return WenyanBoolean.FALSE;
                }
                return DataDiskStorage.read(serverLevel, disk.get());
            }))
            // ====== 3. 写入数据磁盘内容 ======
            .description("写入数据磁盘内容")
            .handler(ChineseUtils.bracketOf("写入磁盘"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)
                        || !(ctx.level().getBlockEntity(bp) instanceof StorageRuneBlockEntity storage)) {
                    return new WenyanDouble(0);
                }
                Optional<UUID> disk = diskIdAt(storage, request);
                if (disk.isEmpty() || request.args().size() < 2) {
                    return new WenyanDouble(0);
                }
                return new WenyanDouble(DataDiskStorage.write(serverLevel, disk.get(), request.args().get(1)) ? 1 : 0);
            }))
            // ====== 4. 获取已插入的磁盘数量 ======
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
        return storage.ensureDiskId(slot);
    }
}
