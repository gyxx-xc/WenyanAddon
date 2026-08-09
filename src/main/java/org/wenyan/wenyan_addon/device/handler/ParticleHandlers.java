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
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ParticleHandlers {
    private static final List<SimpleParticleType> BASIC_PARTICLES = BuiltInRegistries.PARTICLE_TYPE.entrySet().stream()
            .filter(entry -> entry.getValue() instanceof SimpleParticleType)
            .map(entry -> (SimpleParticleType) entry.getValue())
            .toList();

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PARTICLE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("在指定位置释放指定编号的基本粒子")
            .handler(ChineseUtils.bracketOf("粒子放出·多态"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 3) {
                    return;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int count = (int) Math.clamp(request.args().get(1).as(WenyanDouble.TYPE).value(), 1, 20);
                int typeIndex = (int) request.args().get(2).as(WenyanDouble.TYPE).value() - 1;
                if (ctx.level() instanceof ServerLevel server) {
                    releaseBasicParticles(server, center, count, typeIndex);
                }
            }))
            .description("在指定位置释放特殊粒子（编号1物品粒子 2方块粒子）")
            .handler(ChineseUtils.bracketOf("粒子放出·易形"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 4) {
                    return;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int count = (int) Math.clamp(request.args().get(1).as(WenyanDouble.TYPE).value(), 1, 20);
                int form = (int) request.args().get(2).as(WenyanDouble.TYPE).value();
                String id = request.args().get(3).as(WenyanString.TYPE).value();
                if (ctx.level() instanceof ServerLevel server) {
                    releaseSpecialParticles(server, center, count, form, id);
                }
            }))
            .description("在指定位置生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("粒子放出"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 5) {
                    return;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int color = colorOf(request, 1, 2, 3);
                int count = (int) Math.clamp(request.args().get(4).as(WenyanDouble.TYPE).value(), 1, 20);
                if (ctx.level() instanceof ServerLevel server) {
                    scatter(server, center, count, new DustParticleOptions(color, 1.0f));
                }
            }))
            .description("在指定位置释放彩色效果粒子")
            .handler(ChineseUtils.bracketOf("粒子放出·扭"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                if (request.args().size() < 5) {
                    return;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                int color = colorOf(request, 1, 2, 3);
                int count = (int) Math.clamp(request.args().get(4).as(WenyanDouble.TYPE).value(), 1, 20);
                if (ctx.level() instanceof ServerLevel server) {
                    scatter(server, center, count, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color));
                }
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_PARTICLE_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("在指定位置释放指定编号的基本粒子")
            .handler(ChineseUtils.bracketOf("粒子放出·多态"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().size() < 3) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    int count = (int) Math.clamp(argsRequest.args().get(1).as(WenyanDouble.TYPE).value(), 1, 20);
                    int typeIndex = (int) argsRequest.args().get(2).as(WenyanDouble.TYPE).value() - 1;
                    if (entity.level() instanceof ServerLevel server) {
                        releaseBasicParticles(server, center, count, typeIndex);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置释放特殊粒子（编号1物品粒子 2方块粒子）")
            .handler(ChineseUtils.bracketOf("粒子放出·易形"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (request.args().size() < 4) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                    int count = (int) Math.clamp(request.args().get(1).as(WenyanDouble.TYPE).value(), 1, 20);
                    int form = (int) request.args().get(2).as(WenyanDouble.TYPE).value();
                    String id = request.args().get(3).as(WenyanString.TYPE).value();
                    if (entity.level() instanceof ServerLevel server) {
                        releaseSpecialParticles(server, center, count, form, id);
                    }
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置生成指定颜色的粒子效果")
            .handler(ChineseUtils.bracketOf("粒子放出"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().size() < 5) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    int color = colorOf(argsRequest, 1, 2, 3);
                    int count = (int) Math.clamp(argsRequest.args().get(4).as(WenyanDouble.TYPE).value(), 1, 20);
                    if (entity.level() instanceof ServerLevel server) {
                        scatter(server, center, count, new DustParticleOptions(color, 1.0f));
                    }
                }
                return WenyanNull.NULL;
            })
            .description("在指定位置释放彩色效果粒子")
            .handler(ChineseUtils.bracketOf("粒子放出·扭"), (ctx, argsRequest) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    if (argsRequest.args().size() < 5) {
                        return WenyanNull.NULL;
                    }
                    Vec3 center = argsRequest.args().get(0).as(WenyanVec3.TYPE).value();
                    int color = colorOf(argsRequest, 1, 2, 3);
                    int count = (int) Math.clamp(argsRequest.args().get(4).as(WenyanDouble.TYPE).value(), 1, 20);
                    if (entity.level() instanceof ServerLevel server) {
                        scatter(server, center, count, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, color));
                    }
                }
                return WenyanNull.NULL;
            })
            .build();

    private static void releaseBasicParticles(ServerLevel server, Vec3 center, int count, int typeIndex) {
        if (typeIndex < 0 || typeIndex >= BASIC_PARTICLES.size()) {
            return;
        }
        scatter(server, center, count, BASIC_PARTICLES.get(typeIndex));
    }

    private static void releaseSpecialParticles(ServerLevel server, Vec3 center, int count, int form, String id) {
        ParticleOptions options = switch (form) {
            case 1 -> itemParticleOption(id);
            case 2 -> blockParticleOption(id);
            default -> null;
        };
        if (options == null) {
            return;
        }
        scatter(server, center, count, options);
    }

    private static ParticleOptions itemParticleOption(String id) {
        try {
            Identifier identifier = Identifier.parse(id);
            if (!BuiltInRegistries.ITEM.containsKey(identifier)) {
                return null;
            }
            return new ItemParticleOption(ParticleTypes.ITEM, BuiltInRegistries.ITEM.getValue(identifier));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static ParticleOptions blockParticleOption(String id) {
        try {
            Identifier identifier = Identifier.parse(id);
            if (!BuiltInRegistries.BLOCK.containsKey(identifier)) {
                return null;
            }
            return new BlockParticleOption(ParticleTypes.BLOCK, BuiltInRegistries.BLOCK.getValue(identifier).defaultBlockState());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void scatter(ServerLevel server, Vec3 center, int count, ParticleOptions options) {
        for (int i = 0; i < count; i++) {
            double sx = center.x + server.getRandom().nextGaussian() * 0.5;
            double sy = center.y + server.getRandom().nextGaussian() * 0.5;
            double sz = center.z + server.getRandom().nextGaussian() * 0.5;
            server.sendParticles(options, sx, sy, sz, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private static int colorOf(IArgsRequest request, int rIndex, int gIndex, int bIndex) throws WenyanException.WenyanTypeException {
        int red = (int) Math.clamp(request.args().get(rIndex).as(WenyanDouble.TYPE).value(), 0, 255);
        int green = (int) Math.clamp(request.args().get(gIndex).as(WenyanDouble.TYPE).value(), 0, 255);
        int blue = (int) Math.clamp(request.args().get(bIndex).as(WenyanDouble.TYPE).value(), 0, 255);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }
}
