package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;


public class MusicHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> MUSIC_BLOCK_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置演奏指定音高的音符盒音效")
            .handler(ChineseUtils.bracketOf("奏乐"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 2) {
                    return;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int note = (int) Math.clamp(request.args().get(1).as(WenyanDouble.TYPE).value(), 0, 24);
                float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                ctx.level().playSound(null, center.x, center.y, center.z, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 3.0F, pitch);
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_MUSIC_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置演奏指定音高的音符盒音效")
            .handler(ChineseUtils.bracketOf("奏乐"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().size() < 2) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    int note = (int) Math.clamp(argsRequest.args().get(1).as(WenyanDouble.TYPE).value(), 0, 24);
                    float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                    entity.level().playSound(null, center.x, center.y, center.z, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.PLAYERS, 3.0F, pitch);
                }
                return WenyanNull.NULL;
            })
            .build();
}
