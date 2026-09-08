package com.miller.wildlifemod.entity.animal;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
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

//This class is in charge of the Toroa mob, It defines how it behaves in game

public class ToroaEntity extends Animal {

    // Whether this particular Toroa is currently floating/resting on water.
    private static final EntityDataAccessor<Boolean> DATA_RESTING_ON_WATER =
            SynchedEntityData.defineId(ToroaEntity.class, EntityDataSerializers.BOOLEAN);

    //these are the animation states that were defined in the EntityRenderer class
    public final AnimationState lowSweepPhase1AnimationState = new AnimationState();
    public final AnimationState ascendPhase2AnimationState = new AnimationState();
    public final AnimationState apexPhase3AnimationState = new AnimationState();
    public final AnimationState descendPhase4AnimationState = new AnimationState();
    public final AnimationState waterSittingAnimationState = new AnimationState();
    public final AnimationState standingAnimationState = new AnimationState();
    public final AnimationState waddlingAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    // How fast it needs to be moving up or down before we
    // count that as "ascending"/"descending" rather than just level flight.
    // Small, so gentle drifting doesn't cause flickering between animations.
    private static final double VERTICAL_ANIMATION_THRESHOLD = 0.02D;

    private int takeoffRunTicks; // Counts down during the running/paddling takeoff start.
    private int idleCooldown;    // Counts down during the occasional idle animation while grounded.

    public ToroaEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        // FlyingMoveControl "10" controls how sharply it can turn,
        // and "false" means it can't hover in place (it always needs forward motion).
        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 1.1D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D); // Slow and clumsy on the ground.
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_RESTING_ON_WATER, false);
    }

    public boolean isRestingOnWater() {
        return this.entityData.get(DATA_RESTING_ON_WATER);
    }

    public void setRestingOnWater(boolean resting) {
        this.entityData.set(DATA_RESTING_ON_WATER, resting);
    }

    // True whenever it's airborne and not currently floating on water.
    public boolean isFlying() {
        return !this.onGround() && !this.isRestingOnWater();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true); // Allowed to float on the surface of water.
        return navigation;
    }
    //these are goals that affect how the entity behaves.
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new ToroaRestOnWaterGoal(this));
        this.goalSelector.addGoal(1, new ToroaTakeoffGoal(this));
        this.goalSelector.addGoal(2, new ToroaFlyGoal(this)); // Simple random wandering flight — replaces the old soaring cycle.
        this.goalSelector.addGoal(3, new ToroaFeedGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }
    //tells the game what food Toroa can eat
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
        this.updateAnimationStates();
    }


     //Decides which animation to play. While flying, this is based purely on
     //vertical speed (getDeltaMovement().y) — moving up plays Ascend, moving
     //down plays Descend, and anything in between (roughly level) plays Apex.
     //While grounded, it falls back to the same waddling/standing/idle logic
     //as before.

    private void updateAnimationStates() {
        this.lowSweepPhase1AnimationState.stop(); // Never used in this version.
        this.ascendPhase2AnimationState.stop();
        this.apexPhase3AnimationState.stop();
        this.descendPhase4AnimationState.stop();
        this.waterSittingAnimationState.stop();
        this.standingAnimationState.stop();
        this.waddlingAnimationState.stop();
        this.idleAnimationState.stop();

        if (this.isRestingOnWater()) {
            this.waterSittingAnimationState.startIfStopped(this.tickCount);
        } else if (this.isFlying()) {
            double verticalSpeed = this.getDeltaMovement().y;
            if (verticalSpeed > VERTICAL_ANIMATION_THRESHOLD) {
                this.ascendPhase2AnimationState.startIfStopped(this.tickCount);
            } else if (verticalSpeed < -VERTICAL_ANIMATION_THRESHOLD) {
                this.descendPhase4AnimationState.startIfStopped(this.tickCount);
            } else {
                this.apexPhase3AnimationState.startIfStopped(this.tickCount); // Roughly level flight.
            }
        } else {
            // On the ground, it is waddling while moving, standing while stationary, with an
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

    @Override
    public boolean isNoGravity() {
        return this.isFlying();
    }

    // No fall damage.
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
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

    // Custom goal makes it occasionally settle down to float and rest on open water.
    static class ToroaRestOnWaterGoal extends Goal {
        private final ToroaEntity toroa;

        public ToroaRestOnWaterGoal(ToroaEntity toroa) {
            this.toroa = toroa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.toroa.isInWater() && !this.toroa.isRestingOnWater()
                    && this.toroa.getRandom().nextInt(100) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.toroa.isRestingOnWater() && this.toroa.isInWater();
        }

        @Override
        public void start() {
            this.toroa.setRestingOnWater(true);
            // Slow the bird down a lot when it settles, so it doesn't keep skidding
            // across the water's surface at flying speed.
            this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().multiply(0.2D, 0.0D, 0.2D));
        }

        @Override
        public void stop() {
            this.toroa.setRestingOnWater(false);
        }
    }
      //the Toroa will
      //need a running (or paddling) start first. Once the run finishes, we just
      //give it an upward boost and let ToroaFlyGoal take over from there.

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
                    && !this.toroa.isFlying()
                    && this.toroa.getRandom().nextInt(120) == 0; // Small random chance each tick.
        }

        @Override
        public boolean canContinueToUse() {
            return this.toroa.takeoffRunTicks > 0;
        }

        @Override
        public void start() {
            this.toroa.setRestingOnWater(false);
            this.toroa.takeoffRunTicks = 30; // Run for 30 ticks (1.5 seconds) before lifting off.
            RandomSource random = this.toroa.getRandom();
            float randomAngle = random.nextFloat() * (float) (Math.PI * 2);
            this.runDirection = new Vec3(Math.cos(randomAngle), 0.0D, Math.sin(randomAngle));
        }

        @Override
        public void tick() {
            this.toroa.takeoffRunTicks--;
            double liftProgress = 1.0D - (this.toroa.takeoffRunTicks / 30.0D);
            Vec3 accel = this.runDirection.scale(0.12D).add(0.0D, 0.02D + liftProgress * 0.05D, 0.0D);
            this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().add(accel));

            if (this.toroa.takeoffRunTicks <= 0) {
                // One last upward push to properly leave the ground/water — ToroaFlyGoal
                // takes over from the very next tick once it's actually airborne.
                this.toroa.setDeltaMovement(this.toroa.getDeltaMovement().add(0.0D, 0.3D, 0.0D));
            }
        }
    }


    static class ToroaFlyGoal extends Goal {
        private final ToroaEntity toroa;

        public ToroaFlyGoal(ToroaEntity toroa) {
            this.toroa = toroa;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.toroa.isFlying();
        }

        @Override
        public boolean canContinueToUse() {
            return this.toroa.isFlying();
        }

        @Override
        public void tick() {
            // Once the navigation has finished (or never had) a destination, pick
            // a new random point to fly toward.
            if (this.toroa.getNavigation().isDone()) {
                RandomSource random = this.toroa.getRandom();
                double x = this.toroa.getX() + (random.nextDouble() - 0.5D) * 40.0D;
                double y = this.toroa.getY() + (random.nextDouble() - 0.3D) * 12.0D;
                double z = this.toroa.getZ() + (random.nextDouble() - 0.5D) * 40.0D;
                this.toroa.getNavigation().moveTo(x, y, z, 1.2D);
            }
        }
    }

    //Custom goal: while flying low over the water, snatch up nearby fish
    static class ToroaFeedGoal extends Goal {
        private final ToroaEntity toroa;

        public ToroaFeedGoal(ToroaEntity toroa) {
            this.toroa = toroa;
        }

        @Override
        public boolean canUse() {
            return this.toroa.isFlying()
                    && this.toroa.getY() < this.toroa.level().getSeaLevel() + 4.0D;
        }

        @Override
        public void tick() {
            List<AbstractFish> nearby = this.toroa.level().getEntitiesOfClass(
                    AbstractFish.class, this.toroa.getBoundingBox().inflate(3.0D, 2.0D, 3.0D));
            if (!nearby.isEmpty()) {
                nearby.get(0).discard(); // "Catches" the first fish found
                this.toroa.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT);
            }
        }
    }
}