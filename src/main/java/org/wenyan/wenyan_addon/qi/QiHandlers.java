package org.wenyan.wenyan_addon.qi;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.element.RelationType;
import org.wenyan.wenyan_addon.qi.environment.EnvironmentQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;
import org.wenyan.wenyan_addon.value.WenyanElement;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.util.EnumMap;
import java.util.function.BiFunction;

/**
 * 灵气设备（测试阶段）：仅提供五行关系与玩家灵气的查询操作，暂不参与消耗。
 */
public class QiHandlers {
    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> QI_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("（测试阶段）查询相生：谁生我")
            .handler(ChineseUtils.bracketOf("五行生"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanList result = new WenyanList();
                if (request.args().isEmpty()) {
                    return result;
                }
                ElementType element = parseElement(request.args().get(0));
                if (element == null) {
                    return result;
                }
                ElementType generator = ElementRelations.generatedBy(element);
                if (generator != null) {
                    result.add(new WenyanElement(generator));
                }
                return result;
            }))
            .description("（测试阶段）查询相生：我生谁")
            .handler(ChineseUtils.bracketOf("五行生往"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanList result = new WenyanList();
                if (request.args().isEmpty()) {
                    return result;
                }
                ElementType element = parseElement(request.args().get(0));
                if (element == null) {
                    return result;
                }
                ElementType generated = ElementRelations.generates(element);
                if (generated != null) {
                    result.add(new WenyanElement(generated));
                }
                return result;
            }))
            .description("（测试阶段）查询相克：谁克我")
            .handler(ChineseUtils.bracketOf("五行克"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanList result = new WenyanList();
                if (request.args().isEmpty()) {
                    return result;
                }
                ElementType element = parseElement(request.args().get(0));
                if (element == null) {
                    return result;
                }
                ElementType counter = ElementRelations.counteredBy(element);
                if (counter != null) {
                    result.add(new WenyanElement(counter));
                }
                return result;
            }))
            .description("（测试阶段）查询相克：我克谁")
            .handler(ChineseUtils.bracketOf("五行克往"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanList result = new WenyanList();
                if (request.args().isEmpty()) {
                    return result;
                }
                ElementType element = parseElement(request.args().get(0));
                if (element == null) {
                    return result;
                }
                ElementType countered = ElementRelations.counters(element);
                if (countered != null) {
                    result.add(new WenyanElement(countered));
                }
                return result;
            }))
            .description("（测试阶段）查询两元素五行关系")
            .handler(ChineseUtils.bracketOf("五行关系"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (request.args().size() < 2) {
                    return new WenyanString("");
                }
                ElementType a = parseElement(request.args().get(0));
                ElementType b = parseElement(request.args().get(1));
                if (a == null || b == null) {
                    return new WenyanString("");
                }
                return new WenyanString(relationName(ElementRelations.relation(a, b)));
            }))
            .description("（测试阶段）查询玩家灵气储量")
            .handler(ChineseUtils.bracketOf("灵气储量"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanMapValue result = new WenyanMapValue();
                ServerPlayer player = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (player == null) {
                    return result;
                }
                PlayerQiData qi = PlayerQi.of(player);
                for (ElementType element : ElementType.values()) {
                    result.put(element.displayName(), WenyanValues.of(qi.get(element)));
                }
                return result;
            }))
            .description("（测试阶段）查询玩家灵气总量上限")
            .handler(ChineseUtils.bracketOf("灵气上限"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (request.args().isEmpty()) {
                    return WenyanValues.of(0);
                }
                Player player = entityAsPlayer(request.args().get(0));
                if (player == null) {
                    return WenyanValues.of(0);
                }
                return WenyanValues.of(PlayerQiData.MAX_QI);
            }))
            .description("（测试阶段）查询区域灵气浓度")
            .handler(ChineseUtils.bracketOf("环境浓度"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanMapValue result = new WenyanMapValue();
                if (request.args().isEmpty()) {
                    return result;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                EnumMap<ElementType, Double> concentration = EnvironmentQi.concentration(ctx.level(), BlockPos.containing(center));
                for (ElementType element : ElementRelations.ELEMENTS) {
                    result.put(element.displayName(), WenyanValues.of(concentration.get(element)));
                }
                return result;
            }))
            .build();

    private static Player entityAsPlayer(IWenyanValue value) throws WenyanException.WenyanTypeException {
        return value.as(WenyanEntity.TYPE).value() instanceof Player player ? player : null;
    }

    private static ElementType parseElement(IWenyanValue value) throws WenyanException.WenyanTypeException {
        if (value.is(WenyanElement.TYPE)) {
            return value.as(WenyanElement.TYPE).value();
        }
        String raw = value.as(WenyanString.TYPE).value();
        for (ElementType element : ElementType.values()) {
            if (element.displayName().equals(raw) || element.name().equalsIgnoreCase(raw)) {
                return element;
            }
        }
        return null;
    }

    private static String relationName(RelationType relation) {
        return switch (relation) {
            case SAME -> "同";
            case GENERATING -> "相生";
            case GENERATED -> "被生";
            case COUNTER -> "相克";
            case COUNTERED -> "被克";
            case NONE -> "无";
        };
    }
}
