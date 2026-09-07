package com.miller.wildlifemod.client;

import com.miller.wildlifemod.client.model.ModEntityModelLayers;
import com.miller.wildlifemod.client.renderer.HoihoEntityRenderer;
import com.miller.wildlifemod.client.renderer.KekenoEntityRenderer;
import com.miller.wildlifemod.client.renderer.KiwiEntityRenderer;
import com.miller.wildlifemod.client.renderer.PiwakawakaEntityRenderer;
import com.miller.wildlifemod.client.renderer.ToroaEntityRenderer;
import com.miller.wildlifemod.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class AotearoaWildlifeClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        //Registers the modellayers and the renderentity, so the entitys show up in game.
        ModEntityModelLayers.registerModelLayers();

        EntityRendererRegistry.register(ModEntityTypes.HOIHO, HoihoEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.KEKENO, KekenoEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.KIWI, KiwiEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.PIWAKAWAKA, PiwakawakaEntityRenderer::new);
        EntityRendererRegistry.register(ModEntityTypes.TOROA, ToroaEntityRenderer::new);
    }
}
