package com.redstonedev.fivenightsatminecraft.entity;

import com.redstonedev.fivenightsatminecraft.network.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public abstract class AbstractAnimatronic extends Monster implements IAnimatable {

    public enum State { DORMANT, SPOTTED, AGGRESSIVE }

    protected static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(AbstractAnimatronic.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> DATA_POSTURE =
            SynchedEntityData.defineId(AbstractAnimatronic.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> DATA_MOVING =
            SynchedEntityData.defineId(AbstractAnimatronic.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> DATA_AGGRO =
            SynchedEntityData.defineId(AbstractAnimatronic.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> DATA_CLIMBING =
            SynchedEntityData.defineId(AbstractAnimatronic.class, EntityDataSerializers.BOOLEAN);

    protected final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    private int stepCooldown = 0;
    private int passiveTicks = 0;        // time spent not aggressive
    private int aggroNoTouchTicks = 0;   // time aggressive without landing a hit
    protected boolean pendingDespawn = false;
    private double lastX, lastZ;
    protected boolean stalking;

    protected AbstractAnimatronic(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
        this.maxUpStep = 1.0F;
        this.stalking = this.random.nextFloat() < stalkBias();
    }

    public static AttributeSupplier.Builder baseAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100000.0D)
                .add(Attributes.ATTACK_DAMAGE, 1000.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // === Per-character configuration ==========================================
    protected abstract int charId();
    protected boolean canClimb()          { return false; }
    protected boolean canCrouch()         { return false; }
    protected boolean canCrawl()          { return false; }
    protected boolean breaksPlayerBlocks(){ return false; }
    protected boolean usesStandoff()      { return false; }
    protected float standHeight()         { return 2.6F; }
    protected float stalkBias()           { return 0.5F; }
    protected double walkSpeed()          { return 0.10D; }
    protected double runSpeed()           { return 0.26D; }
    protected double crouchSpeed()        { return 0.24D; }
    protected double crawlSpeed()         { return 0.12D; }
    protected abstract SoundEvent footstepSound();
    protected void onPassiveTick(Player nearest) {}      // per-character ambient sounds
    protected void onSpotSound() {}                       // e.g. Freddy "spots you"
    protected void tickSpotted(Player player) { setState(State.AGGRESSIVE); } // Freddy overrides

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, State.DORMANT.ordinal());
        this.entityData.define(DATA_POSTURE, 0);
        this.entityData.define(DATA_MOVING, false);
        this.entityData.define(DATA_AGGRO, false);
        this.entityData.define(DATA_CLIMBING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        if (canClimb()) {
            WallClimberNavigation nav = new WallClimberNavigation(this, level);
            nav.setCanOpenDoors(true);
            nav.setCanPassDoors(true);
            return nav;
        }
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    // === Accessors ============================================================
    public State getState() {
        int i = this.entityData.get(DATA_STATE);
        State[] v = State.values();
        return v[Math.max(0, Math.min(v.length - 1, i))];
    }
    public void setState(State s) {
        this.entityData.set(DATA_STATE, s.ordinal());
        this.entityData.set(DATA_AGGRO, s == State.AGGRESSIVE);
    }
    public int getPosture() { return this.entityData.get(DATA_POSTURE); }
    public boolean isMovingAnim() { return this.entityData.get(DATA_MOVING); }
    public boolean isAggressive() { return this.entityData.get(DATA_AGGRO); }
    public boolean isClimbing() { return this.entityData.get(DATA_CLIMBING); }
    @Override public boolean onClimbable() { return canClimb() && this.isClimbing(); }
    @Override public AnimationFactory getFactory() { return factory; }

    // === Dimensions / posture =================================================
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        int p = this.entityData.get(DATA_POSTURE);
        if (p == 2) return EntityDimensions.scalable(0.9F, 0.9F);
        if (p == 1) return EntityDimensions.scalable(0.9F, 1.9F);
        return EntityDimensions.scalable(0.9F, standHeight());
    }

    private int clearanceAt(BlockPos base) {
        int clear = 0;
        for (int i = 0; i < 4; i++) {
            BlockPos p = base.above(i);
            if (this.level.getBlockState(p).getCollisionShape(this.level, p).isEmpty()) clear++;
            else break;
        }
        return clear;
    }

    // === Tick =================================================================
    @Override
    public void aiStep() {
        if (!this.level.isClientSide && pendingDespawn) { this.discard(); return; }
        super.aiStep();
        if (this.level.isClientSide) return;

        if (stepCooldown > 0) stepCooldown--;
        if (canClimb()) this.setClimbingFlag(this.horizontalCollision);

        // Posture (only if this character can crouch/crawl), with look-ahead toward target.
        int posture = 0;
        if (canCrouch() || canCrawl()) {
            int hereClear = clearanceAt(this.blockPosition());
            int clear = hereClear;
            LivingEntity tgt = this.getTarget();
            if (tgt != null) {
                double tdx = tgt.getX() - this.getX();
                double tdz = tgt.getZ() - this.getZ();
                double tlen = Math.sqrt(tdx * tdx + tdz * tdz);
                if (tlen > 0.001D) {
                    int ox = (int) Math.round(tdx / tlen);
                    int oz = (int) Math.round(tdz / tlen);
                    if (ox != 0 || oz != 0) clear = Math.min(hereClear, clearanceAt(this.blockPosition().offset(ox, 0, oz)));
                }
            }
            if (clear <= 1) { if (canCrawl()) posture = 2; }
            else if (clear == 2) { if (canCrouch()) posture = 1; }
        }
        if (posture != this.entityData.get(DATA_POSTURE)) {
            this.entityData.set(DATA_POSTURE, posture);
            this.refreshDimensions();
            this.getNavigation().stop();
        }

        // Moving flag from actual displacement.
        double mdx = this.getX() - lastX, mdz = this.getZ() - lastZ;
        boolean moving = (mdx * mdx + mdz * mdz) > 1.0E-5D;
        lastX = this.getX(); lastZ = this.getZ();
        this.entityData.set(DATA_MOVING, moving);
        if (moving && stepCooldown <= 0) {
            this.level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    footstepSound(), SoundSource.HOSTILE, 1.0F, 1.0F);
            stepCooldown = 11;
        }

        Player player = nearestVisiblePlayer();
        State state = getState();

        if (state != State.DORMANT) breakAdjacentDoors();
        if (breaksPlayerBlocks() && state == State.AGGRESSIVE && player != null) tryBreakPlayerBlocks(player);

        // Speed from state + posture.
        double speed;
        if (posture == 2) speed = crawlSpeed();
        else if (posture == 1) speed = crouchSpeed();
        else if (state == State.AGGRESSIVE) speed = runSpeed();
        else speed = walkSpeed();
        AttributeInstance attr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && Math.abs(attr.getBaseValue() - speed) > 1e-6) attr.setBaseValue(speed);

        if (state == State.DORMANT) {
            onPassiveTick(player);
            doStalkOrWander(player);
            passiveTicks++;
            if (player != null && (canSee(player) || isPlayerLookingAt(player))) {
                if (usesStandoff()) { setState(State.SPOTTED); onSpotSound(); }
                else setState(State.AGGRESSIVE);
            }
            if (passiveTicks >= 3600) this.discard(); // 3 min unbothered -> leave
        } else if (state == State.SPOTTED) {
            passiveTicks++;
            tickSpotted(player);
            if (passiveTicks >= 3600) this.discard();
        } else { // AGGRESSIVE
            if (player != null) this.setTarget(player);
            else {
                LivingEntity t = this.getTarget();
                if (t == null || (t instanceof Player && (((Player) t).isCreative() || ((Player) t).isSpectator())))
                    this.setTarget(null);
            }
            aggroNoTouchTicks++;
            if (aggroNoTouchTicks >= 2400) this.discard(); // 2 min without a hit -> leave
        }
    }

    protected void setClimbingFlag(boolean c) { this.entityData.set(DATA_CLIMBING, c); }

    private void doStalkOrWander(Player player) {
        // Wandering is handled by the stroll goal. Stalkers instead creep toward the player,
        // stopping a few blocks away to watch.
        if (!stalking || player == null) return;
        if (this.tickCount % 20 != 0) return;
        double d = this.distanceTo(player);
        if (d > 7.0D) this.getNavigation().moveTo(player, 0.8D);
        else this.getNavigation().stop();
    }

    // === Attack -> jumpscare ==================================================
    @Override
    public boolean doHurtTarget(Entity target) {
        this.swing(InteractionHand.MAIN_HAND);
        boolean ok = super.doHurtTarget(target);
        if (ok) {
            aggroNoTouchTicks = 0;
            if (target instanceof Player) {
                if (target instanceof ServerPlayer) {
                    ServerPlayer sp = (ServerPlayer) target;
                    PacketHandler.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> sp),
                            new PacketHandler.JumpscarePacket(charId(), 100));
                }
                if (!target.isAlive()) pendingDespawn = true;
            }
        }
        return ok;
    }

    // === Damage immunity (shared) ============================================
    // Immune to: fire (not lava), suffocation, drowning, projectiles/arrows, bare hands,
    // and every non-netherite tool/weapon. Only a netherite sword/axe, lava, and other
    // non-living damage sources get through.
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source == DamageSource.IN_FIRE || source == DamageSource.ON_FIRE) return false;
        if (source == DamageSource.IN_WALL || source == DamageSource.DROWN) return false;
        if (source.isProjectile()) return false;
        Entity direct = source.getEntity();
        if (direct instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) direct;
            Item item = attacker.getMainHandItem().getItem();
            boolean netherite = item == Items.NETHERITE_SWORD || item == Items.NETHERITE_AXE;
            if (!netherite) return false; // bare hands + any non-netherite tool do nothing
        }
        return super.hurt(source, amount);
    }

    // === Detection ============================================================
    protected Player nearestVisiblePlayer() {
        Player best = null;
        double bestSq = 48.0D * 48.0D;
        for (int i = 0; i < this.level.players().size(); i++) {
            Player p = this.level.players().get(i);
            if (p.isCreative() || p.isSpectator() || !p.isAlive()) continue;
            double d = p.distanceToSqr(this);
            if (d < bestSq) { bestSq = d; best = p; }
        }
        return best;
    }

    protected boolean canSee(Player p) {
        if (this.distanceTo(p) > 32.0D) return false;
        if (!this.hasLineOfSight(p)) return false;
        Vec3 toPlayer = p.position().subtract(this.position()).normalize();
        Vec3 facing = Vec3.directionFromRotation(0.0F, this.getYRot());
        return (toPlayer.x * facing.x + toPlayer.z * facing.z) > 0.2D;
    }

    protected boolean isPlayerLookingAt(Player p) {
        if (this.distanceTo(p) > 40.0D) return false;
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

    protected void lockYawTo(Player p) {
        double dx = p.getX() - this.getX();
        double dz = p.getZ() - this.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * (180D / Math.PI)) - 90.0F;
        this.setYBodyRot(yaw); this.setYHeadRot(yaw); this.setYRot(yaw);
    }

    // === Block / door breaking ================================================
    private void breakAdjacentDoors() {
        BlockPos base = this.blockPosition();
        for (int dy = 0; dy <= 2; dy++) {
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos p = base.above(dy).relative(dir);
                if (this.level.getBlockState(p).getBlock() instanceof net.minecraft.world.level.block.DoorBlock) {
                    this.level.destroyBlock(p, false);
                }
            }
        }
    }

    /** Foxy: break the blocks the player is crammed into when they hide in a 1/2-block gap. */
    private void tryBreakPlayerBlocks(Player player) {
        if (this.tickCount % 10 != 0) return;
        if (this.distanceTo(player) > 4.0D) return;
        int clear = clearanceAt(player.blockPosition());
        if (clear > 2) return; // only when they're squeezed into a low gap
        BlockPos around = player.blockPosition().above(2);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos p = around.offset(dx, 0, dz);
                if (!this.level.getBlockState(p).isAir()
                        && this.level.getBlockState(p).getDestroySpeed(this.level, p) >= 0) {
                    this.level.destroyBlock(p, true, this);
                }
            }
        }
    }

    @Override protected float getSoundVolume() { return 1.0F; }

    // === NBT ==================================================================
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("State", this.entityData.get(DATA_STATE));
        tag.putBoolean("Stalking", stalking);
        tag.putInt("PassiveTicks", passiveTicks);
        tag.putInt("AggroNoTouch", aggroNoTouchTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_STATE, tag.getInt("State"));
        stalking = tag.getBoolean("Stalking");
        passiveTicks = tag.getInt("PassiveTicks");
        aggroNoTouchTicks = tag.getInt("AggroNoTouch");
    }
}
