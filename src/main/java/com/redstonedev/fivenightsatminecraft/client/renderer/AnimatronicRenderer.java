package com.redstonedev.fivenightsatminecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstonedev.fivenightsatminecraft.client.model.AnimatronicModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
public class AnimatronicRenderer<T extends LivingEntity & IAnimatable> extends GeoEntityRenderer<T> {
    public AnimatronicRenderer(EntityRendererProvider.Context ctx, String geo, String tex, String anim, float shadow) {
        super(ctx, new AnimatronicModel<>(geo, tex, anim));
        this.shadowRadius = shadow;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        // Keep him lit in tight pockets (default sample can land in the ceiling and read black).
        int best = packedLight;
        BlockPos feet = entity.blockPosition();
        best = maxLight(best, LevelRenderer.getLightColor(entity.level, feet));
        best = maxLight(best, LevelRenderer.getLightColor(entity.level, feet.above()));
        best = maxLight(best, LevelRenderer.getLightColor(entity.level,
                new BlockPos(entity.getX(), entity.getEyeY(), entity.getZ())));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, best);
    }

    private static int maxLight(int a, int b) {
        int blockA = a & 0xFFFF, skyA = (a >>> 16) & 0xFFFF;
        int blockB = b & 0xFFFF, skyB = (b >>> 16) & 0xFFFF;
        return (Math.max(skyA, skyB) << 16) | Math.max(blockA, blockB);
    }
}
