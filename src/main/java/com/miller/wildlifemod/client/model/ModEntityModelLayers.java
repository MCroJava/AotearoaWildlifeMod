package com.miller.wildlifemod.client.model;

import com.miller.wildlifemod.AotearoaWildlife;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class ModEntityModelLayers {

    private ModEntityModelLayers() {}
//defines the model (e.g. "hoihomodel") to the project identifier (e.g. "HOIHO")
    public static final ModelLayerLocation HOIHO = createMain("hoihomodel");
    public static final ModelLayerLocation KEKENO = createMain("kekenomodel");
    public static final ModelLayerLocation KIWI = createMain("kiwimodel");
    public static final ModelLayerLocation PIWAKAWAKA = createMain("piwakawakamodel");
    public static final ModelLayerLocation TOROA = createMain("toroamodel");

    private static ModelLayerLocation createMain(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, name), "main");
    }

    public static void registerModelLayers() {
        //registers all the models in game.
        ModelLayerRegistry.registerModelLayer(HOIHO, HoihoModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(KEKENO, KekenoModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(KIWI, KiwiModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PIWAKAWAKA, PiwakawakaModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TOROA, ToroaModel::createBodyLayer);
    }
}
