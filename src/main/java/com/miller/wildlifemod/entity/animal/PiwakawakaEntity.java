package com.miller.wildlifemod.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;


public class PiwakawakaEntity extends Animal {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState perchedAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> DATA_PERCHED =
            SynchedEntityData.defineId(PiwakawakaEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private LivingEntity followTarget;

    public PiwakawakaEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setPathfindingMalus(PathType.FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 3.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PERCHED, false);
    }

    public boolean isPerched() {
        return this.entityData.get(DATA_PERCHED);
    }

    public void setPerched(boolean perched) {
        this.entityData.set(DATA_PERCHED, perched);
        this.setNoGravity(perched);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(BlockPos pos) {
                return !this.level.getBlockState(pos.below()).isAir();
            }
        };
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 2.2D));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new PiwakawakaFollowGoal(this, 6.0D, 12.0D));
        this.goalSelector.addGoal(3, new PiwakawakaDartGoal(this));
        this.goalSelector.addGoal(4, new PiwakawakaPerchGoal(this));
        this.goalSelector.addGoal(5, new PiwakawakaFlitGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 5.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public boolean isFlying() {
        return !this.onGround() && !this.isPerched();
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D;

        if (this.isPerched()) {
            this.perchedAnimationState.startIfStopped(this.tickCount);
            this.flyingAnimationState.stop();
            this.idleAnimationState.stop();
        } else if (moving || !this.onGround()) {
            this.flyingAnimationState.startIfStopped(this.tickCount);
            this.perchedAnimationState.stop();
            this.idleAnimationState.stop();
        } else {
            this.idleAnimationState.startIfStopped(this.tickCount);
            this.perchedAnimationState.stop();
            this.flyingAnimationState.stop();
        }
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {
        // fantail are birds — no fall damage
    }

    @Override
    public boolean causeFallDamage(double distance, float multiplier, DamageSource source) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return com.miller.wildlifemod.entity.ModEntityTypes.PIWAKAWAKA.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // TODO: ModSounds.PIWAKAWAKA_CHIRP.get()
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    static class PiwakawakaFollowGoal extends Goal {
        private final PiwakawakaEntity fantail;
        private final double followRange;
        private final double giveUpRange;

        public PiwakawakaFollowGoal(PiwakawakaEntity fantail, double followRange, double giveUpRange) {
            this.fantail = fantail;
            this.followRange = followRange;
            this.giveUpRange = giveUpRange;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.fantail.isPerched()) return false;
            if (this.fantail.followTarget != null && this.fantail.followTarget.isAlive()
                    && this.fantail.distanceTo(this.fantail.followTarget) < this.giveUpRange) {
                return true;
            }
            List<LivingEntity> candidates = this.fantail.level().getEntitiesOfClass(
                    LivingEntity.class, this.fantail.getBoundingBox().inflate(this.followRange),
                    e -> (e instanceof Player || e instanceof net.minecraft.world.entity.animal.Animal) && e != this.fantail);
            if (candidates.isEmpty()) return false;
            this.fantail.followTarget = candidates.get(this.fantail.getRandom().nextInt(candidates.size()));
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.fantail.followTarget != null && this.fantail.followTarget.isAlive()
                    && this.fantail.distanceTo(this.fantail.followTarget) < this.giveUpRange
                    && !this.fantail.isPerched();
        }

        @Override
        public void tick() {
            if (this.fantail.followTarget == null) return;
            double angle = (this.fantail.tickCount * 0.15D) % (Math.PI * 2);
            double orbitRadius = 2.0D + this.fantail.getRandom().nextDouble();
            double x = this.fantail.followTarget.getX() + Math.cos(angle) * orbitRadius;
            double y = this.fantail.followTarget.getY() + 1.0D + Math.sin(this.fantail.tickCount * 0.3D) * 0.5D;
            double z = this.fantail.followTarget.getZ() + Math.sin(angle) * orbitRadius;
            this.fantail.getNavigation().moveTo(x, y, z, 1.3D);
        }

        @Override
        public void stop() {
            this.fantail.followTarget = null;
        }
    }

    static class PiwakawakaDartGoal extends Goal {
        private final PiwakawakaEntity fantail;
        private int dartCooldown;

        public PiwakawakaDartGoal(PiwakawakaEntity fantail) {
            this.fantail = fantail;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.fantail.isPerched() || this.fantail.followTarget == null) return false;
            if (this.dartCooldown > 0) {
                this.dartCooldown--;
                return false;
            }
            return this.fantail.getRandom().nextInt(15) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            RandomSource random = this.fantail.getRandom();
            Vec3 dart = new Vec3(
                    (random.nextDouble() - 0.5D) * 1.4D,
                    (random.nextDouble() - 0.3D) * 0.8D,
                    (random.nextDouble() - 0.5D) * 1.4D);
            this.fantail.setDeltaMovement(this.fantail.getDeltaMovement().add(dart));
            this.dartCooldown = 10 + random.nextInt(20);
        }
    }

    static class PiwakawakaPerchGoal extends Goal {
        private final PiwakawakaEntity fantail;
        private int perchTicks;

        public PiwakawakaPerchGoal(PiwakawakaEntity fantail) {
            this.fantail = fantail;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (this.fantail.isPerched()) return false;
            if (!this.fantail.onGround() && this.fantail.getRandom().nextInt(300) == 0) {
                net.minecraft.world.level.block.state.BlockState below =
                        this.fantail.level().getBlockState(this.fantail.blockPosition().below());
                return !below.isAir();
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.perchTicks > 0;
        }

        @Override
        public void start() {
            this.perchTicks = 60 + this.fantail.getRandom().nextInt(100);
            this.fantail.setPerched(true);
            this.fantail.setDeltaMovement(Vec3.ZERO);
        }

        @Override
        public void tick() {
            this.perchTicks--;
        }

        @Override
        public void stop() {
            this.fantail.setPerched(false);
        }
    }

    static class PiwakawakaFlitGoal extends Goal {
        private final PiwakawakaEntity fantail;

        public PiwakawakaFlitGoal(PiwakawakaEntity fantail) {
            this.fantail = fantail;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.fantail.isPerched() && this.fantail.getNavigation().isDone();
        }

        @Override
        public void start() {
            RandomSource random = this.fantail.getRandom();
            Vec3 target = new Vec3(
                    this.fantail.getX() + (random.nextDouble() - 0.5D) * 10.0D,
                    this.fantail.getY() + (random.nextDouble() - 0.3D) * 4.0D,
                    this.fantail.getZ() + (random.nextDouble() - 0.5D) * 10.0D);
            this.fantail.getNavigation().moveTo(target.x, target.y, target.z, 1.0D);
        }
    }
}