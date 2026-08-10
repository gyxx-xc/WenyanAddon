package org.wenyan.wenyan_addon.qi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public final class QiHudRenderer {
    private static final int BAR_X = 4;
    private static final int BAR_Y = 4;
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 6;

    private QiHudRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        PlayerQiData qi = PlayerQi.of(minecraft.player);
        double max = PlayerQiData.MAX_QI;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.fill(BAR_X - 1, BAR_Y - 1, BAR_X + BAR_WIDTH + 1, BAR_Y + BAR_HEIGHT + 1, 0xFF000000);

        int x = BAR_X;
        for (ElementType element : ElementType.values()) {
            int segmentWidth = (int) Math.round(qi.get(element) / max * BAR_WIDTH);
            segmentWidth = Math.min(segmentWidth, BAR_X + BAR_WIDTH - x);
            if (segmentWidth > 0) {
                graphics.fill(x, BAR_Y, x + segmentWidth, BAR_Y + BAR_HEIGHT, colorOf(element));
                x += segmentWidth;
            }
            if (x >= BAR_X + BAR_WIDTH) {
                break;
            }
        }
        graphics.text(minecraft.font,
                Component.literal(Math.round(qi.getTotal()) + "/" + Math.round(max)),
                BAR_X, BAR_Y + BAR_HEIGHT + 1, 0xFFFFFFFF, false);
    }

    private static int colorOf(ElementType element) {
        return switch (element) {
            case METAL -> 0xFFF0F5FF;
            case WOOD -> 0xFF00CED1;
            case WATER -> 0xFF0A0A1A;
            case FIRE -> 0xFFE05A4E;
            case EARTH -> 0xFFB98A4F;
            case NEUTRAL -> 0xFF9AA5B1;
        };
    }
}
