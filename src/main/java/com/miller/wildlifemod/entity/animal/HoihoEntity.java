package com.miller.wildlifemod.entity.animal;

import com.miller.wildlifemod.entity.WildlifeAnimationUtils;
import net.minecraft.tags.ItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;


public class HoihoEntity extends Animal {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState waddleAnimationState = new AnimationState();
    public final AnimationState celebrateAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState enterWaterAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState temptedAnimationState = new AnimationState();

    private boolean wasInWater;
    private int enterWaterTicks;
    private int eatTicks;
    private int celebrateTicks;
    private int celebrateCooldown;

    public HoihoEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.14D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new HoihoBreatheGoal());
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new HoihoDiveHuntGoal(this));
        this.goalSelector.addGoal(4, new HoihoReturnToNestGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.1D, this::isFood, false));
        this.goalSelector.addGoal(6, new RandomSwimmingGoal(this, 1.2D, 6));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D) {
            @Override
            public boolean canUse() {
                return !HoihoEntity.this.isInWater() && super.canUse();
            }
        });
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public void playEatAnimation() {
        this.eatTicks = 15;
    }

    private boolean isBeingTempted() {
        List<Player> nearby = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0D));
        for (Player player : nearby) {
            if (this.isFood(player.getMainHandItem()) || this.isFood(player.getOffhandItem())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        boolean inWater = this.isInWater();

        // Enter-water trigger: fires briefly on the water-entry transition, then hands off to swim
        if (inWater && !this.wasInWater) {
            this.enterWaterTicks = 12;
        }
        this.wasInWater = inWater;

        if (this.enterWaterTicks > 0) {
            this.enterWaterTicks--;
            this.enterWaterAnimationState.startIfStopped(this.tickCount);
        } else {
            this.enterWaterAnimationState.stop();
        }

        if (this.eatTicks > 0) {
            this.eatTicks--;
            this.eatAnimationState.startIfStopped(this.tickCount);
        } else {
            this.eatAnimationState.stop();
        }

        boolean tempted = this.isBeingTempted();
        if (tempted && this.enterWaterTicks <= 0 && this.eatTicks <= 0) {
            this.temptedAnimationState.startIfStopped(this.tickCount);
        } else {
            this.temptedAnimationState.stop();
        }

        // Occasional idle flourish while standing around on land, not otherwise occupied
        if (!inWater && !this.walkAnimation.isMoving() && !tempted && this.eatTicks <= 0) {
            if (this.celebrateTicks > 0) {
                this.celebrateTicks--;
                this.celebrateAnimationState.startIfStopped(this.tickCount);
            } else {
                this.celebrateAnimationState.stop();
                if (this.celebrateCooldown > 0) {
                    this.celebrateCooldown--;
                } else if (this.random.nextInt(400) == 0) {
                    this.celebrateTicks = 30;
                    this.celebrateCooldown = 600 + this.random.nextInt(600);
                }
            }
        } else {
            this.celebrateAnimationState.stop();
        }

        boolean overridden = this.enterWaterTicks > 0 || this.eatTicks > 0 || tempted || this.celebrateTicks > 0;

        if (inWater) {
            if (this.walkAnimation.isMoving() && !overridden) {
                this.swimAnimationState.startIfStopped(this.tickCount);
            } else if (this.enterWaterTicks <= 0) {
                this.swimAnimationState.stop();
            }
            this.idleAnimationState.stop();
            this.waddleAnimationState.stop();
            this.runAnimationState.stop();
        } else {
            this.swimAnimationState.stop();
            if (!overridden) {
                WildlifeAnimationUtils.updateLocomotion(this, this.idleAnimationState, this.waddleAnimationState, this.runAnimationState);
            } else {
                this.idleAnimationState.stop();
                this.waddleAnimationState.stop();
                this.runAnimationState.stop();
            }
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        super.travel(travelVector);
        // Porpoising — small leap while swimming fast near the surface
        if (this.isInWater() && this.getDeltaMovement().horizontalDistanceSqr() > 0.01D
                && this.random.nextInt(30) == 0 && this.getY() < this.level().getSeaLevel() + 0.5D) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.28D, 0.0D));
        }
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return com.miller.wildlifemod.entity.ModEntityTypes.HOIHO.create(level, net.minecraft.world.entity.EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // TODO: ModSounds.HOIHO_CALL.get()
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    class HoihoBreatheGoal extends Goal {
        public HoihoBreatheGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return HoihoEntity.this.isInWater()
                    && HoihoEntity.this.getAirSupply() < HoihoEntity.this.getMaxAirSupply() / 4;
        }

        @Override
        public void tick() {
            HoihoEntity.this.getNavigation().moveTo(
                    HoihoEntity.this.getX(), HoihoEntity.this.getY() + 3.0D, HoihoEntity.this.getZ(), 1.3D);
        }
    }

    static class HoihoDiveHuntGoal extends Goal {
        private final HoihoEntity hoiho;
        private AbstractFish target;
        private int huntTicks;

        public HoihoDiveHuntGoal(HoihoEntity hoiho) {
            this.hoiho = hoiho;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.hoiho.isInWater() || this.hoiho.getAirSupply() < this.hoiho.getMaxAirSupply() / 3) return false;
            List<AbstractFish> nearby = this.hoiho.level().getEntitiesOfClass(
                    AbstractFish.class, this.hoiho.getBoundingBox().inflate(12.0D, 6.0D, 12.0D));
            if (nearby.isEmpty()) return this.hoiho.getRandom().nextInt(400) == 0;
            this.target = nearby.get(this.hoiho.getRandom().nextInt(nearby.size()));
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.huntTicks > 0 && this.hoiho.isInWater();
        }

        @Override
        public void start() {
            this.huntTicks = 100 + this.hoiho.getRandom().nextInt(100);
        }

        @Override
        public void tick() {
            this.huntTicks--;
            if (this.target != null && this.target.isAlive()) {
                this.hoiho.getNavigation().moveTo(this.target, 1.4D);
                if (this.hoiho.distanceToSqr(this.target) < 2.5D) {
                    this.target.discard();
                    this.hoiho.gameEvent(net.minecraft.world.level.gameevent.GameEvent.EAT);
                    this.hoiho.playEatAnimation();
                    this.target = null;
                }
            } else {
                this.hoiho.getNavigation().moveTo(
                        this.hoiho.getX() + (this.hoiho.getRandom().nextDouble() - 0.5D) * 6.0D,
                        this.hoiho.getY() - 2.0D,
                        this.hoiho.getZ() + (this.hoiho.getRandom().nextDouble() - 0.5D) * 6.0D,
                        1.2D);
            }
        }

        @Override
        public void stop() {
            this.target = null;
        }
    }

    static class HoihoReturnToNestGoal extends Goal {
        private final HoihoEntity hoiho;
        private final double speedModifier;
        private BlockPos nestPos;

        public HoihoReturnToNestGoal(HoihoEntity hoiho, double speedModifier) {
            this.hoiho = hoiho;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.hoiho.isInWater() || this.hoiho.getRandom().nextInt(800) != 0) return false;
            return this.findNestSpot();
        }

        @Override
        public boolean canContinueToUse() {
            return this.nestPos != null && this.hoiho.isInWater()
                    && this.hoiho.distanceToSqr(this.nestPos.getX(), this.nestPos.getY(), this.nestPos.getZ()) > 4.0D;
        }

        @Override
        public void start() {
            if (this.nestPos != null) {
                this.hoiho.getNavigation().moveTo(
                        this.nestPos.getX() + 0.5D, this.nestPos.getY() + 1.0D, this.nestPos.getZ() + 0.5D, this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.nestPos = null;
        }

        private boolean findNestSpot() {
            BlockPos origin = this.hoiho.blockPosition();
            for (BlockPos pos : BlockPos.withinManhattan(origin, 16, 5, 16)) {
                BlockState state = this.hoiho.level().getBlockState(pos);
                BlockState above = this.hoiho.level().getBlockState(pos.above());
                if (!state.isAir() && state.getFluidState().isEmpty() && state.isSolid() && above.isAir()) {
                    this.nestPos = pos.immutable();
                    return true;
                }
            }
            return false;
        }
    }
}
