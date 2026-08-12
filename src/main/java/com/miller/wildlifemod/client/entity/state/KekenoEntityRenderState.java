package com.miller.wildlifemod.client.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class KekenoEntityRenderState extends LivingEntityRenderState {
    //creates an animationstate for all the individual animations
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState runningAnimationState = new AnimationState();
    public final AnimationState swimmingAnimationState = new AnimationState();
}
