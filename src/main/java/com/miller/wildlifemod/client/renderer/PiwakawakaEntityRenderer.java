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
            //Identifies what texture the entity should use

            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "textures/entity/piwakawaka_texture.png");

    //Says how big the entities shadow will be
    public PiwakawakaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PiwakawakaModel(context.bakeLayer(ModEntityModelLayers.PIWAKAWAKA)), 0.2f);
    }
    //This decides how big the model of the entity will be
    @Override
    protected void scale(PiwakawakaEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }
    //This is called in the AotearoaWildlifeClient class, and registers the entity renderer.
    @Override
    public PiwakawakaEntityRenderState createRenderState() {
        return new PiwakawakaEntityRenderState();
    }

    //tells the class where to get the texture from.
    @Override
    public Identifier getTextureLocation(PiwakawakaEntityRenderState state) {
        return TEXTURE;
    }

    //copys all the entities render states.
    @Override
    public void extractRenderState(PiwakawakaEntity entity, PiwakawakaEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.perchedAnimationState.copyFrom(entity.perchedAnimationState);
        state.flyingAnimationState.copyFrom(entity.flyingAnimationState);
    }
}
