package com.miller.wildlifemod.client.renderer;

import com.miller.wildlifemod.AotearoaWildlife;
import com.miller.wildlifemod.client.entity.state.PiwakawakaEntityRenderState;
import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.client.model.PiwakawakaModel;
import com.miller.wildlifemod.entity.animal.PiwakawakaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class PiwakawakaEntityRenderer extends MobRenderer<PiwakawakaEntity, PiwakawakaEntityRenderState, PiwakawakaModel> {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, "wildlifemod:textures/entity/piwakawaka_texture.png");

    public PiwakawakaEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PiwakawakaModel(context.bakeLayer(ModEntityModelLayers.PIWAKAWAKA)), 0.2f);
    }

    @Override
    public PiwakawakaEntityRenderState createRenderState() {
        return new PiwakawakaEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(PiwakawakaEntityRenderState state) {
        return TEXTURE;
    }

    // NOTE: PiwakawakaEntity needs matching AnimationState fields (idle, perched, flying)
    // with these exact names — the entity class as originally written used different names
    // (flapAnimationState, dartAnimationState) for this to compile.
    @Override
    public void extractRenderState(PiwakawakaEntity entity, PiwakawakaEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
        state.perchedAnimationState.copyFrom(entity.perchedAnimationState);
        state.flyingAnimationState.copyFrom(entity.flyingAnimationState);
    }
}
