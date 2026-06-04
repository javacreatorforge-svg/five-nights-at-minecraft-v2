package com.redstonedev.fivenightsatminecraft.entity;

import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class ChicaEntity extends AbstractAnimatronic {
    private static final String K = "animation.chica.";
    private int groanCooldown;

    public ChicaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.groanCooldown = 200 + this.random.nextInt(300);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return baseAttributes();
    }

    @Override protected int charId() { return 2; }
    @Override protected boolean canCrouch() { return true; }   // can crouch, cannot crawl/climb
    @Override protected float standHeight() { return 2.3F; }    // slightly shorter
    @Override protected float stalkBias() { return 0.85F; }     // stalks more
    @Override protected SoundEvent footstepSound() { return ModSounds.CHICA_FOOTSTEPS.get(); }

    @Override
    protected void onPassiveTick(Player nearest) {
        if (groanCooldown > 0) groanCooldown--;
        // Groan when a player is nearby, but not spammy.
        if (groanCooldown <= 0 && nearest != null && this.distanceTo(nearest) < 14.0D) {
            SoundEvent g = ModSounds.CHICA_GROANS.get(this.random.nextInt(ModSounds.CHICA_GROANS.size())).get();
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(), g, SoundSource.HOSTILE, 0.9F, 1.0F);
            groanCooldown = 320 + this.random.nextInt(360); // ~16-34s between groans
        }
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "loco", 3, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        int posture = getPosture();
        boolean moving = isMovingAnim();
        boolean aggressive = isAggressive();
        String anim;
        if (posture == 1) { // crouch (cannot crawl)
            if (moving) anim = aggressive ? K + "crouch_run" : K + "crouch_walk";
            else anim = K + "crouchidle";
        } else {
            if (aggressive && moving) anim = K + "run";
            else if (moving) anim = K + "walk";
            else anim = K + "idle";
        }
        event.getController().setAnimation(new AnimationBuilder().loop(anim));
        return PlayState.CONTINUE;
    }
}
