package com.miller.wildlifemod;

import com.miller.wildlifemod.entity.ModEntityTypes;
import com.miller.wildlifemod.entity.animal.HoihoEntity;
import com.miller.wildlifemod.entity.animal.KekenoEntity;
import com.miller.wildlifemod.entity.animal.KiwiEntity;
import com.miller.wildlifemod.entity.animal.PiwakawakaEntity;
import com.miller.wildlifemod.entity.animal.ToroaEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AotearoaWildlife implements ModInitializer {
	public static final String MOD_ID = "wildlifemod";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEntityTypes.register();

		FabricDefaultAttributeRegistry.register(ModEntityTypes.KIWI, KiwiEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntityTypes.KEKENO, KekenoEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntityTypes.HOIHO, HoihoEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntityTypes.PIWAKAWAKA, PiwakawakaEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntityTypes.TOROA, ToroaEntity.createAttributes());
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
