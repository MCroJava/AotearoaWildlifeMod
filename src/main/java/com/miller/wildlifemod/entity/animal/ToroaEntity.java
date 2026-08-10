package com.miller.wildlifemod.entity.animal;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;


public class ToroaEntity extends Animal {

    public enum SoarPhase { GROUNDED, LOW_SWEEP, ASCEND, APEX, DESCEND }

    private static final EntityDataAccessor<Integer> DATA_SOAR_PHASE =
            SynchedEntityData.defineId(ToroaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_RESTING_ON_WATER =
            SynchedEntityData.defineId(ToroaEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState lowSweepPhase1AnimationState = new AnimationState();
    public final AnimationState ascendPhase2AnimationState = new AnimationState();
    public final AnimationState apexPhase3AnimationState = new AnimationState();
    public final AnimationState descendPhase4AnimationState = new AnimationState();
    public final AnimationState waterSittingAnimationState = new AnimationState();
    public final AnimationState standingAnimationState = new AnimationState();
    public final AnimationState waddlingAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    private int phaseTicks;
    private float windHeading;
    private int takeoffRunTicks;
    private int idleCooldown;

    public ToroaEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.windHeading = this.random.nextFloat() * 360.0F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 1.1D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SOAR_PHASE, SoarPhase.GROUNDED.ordinal());
        builder.define(DATA_RESTING_ON_WATER, false);
    }

    public SoarPhase getSoarPhase() {
        return SoarPhase.values()[this.entityData.get(DATA_SOAR_PHASE)];
    }

    public void setSoarPhase(SoarPhase phase) {
        this.entityData.set(DATA_SOAR_PHASE, phase.ordinal());
        this.phaseTicks = 0;
    }

    public boolean isRestingOnWater() {
        return this.entityData.get(DATA_RESTING_ON_WATER);
    }

    public void setRestingOnWater(boolean resting) {
        this.entityData.set(DATA_RESTING_ON_WATER, resting);
        this.setNoGravity(resting);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ToroaRestOnWaterGoal(this));
        this.goalSelector.addGoal(1, new ToroaTakeoffGoal(this));
        this.goalSelector.addGoal(2, new ToroaSoarGoal(this));
        this.goalSelector.addGoal(3, new ToroaFeedGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    public boolean isFlying() {
        return this.getSoarPhase() != SoarPhase.GROUNDED && !this.isRestingOnWater();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.phaseTicks++;
        this.updateAnimationStates();

        if (this.random.nextInt(200) == 0) {
            this.windHeading += (this.random.nextFloat() - 0.5F) * 20.0F;
        }
    }

    private void updateAnimationStates() {
        SoarPhase phase = this.getSoarPhase();

        this.lowSweepPhase1AnimationState.stop();
        this.ascendPhase2AnimationState.stop();
        this.apexPhase3AnimationState.stop();
        this.descendPhase4AnimationState.stop();
        this.waterSittingAnimationState.stop();
        this.standingAnimationState.stop();
        this.waddlingAnimationState.stop();
        this.idleAnimationState.stop();

        if (this.isRestingOnWater()) {
            this.waterSittingAnimationState.startIfStopped(this.tickCount);
            return;
        }

        switch (phase) {
            case LOW_SWEEP -> this.lowSweepPhase1AnimationState.startIfStopped(this.tickCount);
            case ASCEND -> this.ascendPhase2AnimationState.startIfStopped(this.tickCount);
            case APEX -> this.apexPhase3AnimationState.startIfStopped(this.tickCount);
            case DESCEND -> this.descendPhase4AnimationState.startIfStopped(this.tickCount);
            case GROUNDED -> {
                // On land: waddling while moving, standing while stationary, with an
                // occasional fallback to idle so it isn't rigid the whole time grounded.
                if (this.walkAnimation.isMoving()) {
                    this.waddlingAnimationState.startIfStopped(this.tickCount);
                    this.idleCooldown = 0;
                } else if (this.idleCooldown > 0) {
                    this.idleCooldown--;
                    this.idleAnimationState.startIfStopped(this.tickCount);
                } else {
                    this.standingAnimationState.startIfStopped(this.tickCount);
                    if (this.random.nextInt(300) == 0) {
                        this.idleCooldown = 40;
                    }
                }
            }
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // no fall damage for a soaring seabird
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return com.miller.wildlifemod.entity.ModEntityTypes.TOROA.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING); // TODO: rename field in ModEntityTypes
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // TODO: ModSounds.TOROA_CALL.get()
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    static class ToroaRestOnWaterGoal extends Goal {
        private final ToroaEntity toroa;

        public ToroaRestOnWaterGoal(ToroaEntity toroa) {
            this.toroa = toroa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.toroa.isInWater() && this.toroa.getSoarPhase() != SoarPhase.GROUNDED
                    && this.toroa.getRandom().nextInt(100) == 0
                    || (this.toroa.isInWater() && this.toroa.getSoarPhase() == SoarPhase.GROUNDED && !this.toroa.isRestingOnWater() && this.toroa.getRandom().nextInt(100) == 0);
        }

        @Override
        public boolean canContinueToUse() {
            return this.toroa.isRestingOnWater() && this.toroa.isInWater();
        }

        @Override
        public void start() {
            this.toroa.setRestingOnWater(true);
            this.toroa.setSoarPhase(SoarPhase.GROUNDED);
            this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().multiply(0.2D, 0.0D, 0.2D));
        }

        @Override
        public void stop() {
            this.toroa.setRestingOnWater(false);
        }
    }

    static class ToroaTakeoffGoal extends Goal {
        private final ToroaEntity toroa;
        private Vec3 runDirection;

        public ToroaTakeoffGoal(ToroaEntity toroa) {
            this.toroa = toroa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return (this.toroa.isRestingOnWater() || this.toroa.onGround())
                    && this.toroa.getSoarPhase() == SoarPhase.GROUNDED
                    && this.toroa.getRandom().nextInt(120) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.toroa.takeoffRunTicks > 0;
        }

        @Override
        public void start() {
            this.toroa.setRestingOnWater(false);
            this.toroa.takeoffRunTicks = 30;
            float headingRad = this.toroa.windHeading * Mth.DEG_TO_RAD;
            this.runDirection = new Vec3(-Mth.sin(headingRad), 0.0D, Mth.cos(headingRad));
        }

        @Override
        public void tick() {
            this.toroa.takeoffRunTicks--;
            double liftProgress = 1.0D - (this.toroa.takeoffRunTicks / 30.0D);
            Vec3 accel = this.runDirection.scale(0.12D).add(0.0D, 0.02D + liftProgress * 0.05D, 0.0D);
            this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().add(accel));

            if (this.toroa.takeoffRunTicks <= 0) {
                this.toroa.setSoarPhase(SoarPhase.ASCEND);
            }
        }
    }

    /** The dynamic-soaring loop: low sweep -> ascend -> apex -> descend -> repeat. */
    static class ToroaSoarGoal extends Goal {
        private static final int LOW_SWEEP_DURATION = 70;
        private static final int ASCEND_DURATION = 60;
        private static final int APEX_DURATION = 90;
        private static final int DESCEND_DURATION = 60;

        private final ToroaEntity toroa;

        public ToroaSoarGoal(ToroaEntity toroa) {
            this.toroa = toroa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            SoarPhase phase = this.toroa.getSoarPhase();
            return phase == SoarPhase.LOW_SWEEP || phase == SoarPhase.ASCEND
                    || phase == SoarPhase.APEX || phase == SoarPhase.DESCEND;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            SoarPhase phase = this.toroa.getSoarPhase();
            float headingRad = this.toroa.windHeading * Mth.DEG_TO_RAD;
            Vec3 forward = new Vec3(-Mth.sin(headingRad), 0.0D, Mth.cos(headingRad));

            switch (phase) {
                case ASCEND -> {
                    Vec3 accel = forward.scale(0.05D).add(0.0D, 0.06D, 0.0D);
                    this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().scale(0.98D).add(accel));
                    if (this.toroa.phaseTicks > ASCEND_DURATION) {
                        this.toroa.setSoarPhase(SoarPhase.APEX);
                    }
                }
                case APEX -> {
                    double bankAngle = Math.sin(this.toroa.phaseTicks * 0.05D) * 0.15D;
                    Vec3 banked = forward.yRot((float) bankAngle);
                    Vec3 accel = banked.scale(0.09D).add(0.0D, -0.01D, 0.0D);
                    this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().scale(0.995D).add(accel));
                    if (this.toroa.phaseTicks > APEX_DURATION) {
                        this.toroa.setSoarPhase(SoarPhase.DESCEND);
                    }
                }
                case DESCEND -> {
                    Vec3 accel = forward.scale(0.07D).add(0.0D, -0.05D, 0.0D);
                    this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().scale(0.98D).add(accel));
                    boolean nearSurface = this.toroa.getY() < this.toroa.level().getSeaLevel() + 3.0D;
                    if (this.toroa.phaseTicks > DESCEND_DURATION || nearSurface) {
                        this.toroa.setSoarPhase(SoarPhase.LOW_SWEEP);
                    }
                }
                case LOW_SWEEP -> {
                    Vec3 accel = forward.scale(0.1D).add(0.0D, 0.0D, 0.0D);
                    this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().scale(0.99D).add(accel));
                    if (this.toroa.phaseTicks > LOW_SWEEP_DURATION) {
                        this.toroa.setSoarPhase(SoarPhase.ASCEND);
                    }
                }
                default -> {}
            }

            this.toroa.getLookControl().setLookAt(
                    this.toroa.getX() + forward.x, this.toroa.getY(), this.toroa.getZ() + forward.z);
        }
    }

    static class ToroaFeedGoal extends Goal {
        private final ToroaEntity toroa;

        public ToroaFeedGoal(ToroaEntity toroa) {
            this.toroa = toroa;
        }

        @Override
        public boolean canUse() {
            return this.toroa.getSoarPhase() == SoarPhase.LOW_SWEEP
                    && this.toroa.getY() < this.toroa.level().getSeaLevel() + 4.0D;
        }

        @Override
        public void tick() {
            List<AbstractFish> nearby = this.toroa.level().getEntitiesOfClass(
                    AbstractFish.class, this.toroa.getBoundingBox().inflate(3.0D, 2.0D, 3.0D));
            if (!nearby.isEmpty()) {
                nearby.get(0).discard(); // TODO: replace with a proper snatch/feed effect instead of despawn
                this.toroa.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT);
            }
        }
    }
}