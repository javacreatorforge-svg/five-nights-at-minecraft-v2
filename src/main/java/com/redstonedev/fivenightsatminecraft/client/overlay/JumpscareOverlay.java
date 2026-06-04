package com.redstonedev.fivenightsatminecraft.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

@OnlyIn(Dist.CLIENT)
public class JumpscareOverlay implements IGuiOverlay {
    public static final JumpscareOverlay INSTANCE = new JumpscareOverlay();

    private static final ResourceLocation[] IMG = {
            tex("freddy_jumpscare"), tex("bonnie_jumpscare"), tex("chica_jumpscare"),
            tex("foxy_jumpscare"), tex("goldenfreddy_jumpscare")
    };
    // width,height of each jumpscare image (for contain-scaling)
    private static final float[][] DIM = {
            {640, 360}, {640, 295}, {640, 506}, {269, 360}, {640, 384}
    };

    private static ResourceLocation tex(String n) {
        return new ResourceLocation(FiveNightsAtMinecraft.MODID, "textures/gui/" + n + ".png");
    }

    @Override
    public void render(net.minecraftforge.client.gui.overlay.ForgeGui gui, PoseStack pose,
                       float partialTick, int width, int height) {
        if (JumpscareState.ticksRemaining <= 0) return;
        int id = JumpscareState.charId;
        if (id < 0 || id >= IMG.length) id = 0;

        GuiComponent.fill(pose, 0, 0, width, height, 0xFF000000); // black background

        float iw = DIM[id][0], ih = DIM[id][1];
        float scale = Math.min(width / iw, height / ih);
        float drawW = iw * scale, drawH = ih * scale;
        float x = (width - drawW) / 2.0F, y = (height - drawH) / 2.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, IMG[id]);
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0F);
        GuiComponent.blit(pose, 0, 0, 0, 0.0F, 0.0F, (int) iw, (int) ih, (int) iw, (int) ih);
        pose.popPose();
        RenderSystem.disableBlend();
    }
}
