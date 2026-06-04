package com.redstonedev.fivenightsatminecraft.entity;

import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class FreddyEntity extends AbstractAnimatronic {

    private static final String K = "animation.freddy.";
    private int stareTicks = 0;
    private int laughCooldown;
    private boolean spotSoundPlayed = false;
    private Vec3 spottedPlayerPos = null;

    public FreddyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.laughCooldown = 1200 + this.random.nextInt(2400);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return baseAttributes();
    }

    @Override protected int charId() { return 0; }
    @Override protected boolean canCrouch() { return true; }
    @Override protected boolean canCrawl() { return true; }
    @Override protected boolean usesStandoff() { return true; }
    @Override protected float standHeight() { return 2.6F; }
    @Override protected float stalkBias() { return 0.0F; } // Freddy doesn't stalk; he waits/wanders
    @Override protected SoundEvent footstepSound() { return ModSounds.FOOTSTEPS.get(); }

    @Override
    protected void onSpotSound() {
        if (!spotSoundPlayed) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.SPOTS_YOU.get(), SoundSource.HOSTILE, 1.2F, 1.0F);
            spotSoundPlayed = true;
        }
        stareTicks = 0;
        spottedPlayerPos = null;
    }

    @Override
    protected void onPassiveTick(Player nearest) {
        if (laughCooldown > 0) laughCooldown--;
        if (laughCooldown <= 0) {
            playLaugh();
            laughCooldown = 2400 + this.random.nextInt(3600);
        }
    }

    @Override
    protected void tickSpotted(Player player) {
        if (player == null) { setState(State.DORMANT); spotSoundPlayed = false; return; }
        this.getNavigation().stop();
        lockYawTo(player);
        if (spottedPlayerPos == null) spottedPlayerPos = player.position();

        boolean moved = player.position().distanceToSqr(spottedPlayerPos) > 0.04D;
        boolean staring = isPlayerLookingAt(player);
        if (moved) { setState(State.AGGRESSIVE); return; } // player moved (closer or away) -> chase

        if (staring) {
            stareTicks++;
            spottedPlayerPos = player.position();
            if (stareTicks >= 1200) { // stared, didn't move, 1 minute
                if (this.random.nextInt(100) < 12) {
                    setState(State.AGGRESSIVE); // rare: turns aggressive instead (no laugh)
                } else {
                    playLaugh();
                    this.discard();
                }
            }
        } else {
            stareTicks = Math.max(0, stareTicks - 1);
        }
    }

    private void playLaugh() {
        SoundEvent laugh = ModSounds.LAUGHS.get(this.random.nextInt(ModSounds.LAUGHS.size())).get();
        this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                laugh, SoundSource.HOSTILE, 1.1F, 1.0F);
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
        if (posture == 2) { // crawl
            if (moving) anim = aggressive ? K + "crawlaggressive " : K + "crawlcalm"; // NOTE trailing space is in the file
            else anim = K + "crawlidle";
        } else if (posture == 1) { // crouch
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
