package com.miller.wildlifemod.client.entity.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class PiwakawakaEntityRenderState extends LivingEntityRenderState {
    //creates an animationstate for all the individual animations
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState perchedAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();
}
