package org.wenyan.wenyan_addon.qi.ritual;

import indi.wenyan.content.block.crafting_block.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 淬体仪式状态机：
 * 启动时检查 8 格盛放方块配方并消耗物品；仪式 30 秒，每 2 秒降闪电（不致死），
 * 玩家离开结构范围则中断（物品不返还）；结束后清空经验与灵气，
 * 按配方提升血量/属性系数/灵气条上限（经验换算的上限按无属性 90%、已解锁属性 10% 分配）。
 */
public class QiRitualBlockEntity extends BlockEntity {
    public static final int RITUAL_DURATION = 600;      // 30 秒
    public static final int LIGHTNING_INTERVAL = 40;    // 每 2 秒
    public static final int STRUCTURE_RADIUS = 5;
    public static final double EXP_CAP_RATIO = 1000.0;  // 每 1000 经验 → 1 点上限
    public static final double MAX_CAP_GAIN = 50.0;

    /**
     * 8 个盛放方块位置：东西南北间隔 2 格，四角间隔 1 格。
     */
    private static final List<BlockPos> PEDESTAL_OFFSETS = List.of(
            new BlockPos(3, 0, 0), new BlockPos(-3, 0, 0), new BlockPos(0, 0, 3), new BlockPos(0, 0, -3),
            new BlockPos(2, 0, 2), new BlockPos(2, 0, -2), new BlockPos(-2, 0, 2), new BlockPos(-2, 0, -2));

    private UUID casterId = null;
    private BlockPos casterStartPos = null;
    private int elapsed = 0;
    private int nextLightning = 0;
    private QiRitualRecipe activeRecipe = null;

    public QiRitualBlockEntity(BlockPos pos, BlockState blockState) {
        super(WenyanAddon.QI_RITUAL_BLOCK_ENTITY.get(), pos, blockState);
    }

    public boolean isRunning() {
        return casterId != null;
    }

    /**
     * 尝试启动：8 格盛放方块（WenyanNature 基座）满足配方则消耗物品并开始。
     */
    public boolean tryStart(ServerLevel level, ServerPlayer player) {
        List<PedestalBlockEntity> pedestals = findPedestals(level);
        if (pedestals.size() < 8) {
            return false;
        }
        List<ItemStack> stacks = pedestals.stream()
                .map(pedestal -> ItemUtil.getStack(pedestal.getItemHandler(), 0))
                .filter(stack -> !stack.isEmpty())
                .toList();
        for (QiRitualRecipe recipe : QiRitualRecipes.recipes()) {
            if (recipe.matches(stacks)) {
                for (PedestalBlockEntity pedestal : pedestals) {
                    ResourceHandlerUtil.extractFirst(pedestal.getItemHandler(), _ -> true, 1, null);
                }
                casterId = player.getUUID();
                casterStartPos = player.blockPosition();
                elapsed = 0;
                nextLightning = 0;
                activeRecipe = recipe;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public void tick(ServerLevel level) {
        // 结构完整时核心上方不定时出现蓝白色粒子
        if (level.getRandom().nextFloat() < 0.05f && isStructureComplete(level)) {
            BlockPos center = getBlockPos();
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                    center.getX() + 0.5, center.getY() + 1.2, center.getZ() + 0.5,
                    1, 0.3, 0.3, 0.3, 0.02);
        }
        if (!isRunning()) {
            return;
        }
        elapsed++;
        ServerPlayer caster = level.getServer().getPlayerList().getPlayer(casterId);
        if (caster == null || !caster.isAlive()) {
            abort();
            return;
        }
        // 离开结构范围 → 中断，物品不返还
        if (casterStartPos == null || manhattanDistance(caster.blockPosition(), casterStartPos) > STRUCTURE_RADIUS) {
            abort();
            caster.sendSystemMessage(net.minecraft.network.chat.Component.literal("淬体仪式中断：离开仪式范围"));
            return;
        }
        // 闪电淬炼
        if (++nextLightning >= LIGHTNING_INTERVAL) {
            nextLightning = 0;
            strike(level, caster);
        }
        if (elapsed >= RITUAL_DURATION) {
            complete(level, caster);
        }
    }

    private void strike(ServerLevel level, ServerPlayer caster) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (bolt == null) {
            return;
        }
        bolt.setPos(caster.getX(), caster.getY(), caster.getZ());
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
        // 闪电伤害但不致死
        caster.hurt(caster.damageSources().lightningBolt(), 4.0f);
        if (caster.getHealth() <= 0) {
            caster.setHealth(1.0f);
        }
        // 身穿护甲/手持 → 耐久降至 1
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            ItemStack stack = caster.getItemBySlot(slot);
            if (stack.isDamageableItem()) {
                stack.setDamageValue(stack.getMaxDamage() - 1);
            }
        }
    }

    private void complete(ServerLevel level, ServerPlayer caster) {
        QiRitualRecipe recipe = activeRecipe;
        abort();
        if (recipe == null) {
            return;
        }
        // 清空当前所有灵气值
        PlayerQiData qi = PlayerQi.of(caster);
        qi.clearAll();
        // 血量提升
        if (recipe.maxHealthBonus() > 0) {
            double current = caster.getAttributeBaseValue(Attributes.MAX_HEALTH);
            caster.getAttribute(Attributes.MAX_HEALTH).setBaseValue(current + recipe.maxHealthBonus());
        }
        // 属性系数提升
        applyCoefficients(caster, qi, recipe);
        // 经验 → 灵气条上限（无属性 90%、已解锁属性 10%）
        applyCapGain(caster, qi, recipe);
        PlayerQi.markDirty(caster);
        caster.sendSystemMessage(net.minecraft.network.chat.Component.literal("淬体仪式完成"));
    }

    private void applyCoefficients(ServerPlayer caster, PlayerQiData qi, QiRitualRecipe recipe) {
        List<ElementAttribute> targets = new ArrayList<>();
        if (recipe.attributes().isEmpty()) {
            targets.addAll(ElementRegistry.all());
        } else {
            for (String id : recipe.attributes()) {
                ElementAttribute attribute = ElementRegistry.byId(id);
                if (attribute != null) {
                    targets.add(attribute);
                }
            }
        }
        for (ElementAttribute attribute : targets) {
            if (attribute == ElementType.YIN || attribute == ElementType.YANG) {
                continue;
            }
            var coefficients = qi.coefficients(attribute);
            for (var entry : recipe.coefficients().entrySet()) {
                coefficients = coefficients.with(entry.getKey(), entry.getValue());
            }
            qi.setCoefficients(attribute, coefficients);
        }
    }

    private void applyCapGain(ServerPlayer caster, PlayerQiData qi, QiRitualRecipe recipe) {
        // 经验换算的上限仅分给无属性
        double expGain = Math.min(MAX_CAP_GAIN, caster.totalExperience / EXP_CAP_RATIO);
        if (expGain > 0) {
            qi.increaseCap(ElementType.NEUTRAL, expGain);
        }
        // 配方 capBonus：按配方 attributes 指定属性；空列表 = 全部已解锁均分
        double capBonus = recipe.capBonus();
        if (capBonus > 0) {
            List<ElementAttribute> targets = resolveCapTargets(recipe, qi);
            if (targets.isEmpty()) {
                qi.increaseCap(ElementType.NEUTRAL, capBonus);
            } else {
                double each = capBonus / targets.size();
                for (ElementAttribute attribute : targets) {
                    qi.increaseCap(attribute, each);
                }
            }
        }
        // 清空经验
        caster.totalExperience = 0;
        caster.experienceLevel = 0;
        caster.experienceProgress = 0.0F;
    }

    /**
     * 配方 capBonus 目标：attributes 非空按配方指定（unlockQi 配方可解锁未解锁属性）；
     * attributes 为空 = 默认无属性。
     */
    private List<ElementAttribute> resolveCapTargets(QiRitualRecipe recipe, PlayerQiData qi) {
        if (recipe.attributes().isEmpty()) {
            return List.of(ElementType.NEUTRAL);
        }
        List<ElementAttribute> targets = new ArrayList<>();
        for (String id : recipe.attributes()) {
            ElementAttribute attribute = ElementRegistry.byId(id);
            if (attribute == null || attribute == ElementType.YIN || attribute == ElementType.YANG) {
                continue;
            }
            if (recipe.unlockQi() || qi.cap(attribute) > 0) {
                targets.add(attribute);
            }
        }
        return targets;
    }

    private void abort() {
        casterId = null;
        casterStartPos = null;
        elapsed = 0;
        nextLightning = 0;
        activeRecipe = null;
        setChanged();
    }

    private List<PedestalBlockEntity> findPedestals(ServerLevel level) {
        List<PedestalBlockEntity> result = new ArrayList<>();
        for (BlockPos offset : PEDESTAL_OFFSETS) {
            if (level.getBlockEntity(getBlockPos().offset(offset)) instanceof PedestalBlockEntity pedestal) {
                result.add(pedestal);
            }
        }
        return result;
    }

    /**
     * 结构完整性：8 个盛放方块是否全部就位
     * （东西南北间隔 2 格、四角间隔 1 格）。
     */
    public boolean isStructureComplete(ServerLevel level) {
        return findPedestals(level).size() == PEDESTAL_OFFSETS.size();
    }

    private static int manhattanDistance(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }
}
