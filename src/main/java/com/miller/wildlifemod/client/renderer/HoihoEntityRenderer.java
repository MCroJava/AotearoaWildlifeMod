package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.HoihoEntityRenderState;
import com.miller.wildlifemod.client.model.HoihoModel;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.entity.animal.HoihoEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class HoihoEntityRenderer extends MobRenderer<HoihoEntity, HoihoEntityRenderState, HoihoModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "wildlifemod/textures/entity/hoiho_texture.png");

    public HoihoEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new HoihoModel(context.bakeLayer(ModEntityModelLayers.HOIHO)), 0.4f);
    }

    @Override
    public HoihoEntityRenderState createRenderState() {
        return new HoihoEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(HoihoEntityRenderState state) {
        return TEXTURE;
    }

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
