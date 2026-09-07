package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.HoihoEntityRenderState;
import com.miller.wildlifemod.client.entity.state.KiwiEntityRenderState;
import com.miller.wildlifemod.client.model.KiwiModel;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.entity.animal.KiwiEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class KiwiEntityRenderer extends MobRenderer<KiwiEntity, KiwiEntityRenderState, KiwiModel> {
    private static final Identifier TEXTURE =
            //Identifies what texture the entity should use
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "textures/entity/kiwi_texture.png");

    //Says how big the entities shadow will be
    public KiwiEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new KiwiModel(context.bakeLayer(ModEntityModelLayers.KIWI)), 0.3f);
    }

    //This is called in the AotearoaWildlifeClient class, and registers the entity renderer.
    @Override
    public KiwiEntityRenderState createRenderState() {
        return new KiwiEntityRenderState();
    }

    //tells the class where to get the texture from.
    @Override
    public Identifier getTextureLocation(KiwiEntityRenderState state) {
        return TEXTURE;
    }

    //This decides how big the model of the entity will be
    @Override
    protected void scale(KiwiEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(1.0F, 1.0F, 1.0F);
    }
    //copys all the entities render states.
    @Override
    public void extractRenderState(KiwiEntity entity, KiwiEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.walkingAnimationState.copyFrom(entity.walkingAnimationState);
        state.runningAnimationState.copyFrom(entity.runningAnimationState);
    }
}
