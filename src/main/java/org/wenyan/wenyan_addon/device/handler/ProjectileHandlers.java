package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.args.ArgsSpecBuilder;
import indi.wenyan.interpreter_impl.args.WenyanArgsResolver;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanBoolean;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.wenyan.wenyan_addon.device.handler.HandlerUtils.lampToRangeByBiFunction;
import static org.wenyan.wenyan_addon.device.handler.HandlerUtils.lampToRangeByFunction;


public class ProjectileHandlers {


    private static IntList rgbListToColorsByBiFunction(List<IWenyanValue> rgbList) throws WenyanException.WenyanTypeException {
        IntList colors = new IntArrayList();
        if (rgbList == null || rgbList.isEmpty()) {
            return colors;
        }
        for (int i = 0; i < rgbList.size(); i += 3) {
            double r = rgbList.get(i).as(WenyanDouble.TYPE).value();
            double g = rgbList.get(i + 1).as(WenyanDouble.TYPE).value();
            double b = rgbList.get(i + 2).as(WenyanDouble.TYPE).value();
            colors.add(((int) r << 16) | ((int) g << 8) | (int) b);
        }
        return colors;
    }
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> PROJECTILE_SPAWNER_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("生成箭矢")
            .handler(ChineseUtils.bracketOf("箭"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                Vec3 target = lampToRangeByBiFunction(bp, args.get(0).as(WenyanVec3.TYPE).value());
                Arrow arrow = new Arrow(ctx.level(), target.x, target.y, target.z,
                        new ItemStack(Items.ARROW), null);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                ctx.level().addFreshEntity(arrow);
            }))
            .description("发射烟花火箭")
            .handler(ChineseUtils.bracketOf("煙火"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                int shapeId = (int) args.get(0).as(WenyanDouble.TYPE).value();
                FireworkExplosion.Shape shape = switch (shapeId) {
                    case 1 -> FireworkExplosion.Shape.SMALL_BALL;
                    case 2 -> FireworkExplosion.Shape.STAR;
                    case 3 -> FireworkExplosion.Shape.CREEPER;
                    case 4 -> FireworkExplosion.Shape.BURST;
                    default -> FireworkExplosion.Shape.LARGE_BALL;
                };

                List<IWenyanValue> arg_colors = args.get(1).as(WenyanList.TYPE).value();
                if (arg_colors.isEmpty()) {
                    throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                } else if (arg_colors.size() % 3 != 0) {
                    throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                }
                for (IWenyanValue v : arg_colors) {
                    v.as(WenyanDouble.TYPE);
                }
                List<IWenyanValue> arg_fadeColors = args.get(2).as(WenyanList.TYPE).value();
                if (arg_fadeColors.isEmpty()) {
                    throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                } else if (arg_fadeColors.size() % 3 != 0) {
                    throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                }
                for (IWenyanValue v : arg_fadeColors) {
                    v.as(WenyanDouble.TYPE);
                }
                boolean hasTrail = args.get(3).as(WenyanBoolean.TYPE).value();
                boolean hasTwinkle = args.get(4).as(WenyanBoolean.TYPE).value();
                int flightDuration = Math.clamp((int) Math.round(args.get(5).as(WenyanDouble.TYPE).value()), 1, 3);

                Vec3 target = lampToRangeByBiFunction(bp, args.get(6).as(WenyanVec3.TYPE).value());
                ItemStack fireworkItem = new ItemStack(Items.FIREWORK_ROCKET);

                FireworkExplosion explosion = new FireworkExplosion(
                        shape,
                        rgbListToColorsByBiFunction(arg_colors),
                        rgbListToColorsByBiFunction(arg_fadeColors),
                        hasTrail,
                        hasTwinkle
                );
                Fireworks fireworks = new Fireworks(flightDuration, List.of(explosion));
                fireworkItem.set(DataComponents.FIREWORKS, fireworks);

                FireworkRocketEntity firework = new FireworkRocketEntity(
                        ctx.level(),
                        target.x, target.y, target.z,
                        fireworkItem
                );
                ctx.level().addFreshEntity(firework);
            }))
            .description("生成雪球")
            .handler(ChineseUtils.bracketOf("雪丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                Vec3 target = lampToRangeByBiFunction(bp, args.get(0).as(WenyanVec3.TYPE).value());
                Snowball snowball = new Snowball(ctx.level(), target.x, target.y, target.z,
                        new ItemStack(Items.SNOWBALL));
                ctx.level().addFreshEntity(snowball);
            }))
            .description("生成火球")
            .handler(ChineseUtils.bracketOf("火丸"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = request.args();
                Vec3 target = lampToRangeByBiFunction(bp, args.get(0).as(WenyanVec3.TYPE).value());
                SmallFireball fireball = new SmallFireball(ctx.level(), target.x, target.y, target.z,
                        new Vec3(0, 0, 0));
                ctx.level().addFreshEntity(fireball);
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_PROJECTILE_SPAWNER_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("生成箭矢")
            .handler(ChineseUtils.bracketOf("箭"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    Vec3 target = lampToRangeByFunction(entity, args.get(0).as(WenyanVec3.TYPE).value());
                    Arrow arrow = new Arrow(entity.level(), target.x, target.y, target.z,
                            new ItemStack(Items.ARROW), null);
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    entity.level().addFreshEntity(arrow);
                }
                return WenyanNull.NULL;
            })
            .description("发射烟花火箭")
            .handler(ChineseUtils.bracketOf("煙火"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    int shapeId = (int) args.get(0).as(WenyanDouble.TYPE).value();
                    FireworkExplosion.Shape shape = switch (shapeId) {
                        case 1 -> FireworkExplosion.Shape.SMALL_BALL;
                        case 2 -> FireworkExplosion.Shape.STAR;
                        case 3 -> FireworkExplosion.Shape.CREEPER;
                        case 4 -> FireworkExplosion.Shape.BURST;
                        default -> FireworkExplosion.Shape.LARGE_BALL;
                    };

                    List<IWenyanValue> arg_colors = args.get(1).as(WenyanList.TYPE).value();
                    if (arg_colors.isEmpty()) {
                        throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                    } else if (arg_colors.size() % 3 != 0) {
                        throw new WenyanException.WenyanTypeException("第二参数应当为一个数字列表且必须是3的倍数");
                    }
                    for (IWenyanValue v : arg_colors) {
                        v.as(WenyanDouble.TYPE);
                    }
                    List<IWenyanValue> arg_fadeColors = args.get(2).as(WenyanList.TYPE).value();
                    if (arg_fadeColors.isEmpty()) {
                        throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                    } else if (arg_fadeColors.size() % 3 != 0) {
                        throw new WenyanException.WenyanTypeException("第三参数应当为一个数字列表且必须是3的倍数");
                    }
                    for (IWenyanValue v : arg_fadeColors) {
                        v.as(WenyanDouble.TYPE);
                    }
                    boolean hasTrail = args.get(3).as(WenyanBoolean.TYPE).value();
                    boolean hasTwinkle = args.get(4).as(WenyanBoolean.TYPE).value();
                    int flightDuration = Math.clamp((int) Math.round(args.get(5).as(WenyanDouble.TYPE).value()), 1, 3);

                    Vec3 target = lampToRangeByFunction(entity, args.get(6).as(WenyanVec3.TYPE).value());
                    ItemStack fireworkItem = new ItemStack(Items.FIREWORK_ROCKET);

                    FireworkExplosion explosion = new FireworkExplosion(
                            shape,
                            rgbListToColorsByBiFunction(arg_colors),
                            rgbListToColorsByBiFunction(arg_fadeColors),
                            hasTrail,
                            hasTwinkle
                    );
                    Fireworks fireworks = new Fireworks(flightDuration, List.of(explosion));
                    fireworkItem.set(DataComponents.FIREWORKS, fireworks);

                    FireworkRocketEntity firework = new FireworkRocketEntity(
                            entity.level(),
                            target.x, target.y, target.z,
                            fireworkItem
                    );
                    entity.level().addFreshEntity(firework);
                }
                return WenyanNull.NULL;
            })
            .description("生成雪球")
            .handler(ChineseUtils.bracketOf("雪丸"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    Vec3 target = lampToRangeByFunction(entity, args.get(0).as(WenyanVec3.TYPE).value());
                    Snowball snowball = new Snowball(entity.level(), target.x, target.y, target.z,
                            new ItemStack(Items.SNOWBALL));
                    entity.level().addFreshEntity(snowball);
                }
                return WenyanNull.NULL;
            })
            .description("生成火球")
            .handler(ChineseUtils.bracketOf("火丸"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = request.args();
                    Vec3 target = lampToRangeByFunction(entity, args.get(0).as(WenyanVec3.TYPE).value());
                    SmallFireball fireball = new SmallFireball(entity.level(), target.x, target.y, target.z,
                            new Vec3(0, 0, 0));
                    entity.level().addFreshEntity(fireball);
                }
                return WenyanNull.NULL;
            })
            .build();
}
