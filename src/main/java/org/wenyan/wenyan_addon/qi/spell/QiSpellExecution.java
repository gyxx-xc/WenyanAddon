package org.wenyan.wenyan_addon.qi.spell;

import indi.wenyan.content.block.runner.BlockRequest;
import indi.wenyan.content.entity.ThrowEntityContext;
import indi.wenyan.interpreter_impl.HandlerPackageBuilder;
import indi.wenyan.judou.api.exec.request.IArgsRequest;
import indi.wenyan.judou.api.exec.structure.IHandleContext;
import indi.wenyan.judou.api.values.IWenyanValue;
import indi.wenyan.judou.api.values.WenyanNull;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.wenyan.wenyan_addon.mixin_util.BlockContextCasterAccessor;
import org.wenyan.wenyan_addon.qi.consume.ConsumptionResult;
import org.wenyan.wenyan_addon.qi.consume.QiConsumable;
import org.wenyan.wenyan_addon.qi.consume.QiConsumption;
import org.wenyan.wenyan_addon.qi.consume.YinYangTendency;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerEquipment;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;
import org.wenyan.wenyan_addon.qi.storage.QiContainer;
import org.wenyan.wenyan_addon.qi.storage.QiContainerProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 符咒执行包装：取施法者灵气组成 → 标签匹配 → 执行方法体 → 统一扣费。
 * 消耗规则：方法体通过 {@link QiSpellContext#require} 登记自定义消耗（成功后统一扣）；
 * 未登记时按注解 baseCost × 匹配系数执行默认消耗。方法体抛异常时不扣费。
 */
public final class QiSpellExecution {
    private static final int CONTAINER_RANGE = 8;

    private QiSpellExecution() {
    }

    public static HandlerPackageBuilder.HandlerReturnFunction execute(List<ElementAttribute> primary,
                                                                      List<ElementAttribute> compatible,
                                                                      double baseCost,
                                                                      YinYangTendency tendency,
                                                                      QiSpellMethod method) {
        return (ctx, request) -> {
            if (!(request instanceof IArgsRequest argsRequest)) {
                return WenyanNull.NULL;
            }
            ServerPlayer caster = resolveCaster(ctx);
            if (caster == null) {
                return WenyanNull.NULL;
            }
            PlayerQiData qi = PlayerQi.of(caster);
            QiComposition input = QiComposition.of(qi);
            ElementAttribute spell = primary.isEmpty() ? ElementType.NEUTRAL : primary.get(0);
            QiMatch match = QiSpellMatcher.match(primary, compatible, tendency, input, qi.coefficients(spell));
            List<QiContainer> containers = collectContainers(caster);
            if (!hasAnyQi(qi, containers)) {
                argsRequest.thread().platform().handleError("灵气不足");
                return WenyanNull.NULL;
            }
            QiSpellContextImpl spellContext = new QiSpellContextImpl(caster, qi, match, containers);
            IWenyanValue result = method.invoke(ctx, argsRequest, spellContext);
            if (spellContext.registered()) {
                // 自定义消耗：方法体成功后统一扣费
                if (!hasNeed(qi, containers, spellContext.getNeed())) {
                    argsRequest.thread().platform().handleError("灵气不足");
                    return WenyanNull.NULL;
                }
                payNeed(qi, containers, spellContext.getNeed());
                PlayerQi.markDirty(caster);
            } else if (!spendDefault(caster, qi, primary, baseCost, tendency, match, containers, argsRequest)) {
                return WenyanNull.NULL;
            }
            return result;
        };
    }

    /**
     * 解析施法者：方块上下文取 Mixin 注入的施法者；投掷上下文取投掷实体所属玩家；
     * 玩家施法上下文直接取施法玩家。
     */
    private static ServerPlayer resolveCaster(IHandleContext ctx) {
        if (ctx instanceof BlockRequest.BlockContext blockContext) {
            return ((BlockContextCasterAccessor) (Object) blockContext).getCaster();
        }
        if (ctx instanceof ThrowEntityContext throwContext) {
            Player player = throwContext.entity().getPlayer();
            return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        }
        if (ctx instanceof PlayerCastContext playerContext) {
            return playerContext.player();
        }
        return null;
    }

    /**
     * 玩家或容器中是否有任何五行系灵气可支付（前置宽松检查，防止无灵气施法）。
     */
    private static boolean hasAnyQi(PlayerQiData qi, List<QiContainer> containers) {
        for (ElementAttribute element : ElementRegistry.all()) {
            if (element == ElementType.YIN || element == ElementType.YANG) {
                continue;
            }
            if (qi.get(element) > 0) {
                return true;
            }
            for (QiContainer container : containers) {
                if (container.get(element) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 默认消耗：检查灵气 → （方法体已执行）→ 扣除。
     */
    private static boolean spendDefault(ServerPlayer player, PlayerQiData qi, List<ElementAttribute> primary,
                                        double baseCost, YinYangTendency tendency, QiMatch match,
                                        List<QiContainer> containers, IArgsRequest request) {
        QiConsumable consumable = consumableOf(primary, baseCost, tendency, match);
        if (match.grade() == MatchGrade.MISSING) {
            ElementAttribute dominant = match.dominant();
            if (dominant == null || !hasTotal(qi, containers, dominant, consumable.baseQiCost())) {
                request.thread().platform().handleError("灵气不足");
                return false;
            }
            payDirect(qi, containers, dominant, consumable.baseQiCost());
            PlayerQi.markDirty(player);
            return true;
        }
        if (!QiConsumption.checkSufficient(player, consumable, containers)) {
            request.thread().platform().handleError("灵气不足");
            return false;
        }
        ConsumptionResult consumed = QiConsumption.tryConsume(player, consumable, containers);
        if (!consumed.success()) {
            request.thread().platform().handleError("灵气不足");
            return false;
        }
        return true;
    }

    private static QiConsumable consumableOf(List<ElementAttribute> primary, double baseCost,
                                             YinYangTendency tendency, QiMatch match) {
        ElementAttribute spell = primary.isEmpty() ? ElementType.NEUTRAL : primary.get(0);
        double cost = baseCost * match.costMultiplier();
        return new QiConsumable() {
            @Override
            public ElementAttribute spellElement() {
                return spell;
            }

            @Override
            public double baseQiCost() {
                return cost;
            }

            @Override
            public YinYangTendency tendency() {
                return tendency;
            }
        };
    }

    /**
     * 收集支付链：背包/装备栏中的容器物品 + 附近（8 格）方块容器。
     * 按优先级排序：优先级大（灵石等一次性容器）最后支付。
     */
    private static List<QiContainer> collectContainers(ServerPlayer player) {
        List<QiContainer> containers = new ArrayList<>();
        PlayerEquipment.forEachItem(player, stack -> addItemContainer(stack, containers));
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-CONTAINER_RANGE, -CONTAINER_RANGE, -CONTAINER_RANGE),
                center.offset(CONTAINER_RANGE, CONTAINER_RANGE, CONTAINER_RANGE))) {
            if (player.level().getBlockEntity(pos) instanceof QiContainer container) {
                containers.add(container);
            }
        }
        containers.sort(java.util.Comparator.comparingInt(QiContainer::priority));
        return containers;
    }

    private static void addItemContainer(ItemStack stack, List<QiContainer> containers) {
        if (stack.getItem() instanceof QiContainerProvider provider) {
            containers.add(provider.containerOf(stack));
        }
    }

    private static boolean hasNeed(PlayerQiData qi, List<QiContainer> containers,
                                   Map<ElementAttribute, Double> need) {
        for (Map.Entry<ElementAttribute, Double> entry : need.entrySet()) {
            if (!hasTotal(qi, containers, entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static void payNeed(PlayerQiData qi, List<QiContainer> containers,
                                Map<ElementAttribute, Double> need) {
        for (Map.Entry<ElementAttribute, Double> entry : need.entrySet()) {
            payDirect(qi, containers, entry.getKey(), entry.getValue());
        }
    }

    private static boolean hasTotal(PlayerQiData qi, List<QiContainer> containers,
                                    ElementAttribute element, double amount) {
        double inContainers = 0;
        for (QiContainer container : containers) {
            inContainers += container.get(element);
        }
        return qi.get(element) + inContainers >= amount;
    }

    private static void payDirect(PlayerQiData qi, List<QiContainer> containers,
                                  ElementAttribute element, double amount) {
        double remaining = amount;
        for (QiContainer container : containers) {
            remaining -= container.consume(element, remaining);
            if (remaining <= 0) {
                return;
            }
        }
        qi.consume(element, remaining);
    }

    /**
     * 施法上下文实现：方法体登记自定义消耗，方法体成功后由包装层统一扣费。
     */
    private static final class QiSpellContextImpl implements QiSpellContext {
        private final ServerPlayer caster;
        private final PlayerQiData qi;
        private final QiMatch match;
        private final List<QiContainer> containers;
        private final Map<ElementAttribute, Double> need = new HashMap<>();

        private QiSpellContextImpl(ServerPlayer caster, PlayerQiData qi, QiMatch match, List<QiContainer> containers) {
            this.caster = caster;
            this.qi = qi;
            this.match = match;
            this.containers = containers;
        }

        @Override
        public ServerPlayer caster() {
            return caster;
        }

        @Override
        public PlayerQiData qi() {
            return qi;
        }

        @Override
        public QiMatch match() {
            return match;
        }

        @Override
        public List<QiContainer> containers() {
            return containers;
        }

        @Override
        public boolean has(ElementAttribute element, double amount) {
            return hasTotal(qi, containers, element, amount);
        }

        @Override
        public void require(ElementAttribute element, double amount) {
            if (amount > 0) {
                need.merge(element, amount, Double::sum);
            }
        }

        @Override
        public void require(Map<ElementAttribute, Double> need) {
            need.forEach(this::require);
        }

        @Override
        public boolean registered() {
            return !need.isEmpty();
        }

        private Map<ElementAttribute, Double> getNeed() {
            return need;
        }
    }
}
