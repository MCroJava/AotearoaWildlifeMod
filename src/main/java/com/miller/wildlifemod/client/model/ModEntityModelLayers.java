package com.miller.wildlifemod.client.model;

import com.miller.wildlifemod.AotearoaWildlife;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class ModEntityModelLayers {

    private ModEntityModelLayers() {}

    public static final ModelLayerLocation HOIHO = createMain("hoiho");
    public static final ModelLayerLocation KEKENO = createMain("kekeno");
    public static final ModelLayerLocation KIWI = createMain("kiwi");
    public static final ModelLayerLocation PIWAKAWAKA = createMain("piwakawaka");
    public static final ModelLayerLocation TOROA = createMain("toroa");

    private static ModelLayerLocation createMain(String name) {
        return new ModelLayerLocation(Identifier.fromNamespaceAndPath(AotearoaWildlife.MOD_ID, name), "main");
    }

    public static void registerModelLayers() {
        ModelLayerRegistry.registerModelLayer(HOIHO, HoihoModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(KEKENO, KekenoModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(KIWI, KiwiModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(PIWAKAWAKA, PiwakawakaModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(TOROA, ToroaModel::createBodyLayer);
    }
}
