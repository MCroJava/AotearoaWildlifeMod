package com.miller.wildlifemod.entity.animal;

import com.miller.wildlifemod.entity.WildlifeAnimationUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

//This class is in charge of the Kekeno mob, It defines how it behaves in game

public class KekenoEntity extends Animal {

    private static final EntityDataAccessor<Boolean> DATA_HAULED_OUT =
            SynchedEntityData.defineId(KekenoEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState runningAnimationState = new AnimationState();
    public final AnimationState swimmingAnimationState = new AnimationState();

    public KekenoEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        // Kekeno are totally fine going in and out of the sea.
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new KekenoMoveControl(this); // Our own custom movement style, defined below.
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D); // Fur seals can be territorial, so we give a small bite.
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_HAULED_OUT, false); // Starts off false — not hauled out when first spawned.
    }

    // Small "getter" and "setter" pair — the normal Java way of safely reading and
    // writing a piece of data from outside the class.
    public boolean isHauledOut() {
        return this.entityData.get(DATA_HAULED_OUT);
    }

    public void setHauledOut(boolean hauledOut) {
        this.entityData.set(DATA_HAULED_OUT, hauledOut);
    }

    /**
     * "Amphibious" navigation automatically handles switching between walking on
     * land and swimming through water, which is exactly what a fur seal needs.
     */
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }
    //creates a set of goals that affect the entities Ai
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BreatheAirGoal()); // Surfaces for air if it's running low.
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.8D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new KekenoHuntFishGoal(this)); // Chases and "eats" nearby fish.
        // AvoidEntityGoal makes it swim/waddle away from players sometimes — real
        // seals are naturally a bit wary of people, especially when not hauled out.
        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.0D, 1.2D,
                (entity) -> !this.isHauledOut() && this.random.nextInt(4) == 0));
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0D, 8)); // Swims around aimlessly when nothing else to do.
        // Only wander around on land while it's actually hauled out — otherwise this
        // goal just won't activate, thanks to the canUse() override below.
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7D) {
            @Override
            public boolean canUse() {
                return KekenoEntity.this.isHauledOut() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    // isPushedByFluid() = false stops water currents from shoving the Kekeno around —
    // it should be swimming deliberately, not getting dragged.
    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    // Lets the Kekeno stay underwater indefinitely without its air meter mattering
    // for drowning purposes — it is handled, the "needs to breathe" behavior is done ourselves
    // instead, through BreatheAirGoal below.
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    /**
     * Every tick, decide which animation should be playing based on what the
     * Kekeno is actually doing right now (swimming vs. walking/idle/running on land).
     */
    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater() && this.walkAnimation.isMoving()) {
            // walkAnimation.isMoving() is a built-in Minecraft check that's true
            // whenever the entity's legs/body would visually be moving.
            this.swimmingAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            this.walkingAnimationState.stop();
            this.runningAnimationState.stop();
        } else {
            this.swimmingAnimationState.stop();
            // Hands off the idle/walking/running decision to a shared helper method,
            // since Kiwi and Hoiho both need this exact same logic on land.
            WildlifeAnimationUtils.updateLocomotion(this, this.idleAnimationState, this.walkingAnimationState, this.runningAnimationState);
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return com.miller.wildlifemod.entity.ModEntityTypes.KEKENO.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // TODO: ModSounds.KEKENO_BARK.get()
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    // Overriding this just swaps in a slightly randomized pitch for the splash sound
    // each time it swims, so it doesn't sound identical and robotic every time.
    @Override
    protected void playSwimSound(float volume) {
        this.playSound(SoundEvents.GENERIC_SWIM, volume, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
    }

    /**
     * A MoveControl decides HOW an entity physically moves toward wherever its
     * navigation is currently taking it — this custom one gives the Kekeno a
     * gentle upward push while swimming, so it drifts more like a real seal
     * instead of gliding in a perfectly flat line.
     */
    static class KekenoMoveControl extends MoveControl {
        private final KekenoEntity kekeno;

        public KekenoMoveControl(KekenoEntity kekeno) {
            super(kekeno);
            this.kekeno = kekeno;
        }

        @Override
        public void tick() {
            if (this.kekeno.isInWater()) {
                this.kekeno.setDeltaMovement(this.kekeno.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
                if (this.operation == Operation.MOVE_TO && !this.kekeno.getNavigation().isDone()) {
                    // Smoothly ramps the current speed toward the target speed instead
                    // of snapping to it instantly — Mth.lerp() blends two numbers together.
                    float speed = (float) (this.speedModifier * this.kekeno.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    this.kekeno.setSpeed(net.minecraft.util.Mth.lerp(0.125F, this.kekeno.getSpeed(), speed));
                }
            }
            super.tick(); // Still let the normal movement logic run afterward.
        }
    }

    /**
     * Custom goal: if the Kekeno's air supply gets low while it's underwater,
     * head toward the surface to breathe.
     */
    class BreatheAirGoal extends Goal {
        public BreatheAirGoal() {
            this.setFlags(java.util.EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return KekenoEntity.this.getAirSupply() < KekenoEntity.this.getMaxAirSupply() / 3
                    && KekenoEntity.this.isInWater();
        }

        @Override
        public void tick() {
            // DefaultRandomPos picks a nearby point roughly in the given direction —
            // here we aim "4 blocks up" so the seal naturally heads toward the surface.
            Vec3 target = DefaultRandomPos.getPosTowards(KekenoEntity.this, 4, 2, new Vec3(
                    KekenoEntity.this.getX(), KekenoEntity.this.getY() + 4.0D, KekenoEntity.this.getZ()), Math.PI / 2);
            if (target != null) {
                KekenoEntity.this.getNavigation().moveTo(target.x, target.y, target.z, 1.2D);
            }
        }
    }

    /**
     * Custom goal: while swimming, look for nearby fish and chase one down.
     */
    static class KekenoHuntFishGoal extends Goal {
        private final KekenoEntity kekeno;
        private AbstractFish target; // The specific fish currently being chased, if any.

        public KekenoHuntFishGoal(KekenoEntity kekeno) {
            this.kekeno = kekeno;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.kekeno.isInWater() || this.kekeno.isHauledOut()) return false;
            // getEntitiesOfClass finds every entity of a given type within a box around
            // the Kekeno — here, a 10-block bubble in every direction.
            java.util.List<AbstractFish> nearby = this.kekeno.level().getEntitiesOfClass(
                    AbstractFish.class, this.kekeno.getBoundingBox().inflate(10.0D));
            if (nearby.isEmpty()) return false;
            this.target = nearby.get(this.kekeno.getRandom().nextInt(nearby.size())); // Pick one at random.
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.target.isAlive() && this.kekeno.isInWater();
        }

        @Override
        public void tick() {
            if (this.target == null) return;
            this.kekeno.getNavigation().moveTo(this.target, 1.3D); // Chase toward the fish.
            if (this.kekeno.distanceToSqr(this.target) < 2.5D) {
                // Close enough to "catch" it — for now this just removes the fish.
                this.target.discard();
                this.kekeno.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT);
            }
        }

        @Override
        public void stop() {
            this.target = null; // Forget the target once the goal ends, so we don't chase a stale reference.
        }
    }
}