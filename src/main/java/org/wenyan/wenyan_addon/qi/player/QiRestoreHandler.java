package org.wenyan.wenyan_addon.qi.player;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiData;
import org.wenyan.wenyan_addon.qi.chunk.ChunkQiManager;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;

import java.util.HashSet;
import java.util.Set;@EventBusSubscriber(modid = WenyanAddon.MODID)
public final class QiRestoreHandler {
    private static final int SLOW_INTERVAL = 20;
    private static final double LEAK_RATE_PER_SECOND = 0.005;
    private static long tickCounter = 0;

    private QiRestoreHandler() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        tickCounter++;
        boolean slowTick = tickCounter % SLOW_INTERVAL == 0;
        double perTick = 1.0 / 20.0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerLevel level = (ServerLevel) player.level();
            ChunkQiManager manager = ChunkQiManager.of(level);
            if (slowTick) {
                manager.tick(level);
                manager.veinTick(level);
            }
            ChunkQiData chunkQi = manager.getChunkQi(level, ChunkPos.containing(player.blockPosition()));
            PlayerQiData qi = PlayerQi.of(player);
            long dayTime = level.getOverworldClockTime() % 24000;
            qi.updateYinYang(PlayerQiData.yangRatio(dayTime), perTick);
            int veinStage = manager.veinStageAt(ChunkPos.containing(player.blockPosition()));
            if (chunkQi.isDepleted()) {
                qi.leakQi(LEAK_RATE_PER_SECOND);
            } else {
                // 并行恢复：已解锁属性条（cap>0 五行系/衍生/无属性）
                // + 装备 QiRestoreSource（环境增益 1+n×m）
                Set<ElementAttribute> sources = collectRestoreElements(player);
                sources.add(ElementType.NEUTRAL);
                for (ElementAttribute element : ElementRegistry.all()) {
                    if (element == ElementType.YIN || element == ElementType.YANG) {
                        continue;
                    }
                    if (qi.cap(element) > 0) {
                        sources.add(element);
                    }
                }
                for (ElementAttribute source : sources) {
                    ElementCoefficients c = qi.coefficients(source);
                    double veinBoost = 1.0 + c.veinStageGain() * veinStage;
                    double n = chunkQi.ratio(source);
                    double m = chunkQi.remainingRatio();
                    double gain = c.environmentGainBase() * c.environmentRatioWeight() * n * m;
                    qi.restoreAttribute(source, perTick, gain, veinBoost);
                }
            }
            PlayerQi.markDirty(player);
        }
    }

    private static Set<ElementAttribute> collectRestoreElements(ServerPlayer player) {
        Set<ElementAttribute> result = new HashSet<>();
        PlayerEquipment.forEachItem(player, stack -> addRestoreElements(stack, result));
        return result;
    }

    private static void addRestoreElements(ItemStack stack, Set<ElementAttribute> result) {
        if (stack.getItem() instanceof QiRestoreSource source) {
            result.addAll(source.restoreElements());
        }
    }
}
