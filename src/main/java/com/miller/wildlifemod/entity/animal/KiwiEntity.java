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

    // These three variables track which animation should currently be playing.
    // They don't draw anything themselves — think of them like light switches.
    // The renderer (a separate class) checks these switches every frame and decides
    // which animation clip to actually show on screen.
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState runningAnimationState = new AnimationState();

    public KiwiEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);

        // Kiwi are flightless land birds, so we don't want them wading into water.
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.getNavigation().setCanFloat(true); // Still lets them float if they DO end up in water, so they don't drown.
    }

    /**
     * Attributes are the base stats every living thing needs in minecraft, like max health and
     * speed.
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.STEP_HEIGHT, 1.0D); // How tall a block the Kiwi can step up onto without jumping.
    }

    /**
     * This controls HOW the Kiwi finds its way around the world.
     * We're using a "ground" navigator since Kiwi walk, they don't fly or swim.
     */
    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(false); // Kiwi can't open doors — no hands!
        navigation.setCanFloat(false);
        return navigation;
    }

    /**
     * This is the Kiwi's to-do list, from most important (0) to least important.
     * Every tick (20 times a second), the game checks this list top to bottom and
     * runs the first goal that says it's allowed to run. Think of it like a priority
     * queue for behavior — "am I in danger? No? Am I hungry? No? Then just wander."
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this)); // Keeps it from sinking if it ends up in water.
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D)); // Runs away in a panic when hurt.
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D)); // Lets two Kiwi make a baby Kiwi.
        // TemptGoal makes the Kiwi follow a player holding food (here, anything tagged as a fish).
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.15D, stack -> stack.is(ItemTags.FISHES), false));
        this.goalSelector.addGoal(5, new KiwiForageGoal(this, 1.0D)); // Our own custom goal, defined below.
        // Wander around randomly, but ONLY at night — Kiwi are nocturnal in real life.
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D, 0.0001F) {
            @Override
            public boolean canUse() {
                return KiwiEntity.this.level().isDarkOutside() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F)); // Looks at nearby players.
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this)); // Occasionally looks around for no reason, just for realism.
    }

    /**
     * Minecraft calls this to check "is this item something this animal wants to eat?"
     */
    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    /**
     * aiStep() runs every single tick (20 times per second) while the entity exists.
     * It's a good spot for anything that needs to be constantly checked or updated —
     * here, we use it to keep the animation switches in sync with how the Kiwi is moving.
     */
    @Override
    public void aiStep() {
        super.aiStep(); // Always let the parent class do its normal stuff first.
        WildlifeAnimationUtils.updateLocomotion(this, this.idleAnimationState, this.walkingAnimationState, this.runningAnimationState);
    }

    /**
     * Called automatically when two Kiwi successfully breed — this is what actually
     * creates the baby.
     */
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob mate) {
        return ModEntityTypes.KIWI.create(level, EntitySpawnReason.BREEDING);
    }

    // The next three methods tell Minecraft which sounds to play and when.
    // Returning null just means "no sound for this yet" — you can swap these
    // out for real sound events once you've registered some.

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

    /**
     * A "Goal" is a self-contained chunk of AI behavior. Making our own custom Goal
     * class (instead of just using a built-in one) lets us give the Kiwi behavior
     * that doesn't already exist in vanilla Minecraft — here, stopping to peck at
     * the ground at night like a real kiwi foraging for food.
     */
    static class KiwiForageGoal extends Goal {
        private final KiwiEntity kiwi;
        private final double speedModifier;
        private int forageCooldown; // Counts down so the Kiwi doesn't forage constantly back-to-back.
        private int probeTicks;     // Counts down while the Kiwi is actively "pecking."

        public KiwiForageGoal(KiwiEntity kiwi, double speedModifier) {
            this.kiwi = kiwi;
            this.speedModifier = speedModifier;
            // Flags tell the game which "categories" of behavior this goal controls,
            // so two goals that both want to control movement don't fight each other.
            this.setFlags(java.util.EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        // canUse() answers: "Is now a good time to START this goal?"
        @Override
        public boolean canUse() {
            if (!this.kiwi.level().isDarkOutside()) return false; // Only forage at night.
            if (this.forageCooldown > 0) {
                this.forageCooldown--;
                return false; // Still cooling down from the last time it foraged.
            }
            // A small random chance each tick, so Kiwi don't all forage on a perfect schedule.
            return this.kiwi.getRandom().nextInt(200) == 0;
        }

        // canContinueToUse() answers: "Should this goal KEEP running right now?"
        @Override
        public boolean canContinueToUse() {
            return this.probeTicks > 0;
        }

        // start() runs once, the moment the goal is chosen to begin.
        @Override
        public void start() {
            this.probeTicks = 20 + this.kiwi.getRandom().nextInt(30); // Forage for roughly 1-2.5 seconds.
            this.kiwi.getNavigation().stop(); // Stand still while pecking.
        }

        // tick() runs every game tick WHILE the goal is active.
        @Override
        public void tick() {
            this.probeTicks--;
            if (this.probeTicks <= 0) {
                this.forageCooldown = 100 + this.kiwi.getRandom().nextInt(200); // Wait a while before foraging again.
            }
        }
    }
}