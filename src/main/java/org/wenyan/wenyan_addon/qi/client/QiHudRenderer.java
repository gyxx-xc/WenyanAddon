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

import java.util.List;

@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public final class QiHudRenderer {
    private static final int BAR_X = 4;
    private static final int BAR_Y = 4;
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 6;
    private static final int YIN_YANG_BAR_HEIGHT = 3;

    private static final List<ElementType> WUXING_BAR_ELEMENTS = List.of(
            ElementType.METAL, ElementType.WOOD, ElementType.WATER, ElementType.FIRE, ElementType.EARTH, ElementType.NEUTRAL);
    private static final List<ElementType> YIN_YANG_BAR_ELEMENTS = List.of(ElementType.YIN, ElementType.YANG);

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
        drawBar(graphics, qi, max, WUXING_BAR_ELEMENTS, BAR_Y, BAR_HEIGHT,
                Component.literal(Math.round(qi.getTotal()) + "/" + Math.round(max)));
        drawBar(graphics, qi, max, YIN_YANG_BAR_ELEMENTS, BAR_Y + BAR_HEIGHT + 10, YIN_YANG_BAR_HEIGHT,
                Component.literal(Math.round(qi.get(ElementType.YIN)) + "/" + Math.round(qi.get(ElementType.YANG))));
    }

    private static void drawBar(GuiGraphicsExtractor graphics, PlayerQiData qi, double max,
                                List<ElementType> elements, int y, int height, Component label) {
        graphics.fill(BAR_X - 1, y - 1, BAR_X + BAR_WIDTH + 1, y + height + 1, 0xFF000000);
        int x = BAR_X;
        for (ElementType element : elements) {
            int segmentWidth = (int) Math.round(qi.get(element) / max * BAR_WIDTH);
            segmentWidth = Math.min(segmentWidth, BAR_X + BAR_WIDTH - x);
            if (segmentWidth > 0) {
                graphics.fill(x, y, x + segmentWidth, y + height, colorOf(element));
                x += segmentWidth;
            }
            if (x >= BAR_X + BAR_WIDTH) {
                break;
            }
        }
        graphics.text(Minecraft.getInstance().font, label, BAR_X, y + height + 1, 0xFFFFFFFF, false);
    }

    private static int colorOf(ElementType element) {
        return switch (element) {
            case METAL -> 0xFFE5C07B;
            case WOOD -> 0xFF5FD35F;
            case WATER -> 0xFF4FC1FF;
            case FIRE -> 0xFFE05A4E;
            case EARTH -> 0xFFB98A4F;
            case YIN -> 0xFF7B5FD3;
            case YANG -> 0xFFFFD75F;
            case NEUTRAL -> 0xFF9AA5B1;
        };
    }
}
