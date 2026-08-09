package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
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
            .description("在指定位置演奏指定音高和乐器的音符盒音效\n需要3个参数：位置、音高、乐器类型(0-14)")
            .handler(ChineseUtils.bracketOf("奏乐"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 3) {
                    return; // 需要3个参数：位置、音高、乐器类型
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int note = (int) Math.clamp(request.args().get(1).as(WenyanDouble.TYPE).value(), 0, 24);
                int instrumentType = (int) request.args().get(2).as(WenyanDouble.TYPE).value(); // 新增乐器参数
                float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);

                // 根据参数选择音效
                SoundEvent soundEvent = getInstrumentByType(instrumentType);

                ctx.level().playSound(null, center.x, center.y, center.z,
                        soundEvent, SoundSource.BLOCKS, 3.0F, pitch);
            }))
            .build();

    public static final Function<ItemStack, RawHandlerPackage> ITEM_MUSIC_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置演奏指定音高和乐器的音符盒音效\n需要3个参数：位置、音高、乐器类型(0-14)")
            .handler(ChineseUtils.bracketOf("奏乐"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (request.args().size() < 3) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                    int note = Math.clamp(request.args().get(1).as(WenyanInteger.TYPE).value(), 0, 24);
                    int instrumentType = request.args().get(2).as(WenyanInteger.TYPE).value();
                    float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);

                    SoundEvent soundEvent = getInstrumentByType(instrumentType);

                    entity.level().playSound(null, center.x, center.y, center.z,
                            soundEvent, SoundSource.PLAYERS, 3.0F, pitch);
                }
                return WenyanNull.NULL;
            })
            .build();

    private static SoundEvent getInstrumentByType(int type) {
        switch (type) {
            case 0:  // 竖琴（默认）
                return SoundEvents.NOTE_BLOCK_HARP.value();
            case 1:  // 贝斯
                return SoundEvents.NOTE_BLOCK_BASS.value();
            case 2:  // 小军鼓
                return SoundEvents.NOTE_BLOCK_SNARE.value();
            case 3:  // 踩镲
                return SoundEvents.NOTE_BLOCK_HAT.value();
            case 4:  // 铃铛
                return SoundEvents.NOTE_BLOCK_BELL.value();
            case 5:  // 长笛
                return SoundEvents.NOTE_BLOCK_FLUTE.value();
            case 6:  // 钟琴
                return SoundEvents.NOTE_BLOCK_CHIME.value();
            case 7:  // 吉他
                return SoundEvents.NOTE_BLOCK_GUITAR.value();
            case 8:  // 木琴
                return SoundEvents.NOTE_BLOCK_XYLOPHONE.value();
            case 9:  // 铁琴
                return SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE.value();
            case 10: // 牛铃
                return SoundEvents.NOTE_BLOCK_COW_BELL.value();
            case 11: // 迪吉里杜管
                return SoundEvents.NOTE_BLOCK_DIDGERIDOO.value();
            case 12: // 方波
                return SoundEvents.NOTE_BLOCK_BIT.value();
            case 13: // 班卓琴（1.19+）
                return SoundEvents.NOTE_BLOCK_BANJO.value();
            case 14: // 长笛（1.19+ 实际上可能是普莱尔琴）
                return SoundEvents.NOTE_BLOCK_PLING.value();
            default: // 超出范围默认返回竖琴
                return SoundEvents.NOTE_BLOCK_HARP.value();
        }
    }
}
