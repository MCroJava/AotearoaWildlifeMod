package com.miller.wildlifemod.entity.animal;

import com.miller.wildlifemod.entity.ModEntityTypes;
import com.miller.wildlifemod.entity.WildlifeAnimationUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.Nullable;

public class KiwiEntity extends Animal {

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState runningAnimationState = new AnimationState();

    public KiwiEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(false);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.15D, stack -> stack.is(ItemTags.FISHES), false));
        this.goalSelector.addGoal(5, new KiwiForageGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D, 0.0001F) {
            @Override
            public boolean canUse() {
                return KiwiEntity.this.level().isDarkOutside() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }



    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        WildlifeAnimationUtils.updateLocomotion(this, this.idleAnimationState, this.walkingAnimationState, this.runningAnimationState);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return ModEntityTypes.KIWI.create(level, EntitySpawnReason.BREEDING);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null; // TODO: ModSounds.KIWI_CALL.get()
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    static class KiwiForageGoal extends Goal {
        private final KiwiEntity kiwi;
        private final double speedModifier;
        private int forageCooldown;
        private int probeTicks;

        public KiwiForageGoal(KiwiEntity kiwi, double speedModifier) {
            this.kiwi = kiwi;
            this.speedModifier = speedModifier;
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.kiwi.level().isDarkOutside()) return false;
            if (this.forageCooldown > 0) {
                this.forageCooldown--;
                return false;
            }
            return this.kiwi.getRandom().nextInt(200) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.probeTicks > 0;
        }

        @Override
        public void start() {
            this.probeTicks = 20 + this.kiwi.getRandom().nextInt(30);
            this.kiwi.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.probeTicks--;
            if (this.probeTicks <= 0) {
                this.forageCooldown = 100 + this.kiwi.getRandom().nextInt(200);
            }
        }
    }
}
