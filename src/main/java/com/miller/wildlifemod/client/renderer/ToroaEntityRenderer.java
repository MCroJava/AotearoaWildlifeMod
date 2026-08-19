package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.HoihoEntityRenderState;
import com.miller.wildlifemod.client.entity.state.ToroaEntityRenderState;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.client.model.ToroaModel;
import com.miller.wildlifemod.entity.animal.ToroaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class ToroaEntityRenderer extends MobRenderer<ToroaEntity, ToroaEntityRenderState, ToroaModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "textures/entity/toroa_texture.png");

    public ToroaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new ToroaModel(context.bakeLayer(ModEntityModelLayers.TOROA)), 0.6f);
    }

    @Override
    public ToroaEntityRenderState createRenderState() {
        return new ToroaEntityRenderState();
    }
    @Override
    protected void scale(ToroaEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(1.2F, 1.2F, 1.2F);
    }
    @Override
    public Identifier getTextureLocation(ToroaEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(ToroaEntity entity, ToroaEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.lowSweepPhase1AnimationState.copyFrom(entity.lowSweepPhase1AnimationState);
        state.ascendPhase2AnimationState.copyFrom(entity.ascendPhase2AnimationState);
        state.apexPhase3AnimationState.copyFrom(entity.apexPhase3AnimationState);
        state.descendPhase4AnimationState.copyFrom(entity.descendPhase4AnimationState);
        state.waterSittingAnimationState.copyFrom(entity.waterSittingAnimationState);
        state.standingAnimationState.copyFrom(entity.standingAnimationState);
        state.waddlingAnimationState.copyFrom(entity.waddlingAnimationState);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }
}
