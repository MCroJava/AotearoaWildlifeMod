package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.HoihoEntityRenderState;
import com.miller.wildlifemod.client.model.HoihoModel;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.entity.animal.HoihoEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class HoihoEntityRenderer extends MobRenderer<HoihoEntity, HoihoEntityRenderState, HoihoModel> {
    private static final Identifier TEXTURE =
            //Identifies what texture the entity should use
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "textures/entity/hoiho_texture.png");
    //Says how big the entities shadow will be
    public HoihoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HoihoModel(context.bakeLayer(ModEntityModelLayers.HOIHO)), 0.4f);
    }
    //This decides how big the model of the entity will be
    @Override
    protected void scale(HoihoEntityRenderState state, PoseStack poseStack) {
        super.scale(state, poseStack);
        poseStack.scale(1.0F, 1.0F, 1.0F);
    }
    //This is called in the AotearoaWildlifeClient class, and registers the entity renderer.
    @Override
    public HoihoEntityRenderState createRenderState() {
        return new HoihoEntityRenderState();
    }

    //tells the class where to get the texture from.
    @Override
    public Identifier getTextureLocation(HoihoEntityRenderState state) {
        return TEXTURE;
    }

    //copys all the entities render states.
    @Override
    public void extractRenderState(HoihoEntity entity, HoihoEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.waddleAnimationState.copyFrom(entity.waddleAnimationState);
        state.celebrateAnimationState.copyFrom(entity.celebrateAnimationState);
        state.runAnimationState.copyFrom(entity.runAnimationState);
        state.enterWaterAnimationState.copyFrom(entity.enterWaterAnimationState);
        state.swimAnimationState.copyFrom(entity.swimAnimationState);
        state.eatAnimationState.copyFrom(entity.eatAnimationState);
        state.temptedAnimationState.copyFrom(entity.temptedAnimationState);
    }
}
