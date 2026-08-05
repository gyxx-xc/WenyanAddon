package org.wenyan.wenyan_addon.device.handler;

import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.content.entity.ThrowRunnerEntity;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author qq240
 * @version 1.0
 * @className NamingHandlers
 * @Description TODO
 * @date 2026/8/5 14:21
 */
public class NamingHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> NAMING_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("为指定位置的实体命名")
            .handler(ChineseUtils.bracketOf("命名"), BlockHandlerHelper.wrapVoid((ctx, request) -> {
                var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                String name = args.get(3);
                Vec3 center = new Vec3(bp.getX() + (double) args.get(0), bp.getY() + (double) args.get(1), bp.getZ() + (double) args.get(2));
                for (Entity entity : ctx.level().getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                    entity.setCustomName(Component.literal(name));
                }
            }))
            .build();
    public static final Function<ItemStack, RawHandlerPackage> ITEM_NAMING_PACKAGE = _ -> HandlerPackageBuilder.create()
            .description("为指定位置的实体命名")
            .handler(ChineseUtils.bracketOf("命名"), (ctx, request) -> {
                if (ctx instanceof ThrowEntityContext(ThrowRunnerEntity entity)) {
                    var args = BlockHandlerHelper.singleVec3ArgsSpec.copy().string_().resolve(request);
                    String name = args.get(3);
                    Vec3 center = new Vec3(
                            entity.blockPosition().getX() + (double) args.get(0),
                            entity.blockPosition().getY() + (double) args.get(1),
                            entity.blockPosition().getZ() + (double) args.get(2)
                    );
                    for (Entity e : entity.level().getEntities(null, new AABB(center.subtract(0.5, 0.5, 0.5), center.add(0.5, 0.5, 0.5)))) {
                        e.setCustomName(Component.literal(name));
                    }
                }
                return WenyanNull.NULL;
            })
            .build();
}
