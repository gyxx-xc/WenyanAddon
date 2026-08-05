package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;

public class ParticleHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PARTICLE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在符文周围生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("粒子放出"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = WenyanArgsResolver.build()
                        .int_().range(0, 255)
                        .int_().range(0, 255)
                        .int_().range(0, 255)
                        .int_().range(1, 20)
                        .resolve(request);
                int color = 0xFF000000
                        | (((int) args.get(0) & 0xFF) << 16)
                        | (((int) args.get(1) & 0xFF) << 8)
                        | ((int) args.get(2) & 0xFF);
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
