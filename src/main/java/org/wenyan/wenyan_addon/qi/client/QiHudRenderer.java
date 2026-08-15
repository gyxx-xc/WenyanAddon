package org.wenyan.wenyan_addon.qi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.wenyan.wenyan_addon.WenyanAddon;
import org.wenyan.wenyan_addon.qi.element.ElementAttribute;
import org.wenyan.wenyan_addon.qi.element.ElementRegistry;
import org.wenyan.wenyan_addon.qi.element.ElementType;
import org.wenyan.wenyan_addon.qi.player.PlayerQi;
import org.wenyan.wenyan_addon.qi.player.PlayerQiData;

@EventBusSubscriber(modid = WenyanAddon.MODID, value = Dist.CLIENT)
public final class QiHudRenderer {
    private static final int BAR_X = 4;
    private static final int BAR_Y = 4;
    private static final int BAR_WIDTH = 100;
    private static final int SHORT_BAR_WIDTH = 60;
    private static final int BAR_HEIGHT = 6;
    private static final int BAR_GAP = 3;
    private static final int LABEL_GAP = 8;

    private QiHudRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        PlayerQiData qi = PlayerQi.of(minecraft.player);
        GuiGraphicsExtractor graphics = event.getGuiGraphics();

        // 初始为最大的无属性灵气条
        int y = BAR_Y;
        drawAttributeBar(graphics, qi, ElementType.NEUTRAL, y, BAR_WIDTH, BAR_HEIGHT);
        y += BAR_HEIGHT + LABEL_GAP;

        // 已解锁的五行/衍生属性条（缩短，排列在无属性条下方）
        for (ElementAttribute element : ElementRegistry.all()) {
            if (element == ElementType.NEUTRAL
                    || element == ElementType.YIN || element == ElementType.YANG) {
                continue;
            }
            if (qi.cap(element) <= 0) {
                continue;
            }
            drawAttributeBar(graphics, qi, element, y, SHORT_BAR_WIDTH, BAR_HEIGHT);
            y += BAR_HEIGHT + LABEL_GAP;
        }

        // 已解锁的阴阳条
        if (qi.cap(ElementType.YIN) > 0) {
            drawAttributeBar(graphics, qi, ElementType.YIN, y, SHORT_BAR_WIDTH, BAR_HEIGHT);
            y += BAR_HEIGHT + LABEL_GAP;
        }
        if (qi.cap(ElementType.YANG) > 0) {
            drawAttributeBar(graphics, qi, ElementType.YANG, y, SHORT_BAR_WIDTH, BAR_HEIGHT);
        }
    }

    private static void drawAttributeBar(GuiGraphicsExtractor graphics, PlayerQiData qi,
                                         ElementAttribute element, int y, int width, int height) {
        graphics.fill(BAR_X - 1, y - 1, BAR_X + width + 1, y + height + 1, 0xFF000000);
        graphics.fill(BAR_X, y, BAR_X + width, y + height, 0x66CCCCCC);
        double cap = qi.cap(element);
        if (cap > 0) {
            int fill = (int) Math.floor(qi.get(element) / cap * width);
            fill = Math.min(fill, width);
            if (fill > 0) {
                graphics.fill(BAR_X, y, BAR_X + fill, y + height, element.color());
            }
        }
        String label = element.displayName() + " " + format(qi.get(element)) + "/" + format(cap);
        graphics.text(Minecraft.getInstance().font, Component.literal(label), BAR_X, y + height + 1, 0xFFFFFFFF, false);
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
