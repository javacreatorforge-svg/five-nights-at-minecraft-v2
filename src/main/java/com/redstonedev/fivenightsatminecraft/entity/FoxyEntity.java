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

public class FoxyEntity extends AbstractAnimatronic {
    private static final String K = "animation.foxy.";
    private int voiceCooldown;

    public FoxyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.voiceCooldown = 150 + this.random.nextInt(300);
    }

    public static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder createAttributes() {
        return baseAttributes();
    }

    @Override protected int charId() { return 3; }
    @Override protected boolean breaksPlayerBlocks() { return true; } // breaks blocks to reach hidden players
    @Override protected float stalkBias() { return 0.6F; }
    @Override protected double walkSpeed() { return 0.18D; }  // medium walker
    @Override protected double runSpeed()  { return 0.42D; }  // very fast - player cannot outrun
    @Override protected SoundEvent footstepSound() { return ModSounds.FOXY_FOOTSTEPS.get(); }

    @Override
    protected void onPassiveTick(Player nearest) {
        if (voiceCooldown > 0) voiceCooldown--;
        if (voiceCooldown <= 0) {
            // Singing plays rarely; otherwise a random voiceline.
            if (this.random.nextInt(6) == 0) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                        ModSounds.FOXY_SINGING.get(), SoundSource.HOSTILE, 0.9F, 1.0F);
            } else {
                SoundEvent v = ModSounds.FOXY_VOICELINES.get(this.random.nextInt(ModSounds.FOXY_VOICELINES.size())).get();
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), v, SoundSource.HOSTILE, 0.9F, 1.0F);
            }
            voiceCooldown = 360 + this.random.nextInt(500);
        }
    }

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
