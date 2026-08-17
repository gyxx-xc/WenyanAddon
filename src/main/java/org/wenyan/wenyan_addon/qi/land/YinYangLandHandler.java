package org.wenyan.wenyan_addon.qi.land;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;

/**
 * 阴阳之地生物强化：
 * 阴之地 → 敌对生物强化（伤害/血量/速度/抗性），击杀 5% 掉阴灵气结晶；
 * 阳之地 → 中立/友好生物强化（血量/抗性），繁殖 10% 掉阳灵气结晶。
 */
@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class YinYangLandHandler {
    private static final double STRENGTH = 1.5;

    private YinYangLandHandler() {
    }

    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.PositionCheck event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)
                || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        ChunkQiManager manager = ChunkQiManager.of(serverLevel);
        YinYangLandType type = manager.landTypeAt(new ChunkPos((int) event.getX(), (int) event.getZ()));
        if (type == null) {
            return;
        }
        if (type == YinYangLandType.YIN) {
            // 阴之地：敌对生物强化（伤害/血量/速度/抗性）
            if (mob instanceof Monster) {
                boostAttribute(mob, Attributes.ATTACK_DAMAGE, STRENGTH);
                boostAttribute(mob, Attributes.MAX_HEALTH, STRENGTH);
                boostAttribute(mob, Attributes.MOVEMENT_SPEED, 1.3);
                boostAttribute(mob, Attributes.ARMOR, 1.5);
            }
        } else {
            // 阳之地：中立/友好生物强化（血量/抗性）
            if (!(mob instanceof Monster)) {
                boostAttribute(mob, Attributes.MAX_HEALTH, STRENGTH);
                boostAttribute(mob, Attributes.ARMOR, 1.5);
            }
            // 阳之地：友好生物繁殖后代 10% 概率掉阳灵气结晶
            if (event.getSpawnType() == net.minecraft.world.entity.EntitySpawnReason.BREEDING
                    && serverLevel.getRandom().nextDouble() < 0.10) {
                mob.spawnAtLocation(serverLevel, WenyanAddon.YANG_CRYSTAL_ITEM.get().getDefaultInstance());
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)
                || !(event.getEntity() instanceof Monster)) {
            return;
        }
        ChunkQiManager manager = ChunkQiManager.of(serverLevel);
        if (manager.landTypeAt(ChunkPos.containing(event.getEntity().blockPosition())) == YinYangLandType.YIN
                && serverLevel.getRandom().nextDouble() < 0.05) {
            event.getEntity().spawnAtLocation(serverLevel, WenyanAddon.YIN_CRYSTAL_ITEM.get().getDefaultInstance());
        }
    }

    private static void boostAttribute(LivingEntity entity, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
                                       double multiplier) {
        var instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(instance.getBaseValue() * multiplier);
        }
    }
}
