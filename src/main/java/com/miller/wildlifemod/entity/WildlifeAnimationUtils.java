package com.miller.wildlifemod.entity;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;

public final class WildlifeAnimationUtils {

    private WildlifeAnimationUtils() {}

    public static final float DEFAULT_RUN_THRESHOLD = 0.6F;

    public static void updateLocomotion(LivingEntity entity, AnimationState idle,
                                        AnimationState walking, AnimationState running) {
        updateLocomotion(entity, idle, walking, running, DEFAULT_RUN_THRESHOLD);
    }

    public static void updateLocomotion(LivingEntity entity, AnimationState idle,
                                        AnimationState walking, AnimationState running,
                                        float runThreshold) {
        int tick = entity.tickCount;
        if (!entity.walkAnimation.isMoving()) {
            idle.startIfStopped(tick);
            walking.stop();
            running.stop();
        } else if (entity.walkAnimation.speed() > runThreshold) {
            running.startIfStopped(tick);
            idle.stop();
            walking.stop();
        } else {
            walking.startIfStopped(tick);
            idle.stop();
            running.stop();
        }
    }
}
