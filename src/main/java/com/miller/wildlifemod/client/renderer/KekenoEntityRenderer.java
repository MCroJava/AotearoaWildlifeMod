package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.HoihoEntityRenderState;
import com.miller.wildlifemod.client.entity.state.KekenoEntityRenderState;
import com.miller.wildlifemod.client.model.KekenoModel;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.entity.animal.KekenoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class KekenoEntityRenderer extends MobRenderer<KekenoEntity, KekenoEntityRenderState, KekenoModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "textures/entity/kekeno_texture.png");

    public KekenoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new KekenoModel(context.bakeLayer(ModEntityModelLayers.KEKENO)), 0.5f);
    }
    @Override
    protected void scale(KekenoEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(2.5F, 2.5F, 3F);
    }

    @Override
    public KekenoEntityRenderState createRenderState() {
        return new KekenoEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(KekenoEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public void extractRenderState(KekenoEntity entity, KekenoEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkingAnimationState.copyFrom(entity.walkingAnimationState);
        state.runningAnimationState.copyFrom(entity.runningAnimationState);
        state.swimmingAnimationState.copyFrom(entity.swimmingAnimationState);
    }
}