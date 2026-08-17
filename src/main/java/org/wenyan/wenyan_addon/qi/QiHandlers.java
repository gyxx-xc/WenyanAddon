package org.wenyan.wenyan_addon.qi;

import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.interpreter_impl.value.WenyanEntity;
import indi.wenyan.interpreter_impl.value.WenyanVec3;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanList;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.chunk.QiVein;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementRelations;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.element.RelationType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;
import org.wenyan.wenyan_addon.qi.storage.QiContainer;
import org.wenyan.wenyan_addon.qi.storage.QiContainerHandlers;
import org.wenyan.wenyan_addon.value.WenyanElement;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.util.EnumMap;
import java.util.Map;
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
                for (ElementAttribute element : ElementRegistry.all()) {
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
                return WenyanValues.of(PlayerQi.of(player).totalCap());
            }))
            .description("（测试阶段）查询区块灵气数据")
            .handler(ChineseUtils.bracketOf("区块灵气"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanMapValue result = new WenyanMapValue();
                if (!(ctx.level() instanceof ServerLevel serverLevel) || request.args().isEmpty()) {
                    return result;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                ChunkQiData chunkQi = ChunkQiManager.of(serverLevel)
                        .getChunkQi(serverLevel, ChunkPos.containing(BlockPos.containing(center)));
                for (ElementType element : ElementRelations.ELEMENTS) {
                    result.put(element.displayName(), WenyanValues.of(chunkQi.get(element)));
                }
                result.put("阴", WenyanValues.of(chunkQi.yin()));
                result.put("阳", WenyanValues.of(chunkQi.yang()));
                result.put("上限", WenyanValues.of(chunkQi.effectiveCap()));
                result.put("活性", WenyanValues.of(chunkQi.activity()));
                result.put("匮乏", WenyanValues.of(chunkQi.isDepleted() ? 1 : 0));
                return result;
            }))
            .description("（测试阶段）查询灵脉覆盖与阶段")
            .handler(ChineseUtils.bracketOf("灵脉"), BlockHandlerHelper.wrap((ctx, request) -> {
                WenyanMapValue result = new WenyanMapValue();
                if (!(ctx.level() instanceof ServerLevel serverLevel) || request.args().isEmpty()) {
                    return result;
                }
                Vec3 center = request.args().get(0).as(WenyanVec3.TYPE).value();
                ChunkPos pos = ChunkPos.containing(BlockPos.containing(center));
                ChunkQiManager manager = ChunkQiManager.of(serverLevel);
                result.put("覆盖", WenyanValues.of(manager.hasVeinAt(pos) ? 1 : 0));
                result.put("阶段", WenyanValues.of(manager.veinStageAt(pos)));
                return result;
            }))
            .description("（验证）查看所有灵脉：等级/阶段/覆盖数")
            .handler(ChineseUtils.bracketOf("灵脉全览"), BlockHandlerHelper.wrap((ctx, _) -> {
                WenyanList result = new WenyanList();
                if (ctx.level() instanceof ServerLevel serverLevel) {
                    ChunkQiManager manager = ChunkQiManager.of(serverLevel);
                    for (Map.Entry<String, QiVein> entry : manager.veins().entrySet()) {
                        WenyanMapValue vein = new WenyanMapValue();
                        QiVein value = entry.getValue();
                        vein.put("位置", new WenyanString(entry.getKey()));
                        vein.put("等级", WenyanValues.of(value.level()));
                        vein.put("阶段", WenyanValues.of(value.stage()));
                        vein.put("覆盖", WenyanValues.of(value.covered().size()));
                        result.add(vein);
                    }
                }
                return result;
            }))
            .description("（验证）覆盖当前设备所在区块的灵脉等级 +1：灵脉升级")
            .handler(ChineseUtils.bracketOf("灵脉升级"), BlockHandlerHelper.wrap((ctx, _) -> {
                if (!(ctx.level() instanceof ServerLevel serverLevel)) {
                    return WenyanNull.NULL;
                }
                ChunkQiManager manager = ChunkQiManager.of(serverLevel);
                ChunkPos pos = ChunkPos.containing(ctx.pos());
                for (Map.Entry<String, QiVein> entry : manager.veins().entrySet()) {
                    if (entry.getValue().covered().contains(keyOf(pos))) {
                        manager.veins().put(entry.getKey(), entry.getValue().nurtureUp(serverLevel.getGameTime()));
                        manager.setDirty();
                        return new WenyanDouble(entry.getValue().level() + 1);
                    }
                }
                return WenyanNull.NULL;
            }))
            .description("（验证）授予玩家灵气条上限：灵气条授予以「属性」以数额")
            .handler(ChineseUtils.bracketOf("灵气条授予"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return WenyanNull.NULL;
                }
                ElementAttribute element = parseAttribute(request.args().get(0));
                double amount = request.args().get(1).as(WenyanDouble.TYPE).value();
                if (element == null || amount <= 0) {
                    return WenyanNull.NULL;
                }
                PlayerQi.of(caster).increaseCap(element, amount);
                PlayerQi.markDirty(caster);
                return WenyanValues.of(PlayerQi.of(caster).cap(element));
            }))
            .description("（验证）清空玩家所有灵力：灵气清空")
            .handler(ChineseUtils.bracketOf("灵气清空"), BlockHandlerHelper.wrap((ctx, _) -> {
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return WenyanNull.NULL;
                }
                PlayerQi.of(caster).clearAll();
                PlayerQi.markDirty(caster);
                return WenyanNull.NULL;
            }))
            .description("查询手持灵珠储量：灵珠储量")
            .handler(ChineseUtils.bracketOf("灵珠储量"), BlockHandlerHelper.wrap((ctx, _) -> {
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return new WenyanMapValue();
                }
                return QiContainerHandlers.vesselReserves(caster);
            }))
            .description("向手持灵珠注入灵气：灵珠注入以「属性」以数额")
            .handler(ChineseUtils.bracketOf("灵珠注入"), BlockHandlerHelper.wrap((ctx, request) -> {
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                QiContainer container = QiContainerHandlers.vesselOf(caster);
                if (container == null) {
                    return WenyanNull.NULL;
                }
                ElementAttribute element = parseAttribute(request.args().get(0));
                double amount = request.args().get(1).as(WenyanDouble.TYPE).value();
                if (element == null || amount <= 0) {
                    return WenyanNull.NULL;
                }
                PlayerQiData qi = PlayerQi.of(caster);
                double moved = Math.min(amount, qi.get(element));
                double stored = container.add(element, moved);
                qi.consume(element, stored);
                PlayerQi.markDirty(caster);
                return WenyanValues.of(stored);
            }))
            .description("从手持灵珠提取灵气：灵珠提取以「属性」以数额")
            .handler(ChineseUtils.bracketOf("灵珠提取"), BlockHandlerHelper.wrap((ctx, request) -> {
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                QiContainer container = QiContainerHandlers.vesselOf(caster);
                if (container == null) {
                    return WenyanNull.NULL;
                }
                ElementAttribute element = parseAttribute(request.args().get(0));
                double amount = request.args().get(1).as(WenyanDouble.TYPE).value();
                if (element == null || amount <= 0) {
                    return WenyanNull.NULL;
                }
                PlayerQiData qi = PlayerQi.of(caster);
                double removed = container.consume(element, amount);
                double before = qi.getTotal();
                qi.add(element, removed);
                double accepted = qi.getTotal() - before;
                if (accepted < removed) {
                    container.add(element, removed - accepted);
                }
                PlayerQi.markDirty(caster);
                return WenyanValues.of(accepted);
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

    private static ElementAttribute parseAttribute(IWenyanValue value) throws WenyanException.WenyanTypeException {
        if (value.is(WenyanElement.TYPE)) {
            return value.as(WenyanElement.TYPE).value();
        }
        String raw = value.as(WenyanString.TYPE).value();
        for (ElementAttribute attribute : ElementRegistry.all()) {
            if (attribute.displayName().equals(raw) || attribute.id().equalsIgnoreCase(raw)) {
                return attribute;
            }
        }
        return null;
    }

    private static String keyOf(ChunkPos pos) {
        return pos.x() + "," + pos.z();
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
