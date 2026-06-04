package com.redstonedev.fivenightsatminecraft.entity;

import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class BonnieEntity extends AbstractAnimatronic {
    private static final String K = "animation.bonnie.";

    public BonnieEntity(EntityType<? extends Monster> type, Level level) { super(type, level); }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return baseAttributes();
    }

    @Override protected int charId() { return 1; }
    @Override protected boolean canClimb() { return true; }   // can climb, no climb animation
    @Override protected float stalkBias() { return 0.6F; }     // stalks, sometimes just wanders
    @Override protected SoundEvent footstepSound() { return ModSounds.BONNIE_FOOTSTEPS.get(); }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "loco", 3, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        boolean moving = isMovingAnim();
        boolean aggressive = isAggressive();
        String anim = (aggressive && moving) ? K + "run" : (moving ? K + "walk" : K + "idle");
        event.getController().setAnimation(new AnimationBuilder().loop(anim));
        return PlayState.CONTINUE;
    }
}
