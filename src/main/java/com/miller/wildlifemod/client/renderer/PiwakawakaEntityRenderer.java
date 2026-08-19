package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.HoihoEntityRenderState;
import com.miller.wildlifemod.client.entity.state.PiwakawakaEntityRenderState;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.client.model.PiwakawakaModel;
import com.miller.wildlifemod.entity.animal.PiwakawakaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class PiwakawakaEntityRenderer extends MobRenderer<PiwakawakaEntity, PiwakawakaEntityRenderState, PiwakawakaModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "textures/entity/piwakawaka_texture.png");

    public PiwakawakaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PiwakawakaModel(context.bakeLayer(ModEntityModelLayers.PIWAKAWAKA)), 0.2f);
    }
    @Override
    protected void scale(PiwakawakaEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
    @Override
    public PiwakawakaEntityRenderState createRenderState() {
        return new PiwakawakaEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(PiwakawakaEntityRenderState state) {
        return TEXTURE;
    }


    @Override
    public void extractRenderState(PiwakawakaEntity entity, PiwakawakaEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.perchedAnimationState.copyFrom(entity.perchedAnimationState);
        state.flyingAnimationState.copyFrom(entity.flyingAnimationState);
    }
}
