package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author qq240
 * @version 1.0
 * @className MusicHandlers
 * @Description TODO
 * @date 2026/8/5 13:31
 */
public class MusicHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> MUSIC_BLOCK_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("演奏指定音高的音符盒音效")
            .handler(ChineseUtils.bracketOf("奏乐"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = WenyanArgsResolver.build().double_().range(0, 24).resolve(request);
                int note = (int) Math.clamp((double) args.get(0), 0, 24);
                float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                ctx.level().playSound(null, bp, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.BLOCKS, 3.0F, pitch);
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_MUSIC_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("演奏指定音高的音符盒音效")
            .handler(ChineseUtils.bracketOf("奏乐"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = WenyanArgsResolver.build().double_().range(0, 24).resolve(argsRequest);
                    int note = (int) Math.clamp((double) args.get(0), 0, 24);
                    float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
                    entity.level().playSound(null, entity, SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.PLAYERS, 3.0F, pitch);
                }
                return WenyanNull.NULL;
            })
            .build();
}
