package com.redstonedev.fivenightsatminecraft.entity;

import com.redstonedev.fivenightsatminecraft.init.ModSounds;
import com.redstonedev.fivenightsatminecraft.network.PacketHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class GoldenFreddyEntity extends Monster implements IAnimatable {

    // gstate: 0 idle/stare, 1 locked (player frozen ~5s), 2 gliding toward player
    private static final EntityDataAccessor<Integer> DATA_GSTATE =
            SynchedEntityData.defineId(GoldenFreddyEntity.class, EntityDataSerializers.INT);

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private int lockTimer = 0;
    private int notLookedTicks = 0;
    private Player lockedPlayer = null;

    public GoldenFreddyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.setNoGravity(false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100000.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_GSTATE, 0);
    }

    @Override
    protected void registerGoals() { /* immobile - no goals */ }

    public int getGState() { return this.entityData.get(DATA_GSTATE); }
    private void setGState(int s) { this.entityData.set(DATA_GSTATE, s); }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide) return;

        int state = getGState();
        Player player = nearestRealPlayer();

        if (state == 0) {
            // Stare at a nearby player; do nothing unless they LOOK at him.
            if (player != null) {
                facePlayer(player);
                if (playerLookingAtMe(player)) {
                    lockedPlayer = player;
                    setGState(1);
                    lockTimer = 100; // 5 seconds frozen
                    if (player instanceof ServerPlayer) {
                        PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                                new PacketHandler.LockViewPacket(this.getId()));
                    }
                    notLookedTicks = 0;
                    return;
                }
                notLookedTicks++;
            } else {
                notLookedTicks++;
            }
            if (notLookedTicks >= 3600) this.discard(); // 3 min of nobody looking -> leave
        } else if (state == 1) {
            if (lockedPlayer == null || !lockedPlayer.isAlive()) { unlockAndReset(); return; }
            this.setNoGravity(true); // stay put / don't sink while staring
            facePlayer(lockedPlayer);
            lockTimer--;
            if (lockTimer <= 0) setGState(2);
        } else {
            // Glide smoothly toward the locked player in full 3D, then bite on contact.
            if (lockedPlayer == null || !lockedPlayer.isAlive()) { unlockAndReset(); return; }
            this.setNoGravity(true); // so he can rise straight up a tower instead of falling into blocks
            this.setDeltaMovement(0, 0, 0);
            facePlayer(lockedPlayer);
            Vec3 to = new Vec3(lockedPlayer.getX() - this.getX(),
                    lockedPlayer.getEyeY() - this.getEyeY(),
                    lockedPlayer.getZ() - this.getZ());
            double dist = to.length();
            if (dist < 1.6D) {
                bite(lockedPlayer);
                return;
            }
            Vec3 step = to.normalize().scale(0.6D); // smooth fast approach, including upward
            this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
        }
    }

    private void bite(Player player) {
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer) player;
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new PacketHandler.JumpscarePacket(4, 100));
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new PacketHandler.LockViewPacket(-1));
        }
        player.hurt(ModDamage.GOLDEN_BITE, 1000.0F);
        this.discard();
    }

    private void unlockAndReset() {
        if (lockedPlayer instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer) lockedPlayer;
            PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new PacketHandler.LockViewPacket(-1));
        }
        lockedPlayer = null;
        setGState(0);
        notLookedTicks = 0;
        this.setNoGravity(false);
    }

    private Player nearestRealPlayer() {
        Player best = null;
        double bestSq = 32.0D * 32.0D;
        for (int i = 0; i < this.level.players().size(); i++) {
            Player p = this.level.players().get(i);
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < bestSq) { bestSq = d; best = p; }
        }
        return best;
    }

    private void facePlayer(Player p) {
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        this.setYBodyRot(yaw); this.setYHeadRot(yaw); this.setYRot(yaw);
    }

    private boolean playerLookingAtMe(Player p) {
        if (this.distanceTo(p) > 32.0D) return false;
        if (!p.hasLineOfSight(this)) return false;
        double dx = this.getX() - p.getX();
        double dy = this.getEyeY() - p.getEyeY();
        double dz = this.getZ() - p.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001D) return false;
        dx /= len; dy /= len; dz /= len;
        Vec3 look = p.getViewVector(1.0F);
        return (look.x * dx + look.y * dy + look.z * dz) > 0.9D;
    }

    // Effectively immune - he leaves on his own. (Bypass-invul sources like /kill still work.)
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.isBypassInvul()) return super.hurt(source, amount);
        return false;
    }

    @Override public void push(net.minecraft.world.entity.Entity e) { /* immovable */ }
    @Override protected void pushEntities() { }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "loco", 2, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().loop("animation.freddyfazbear.sit"));
        return PlayState.CONTINUE;
    }

    @Override public AnimationFactory getFactory() { return factory; }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GState", getGState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setGState(tag.getInt("GState"));
    }
}
