package org.wenyan.wenyan_addon.device.handler.data_disk;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StorageRuneScreen extends AbstractContainerScreen<StorageRuneMenu> {
    private static final int CONTAINER_ROWS = 1;
    private static final Identifier GUI = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");

    public StorageRuneScreen(StorageRuneMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 114 + CONTAINER_ROWS * 18);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int xo = (this.width - this.imageWidth) / 2;
        int yo = (this.height - this.imageHeight) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, xo, yo, 0.0F, 0.0F, this.imageWidth, CONTAINER_ROWS * 18 + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI, xo, yo + CONTAINER_ROWS * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
    }
}
