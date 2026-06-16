package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.entity.RWBYMProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RWBYM.MOD_ID);

    public static final RegistryObject<EntityType<BasicGrimmEntity>> BEOWOLF = ENTITY_TYPES.register("beowolf", () ->
            grimm("beowolf", 0.6F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> URSA = ENTITY_TYPES.register("ursa", () ->
            grimm("ursa", 0.9F, 1.6F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> BOARBATUSK = ENTITY_TYPES.register("boarbatusk", () ->
            grimm("boarbatusk", 0.9F, 0.9F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> CREEP = ENTITY_TYPES.register("creep", () ->
            grimm("creep", 0.8F, 1.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> SABYR = ENTITY_TYPES.register("sabyr", () ->
            grimm("sabyr", 0.7F, 1.1F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> BERINGLE = ENTITY_TYPES.register("beringle", () ->
            grimm("beringle", 1.0F, 1.8F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> APATHY = ENTITY_TYPES.register("apathy", () ->
            grimm("apathy", 0.6F, 1.9F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> DEATHSTALKER = ENTITY_TYPES.register("deathstalker", () ->
            grimm("deathstalker", 1.4F, 0.9F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> LANCER = ENTITY_TYPES.register("lancer", () ->
            grimm("lancer", 0.7F, 0.7F));

    public static final RegistryObject<EntityType<RWBYMProjectileEntity>> WEAPON_PROJECTILE =
            ENTITY_TYPES.register("weapon_projectile", () -> EntityType.Builder
                    .<RWBYMProjectileEntity>of(RWBYMProjectileEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("weapon_projectile"));

    private static EntityType<BasicGrimmEntity> grimm(String name, float width, float height) {
        return EntityType.Builder.of(BasicGrimmEntity::new, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private RWBYMEntityTypes() {
    }
}
