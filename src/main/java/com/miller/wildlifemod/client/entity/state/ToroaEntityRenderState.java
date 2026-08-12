package com.miller.wildlifemod.client.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;


public class ToroaEntityRenderState extends LivingEntityRenderState {
    //creates an animationstate for all the individual animations
    public final AnimationState lowSweepPhase1AnimationState = new AnimationState();
    public final AnimationState ascendPhase2AnimationState = new AnimationState();
    public final AnimationState apexPhase3AnimationState = new AnimationState();
    public final AnimationState descendPhase4AnimationState = new AnimationState();
    public final AnimationState waterSittingAnimationState = new AnimationState();
    public final AnimationState standingAnimationState = new AnimationState();
    public final AnimationState waddlingAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
}
