package com.redstonedev.fivenightsatminecraft.client.model;

import com.redstonedev.fivenightsatminecraft.FiveNightsAtMinecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnimatronicModel<T extends LivingEntity & IAnimatable> extends AnimatedGeoModel<T> {
    private final ResourceLocation model, texture, anim;

    public AnimatronicModel(String geoName, String texName, String animName) {
        this.model = rl("geo/" + geoName + ".geo.json");
        this.texture = rl("textures/entity/" + texName + ".png");
        this.anim = rl("animations/" + animName + ".animation.json");
    }

    private static ResourceLocation rl(String path) {
        return new ResourceLocation(FiveNightsAtMinecraft.MODID, path);
    }

    @Override public ResourceLocation getModelResource(T e)     { return model; }
    @Override public ResourceLocation getTextureResource(T e)   { return texture; }
    @Override public ResourceLocation getAnimationResource(T e) { return anim; }
}
