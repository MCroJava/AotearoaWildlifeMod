package com.miller.wildlifemod.client.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class HoihoEntityRenderState extends LivingEntityRenderState {
    //creates an animationstate for all the individual animations
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState waddleAnimationState = new AnimationState();
    public final AnimationState celebrateAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState enterWaterAnimationState = new AnimationState();
    public final AnimationState swimAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState temptedAnimationState = new AnimationState();
}
