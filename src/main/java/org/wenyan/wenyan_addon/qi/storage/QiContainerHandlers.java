package org.wenyan.wenyan_addon.qi.storage;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.structure.RawHandlerPackage;
import indi.wenyan.judou.api.utils.ChineseUtils;
import indi.wenyan.judou.api.utils.WenyanValues;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import indi.wenyan.judou.api.values.exception.WenyanException;
import indi.wenyan.judou.api.values.primitive.WenyanDouble;
import indi.wenyan.judou.api.values.primitive.WenyanString;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.wenyan.wenyan_addon.device.BlockHandlerHelper;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.player.PlayerEquipment;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;
import org.wenyan.wenyan_addon.value.WenyanMapValue;

import java.util.function.BiFunction;

/**
 * 灵气容器设备：方块容器（灵气池）查询/注入/提取。
 */
public class QiContainerHandlers {

    public static final BiFunction<BlockPos, BlockState, RawHandlerPackage> QI_STORAGE_PACKAGE = (bp, _) -> HandlerPackageBuilder.create()
            .description("查询灵气池各属性储量")
            .handler(ChineseUtils.bracketOf("储量"), BlockHandlerHelper.wrap((ctx, _) -> {
                WenyanMapValue result = new WenyanMapValue();
                if (ctx.level().getBlockEntity(bp) instanceof QiContainer container) {
                    putReserves(result, container);
                }
                return result;
            }))
            .description("从玩家灵气注入灵气池：注入以「属性」以数量")
            .handler(ChineseUtils.bracketOf("注入"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level().getBlockEntity(bp) instanceof QiContainer container)
                        || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return WenyanNull.NULL;
                }
                ElementAttribute element = parseAttribute(request.args().get(0));
                double amount = amountOf(request.args().get(1));
                if (element == null || amount <= 0) {
                    return WenyanNull.NULL;
                }
                PlayerQiData qi = PlayerQi.of(caster);
                double moved = Math.min(amount, qi.get(element));
                double stored = container.add(element, moved);
                qi.consume(element, stored);
                PlayerQi.markDirty(caster);
                return new WenyanDouble(stored);
            }))
            .description("从灵气池提取灵气到玩家：提取以「属性」以数量")
            .handler(ChineseUtils.bracketOf("提取"), BlockHandlerHelper.wrap((ctx, request) -> {
                if (!(ctx.level().getBlockEntity(bp) instanceof QiContainer container)
                        || request.args().size() < 2) {
                    return WenyanNull.NULL;
                }
                ServerPlayer caster = ((BlockContextCasterAccessor) (Object) ctx).getCaster();
                if (caster == null) {
                    return WenyanNull.NULL;
                }
                ElementAttribute element = parseAttribute(request.args().get(0));
                double amount = amountOf(request.args().get(1));
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
                return new WenyanDouble(accepted);
            }))
            .build();

    /**
     * 查询手持灵珠储量。
     */
    public static WenyanMapValue vesselReserves(ServerPlayer player) {
        WenyanMapValue result = new WenyanMapValue();
        QiContainer container = vesselOf(player);
        if (container != null) {
            putReserves(result, container);
        }
        return result;
    }

    public static QiContainer vesselOf(ServerPlayer player) {
        QiContainer[] found = {null};
        PlayerEquipment.forEachItem(player, stack -> {
            if (found[0] == null && stack.getItem() instanceof QiContainerProvider provider) {
                found[0] = provider.containerOf(stack);
            }
        });
        return found[0];
    }

    private static void putReserves(WenyanMapValue result, QiContainer container) {
        for (ElementAttribute element : ElementRegistry.all()) {
            result.put(element.displayName(), WenyanValues.of(container.get(element)));
        }
    }

    private static ElementAttribute parseAttribute(IWenyanValue value) throws WenyanException.WenyanTypeException {
        String raw = value.as(WenyanString.TYPE).value();
        for (ElementAttribute attribute : ElementRegistry.all()) {
            if (attribute.displayName().equals(raw) || attribute.id().equalsIgnoreCase(raw)) {
                return attribute;
            }
        }
        return null;
    }

    private static double amountOf(IWenyanValue value) throws WenyanException.WenyanTypeException {
        return value.as(WenyanDouble.TYPE).value();
    }
}
