package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

public class ParticleHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PARTICLE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("粒子放出"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 5) {
                    return;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int color = colorOf(request, 1, 2, 3);
                int count = (int) Math.clamp(request.args().get(4).as(WenyanDouble.TYPE).value(), 1, 20);
                DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                if (ctx.level() instanceof ServerLevel server) {
                    for (int i = 0; i < count; i++) {
                        double sx = center.x + server.getRandom().nextGaussian() * 0.5;
                        double sy = center.y + server.getRandom().nextGaussian() * 0.5;
                        double sz = center.z + server.getRandom().nextGaussian() * 0.5;
                        server.sendParticles(dust, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                }
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_PARTICLE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("粒子放出"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().size() < 5) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    int color = colorOf(argsRequest, 1, 2, 3);
                    int count = (int) Math.clamp(argsRequest.args().get(4).as(WenyanDouble.TYPE).value(), 1, 20);
                    DustParticleOptions dust = new DustParticleOptions(color, 1.0f);
                    if (entity.level() instanceof ServerLevel server) {
                        for (int i = 0; i < count; i++) {
                            double sx = center.x + server.getRandom().nextGaussian() * 0.5;
                            double sy = center.y + server.getRandom().nextGaussian() * 0.5;
                            double sz = center.z + server.getRandom().nextGaussian() * 0.5;
                            server.sendParticles(dust, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
                        }
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

    private static int colorOf(IArgsRequest request, int rIndex, int gIndex, int bIndex) throws WenyanException.WenyanTypeException {
        int red = (int) Math.clamp(request.args().get(rIndex).as(WenyanDouble.TYPE).value(), 0, 255);
        int green = (int) Math.clamp(request.args().get(gIndex).as(WenyanDouble.TYPE).value(), 0, 255);
        int blue = (int) Math.clamp(request.args().get(bIndex).as(WenyanDouble.TYPE).value(), 0, 255);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
