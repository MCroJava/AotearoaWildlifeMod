package com.miller.wildlifemod.entity;

import com.miller.wildlifemod.entity.animal.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes {

    private ModEntityTypes() {}

    public static final EntityType<KiwiEntity> KIWI = register(
            "kiwi",
            EntityType.Builder.of(KiwiEntity::new, MobCategory.CREATURE)
                    .sized(1F, 1F));

    public static final EntityType<KekenoEntity> KEKENO = register(
            "kekeno",
            EntityType.Builder.of(KekenoEntity::new, MobCategory.CREATURE)
                    .sized(3F, 2.5F));

    public static final EntityType<HoihoEntity> HOIHO = register(
            "hoiho",
            EntityType.Builder.of(HoihoEntity::new, MobCategory.CREATURE)
                    .sized(1F, 1F));

    public static final EntityType<PiwakawakaEntity> PIWAKAWAKA = register(
            "piwakawaka",
            EntityType.Builder.of(PiwakawakaEntity::new, MobCategory.CREATURE)
                    .sized(0.8F, 0.8F));

    public static final EntityType<ToroaEntity> TOROA = register(
            "toroa",
            EntityType.Builder.of(ToroaEntity::new, MobCategory.CREATURE)
                    .sized(1.2F, 1.2F));

    private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(
            String path, EntityType.Builder<T> builder) {
        Identifier id = Identifier.fromNamespaceAndPath("wildlifemod", path);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        EntityType<T> type = builder.build(key);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type);
    }

    public static void register() {

    }
}
