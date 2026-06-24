package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.entity.AtlasKnightEntity;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.entity.BasicRWBYMNpcEntity;
import io.github.blaezdev.rwbym.entity.BlakeSummonEntity;
import io.github.blaezdev.rwbym.entity.RagoraEntity;
import io.github.blaezdev.rwbym.entity.RenSummonEntity;
import io.github.blaezdev.rwbym.entity.RWBYMMerchantEntity;
import io.github.blaezdev.rwbym.entity.RWBYMProjectileEntity;
import io.github.blaezdev.rwbym.entity.SeerEntity;
import io.github.blaezdev.rwbym.entity.WinterArmorgeistEntity;
import io.github.blaezdev.rwbym.entity.WinterSummonEntity;
import io.github.blaezdev.rwbym.entity.ZweiEntity;
import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RWBYM.MOD_ID);

    public static final RegistryObject<EntityType<BasicGrimmEntity>> BEOWOLF = ENTITY_TYPES.register("beowolf", () ->
            grimm("beowolf", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<WinterSummonEntity>> WINTER_BEOWOLF = ENTITY_TYPES.register("winter_beowolf", () ->
            winterSummon("winter_beowolf", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> URSA = ENTITY_TYPES.register("ursa", () ->
            grimm("ursa", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<WinterSummonEntity>> WINTER_URSA = ENTITY_TYPES.register("winter_ursa", () ->
            winterSummon("winter_ursa", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> URSA_MAJOR = ENTITY_TYPES.register("ursamajor", () ->
            grimm("ursamajor", 1.95F, 2.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> BOARBATUSK = ENTITY_TYPES.register("boarbatusk", () ->
            grimm("boarbatusk", 1.5F, 1.5F));

    public static final RegistryObject<EntityType<WinterSummonEntity>> WINTER_BOARBATUSK = ENTITY_TYPES.register("winter_boarbatusk", () ->
            winterSummon("winter_boarbatusk", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> CREEP = ENTITY_TYPES.register("creep", () ->
            grimm("creep", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> SABYR = ENTITY_TYPES.register("sabyr", () ->
            grimm("sabyr", 1.5F, 1.5F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> BERINGLE = ENTITY_TYPES.register("beringle", () ->
            grimm("beringle", 1.95F, 2.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> APATHY = ENTITY_TYPES.register("apathy", () ->
            grimm("apathy", 0.5F, 2.5F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> DEATHSTALKER = ENTITY_TYPES.register("deathstalker", () ->
            grimm("deathstalker", 2.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> MUTANT_DEATHSTALKER = ENTITY_TYPES.register("mutantdeathstalker", () ->
            grimm("mutantdeathstalker", 2.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> TINY_DEATHSTALKER = ENTITY_TYPES.register("tinyeathstalker", () ->
            grimm("tinyeathstalker", 1.0F, 1.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> LANCER = ENTITY_TYPES.register("lancer", () ->
            grimm("lancer", 0.5F, 2.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> QUEEN_LANCER = ENTITY_TYPES.register("queenlancer", () ->
            grimm("queenlancer", 1.5F, 3.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> GOLIATH = ENTITY_TYPES.register("goliath", () ->
            grimm("goliath", 3.95F, 3.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> NEVERMORE = ENTITY_TYPES.register("nevermore", () ->
            grimm("nevermore", 1.0F, 2.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> GIANT_NEVERMORE = ENTITY_TYPES.register("giantnevermore", () ->
            grimm("giantnevermore", 10.0F, 14.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> ARMORGEIST = ENTITY_TYPES.register("armorgeist", () ->
            grimm("armorgeist", 1.3F, 4.5F));

    public static final RegistryObject<EntityType<WinterArmorgeistEntity>> WINTER_ARMORGEIST = ENTITY_TYPES.register("winterarmorgeist", () ->
            winterArmorgeist("winterarmorgeist", 1.0F, 2.5F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> GEIST = ENTITY_TYPES.register("geist", () ->
            grimm("geist", 1.0F, 3.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> NUCKLEEVE = ENTITY_TYPES.register("nuckleeve", () ->
            grimm("nuckleeve", 2.0F, 2.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> WYVERN = ENTITY_TYPES.register("wyvern", () ->
            grimm("wyvern", 10.0F, 14.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> RAVAGER = ENTITY_TYPES.register("ravager", () ->
            grimm("ravager", 1.0F, 2.0F));

    public static final RegistryObject<EntityType<SeerEntity>> SEER = ENTITY_TYPES.register("seer", () ->
            seer("seer", 2.0F, 2.0F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> ARACHNE = ENTITY_TYPES.register("arachne", () ->
            grimm("arachne", 0.8F, 0.8F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> ARACHNE_CLONE = ENTITY_TYPES.register("arachneclone", () ->
            grimm("arachneclone", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<BasicGrimmEntity>> HOLLOW = ENTITY_TYPES.register("hollow", () ->
            grimm("hollow", 0.5F, 2.5F));

    public static final RegistryObject<EntityType<AtlasKnightEntity>> ATLAS_KNIGHT = ENTITY_TYPES.register("atlasknight", () ->
            atlasKnight("atlasknight", 1.95F, 1.95F));

    public static final RegistryObject<EntityType<BlakeSummonEntity>> BLAKE = ENTITY_TYPES.register("blake", () ->
            summon("blake", 1.5F, 1.5F));

    public static final RegistryObject<EntityType<BlakeSummonEntity>> BLAKE_FIRE = ENTITY_TYPES.register("blakefire", () ->
            summon("blakefire", 1.5F, 1.5F));

    public static final RegistryObject<EntityType<BlakeSummonEntity>> BLAKE_ICE = ENTITY_TYPES.register("blakeice", () ->
            summon("blakeice", 1.5F, 1.5F));

    public static final RegistryObject<EntityType<RWBYMMerchantEntity>> STORE = ENTITY_TYPES.register("store", () ->
            merchant("store", 1.0F, 1.5F));

    public static final RegistryObject<EntityType<RWBYMMerchantEntity>> WEAPON_STORE = ENTITY_TYPES.register("weaponstore", () ->
            merchant("weaponstore", 1.0F, 1.5F));

    public static final RegistryObject<EntityType<RWBYMMerchantEntity>> BLACK_STORE = ENTITY_TYPES.register("blackstore", () ->
            merchant("blackstore", 1.0F, 1.5F));

    public static final RegistryObject<EntityType<RWBYMMerchantEntity>> ARMOR_STORE = ENTITY_TYPES.register("armorstore", () ->
            merchant("armorstore", 1.0F, 1.5F));

    public static final RegistryObject<EntityType<RWBYMMerchantEntity>> CROWBAR = ENTITY_TYPES.register("crowbar", () ->
            merchant("crowbar", 1.0F, 1.5F));

    public static final RegistryObject<EntityType<RenSummonEntity>> REN = ENTITY_TYPES.register("ren", () ->
            renSummon("ren", 1.5F, 1.5F));

    public static final RegistryObject<EntityType<RagoraEntity>> RAGORA = ENTITY_TYPES.register("ragora", () ->
            ragora("ragora", 1.0F, 1.0F));

    public static final RegistryObject<EntityType<ZweiEntity>> ZWEI = ENTITY_TYPES.register("zwei", () ->
            zwei("zwei", 0.6F, 0.85F));

    public static final RegistryObject<EntityType<RWBYMProjectileEntity>> PROJECTILES =
            projectile("projectiles", 0.35F, 0.35F);

    public static final RegistryObject<EntityType<RWBYMProjectileEntity>> FIREBALL =
            projectile("fireball", 0.3125F, 0.3125F);

    public static final RegistryObject<EntityType<RWBYMProjectileEntity>> LARGE_FIREBALL =
            projectile("largefireball", 1.0F, 1.0F);

    public static final RegistryObject<EntityType<RWBYMProjectileEntity>> WEAPON_PROJECTILE =
            projectile("weapon_projectile", 0.35F, 0.35F);

    public static final List<RegistryObject<EntityType<BasicGrimmEntity>>> GRIMM_ENTITIES = List.of(
            BEOWOLF, URSA, URSA_MAJOR, BOARBATUSK,
            CREEP, SABYR, BERINGLE, APATHY, DEATHSTALKER, MUTANT_DEATHSTALKER, TINY_DEATHSTALKER,
            LANCER, QUEEN_LANCER, GOLIATH, NEVERMORE, GIANT_NEVERMORE, ARMORGEIST,
            GEIST, NUCKLEEVE, WYVERN, RAVAGER, ARACHNE, ARACHNE_CLONE, HOLLOW);

    public static final List<RegistryObject<? extends EntityType<? extends Mob>>> NPC_ENTITIES = List.of(
            ATLAS_KNIGHT, BLAKE, BLAKE_FIRE, BLAKE_ICE, STORE, WEAPON_STORE, BLACK_STORE,
            ARMOR_STORE, CROWBAR, REN, RAGORA, ZWEI);
    
    public static final List<RegistryObject<? extends EntityType<? extends Mob>>> SUMMON_ENTITIES = List.of(
            WINTER_BEOWOLF, WINTER_BOARBATUSK, WINTER_URSA, WINTER_ARMORGEIST);
    
    public static final List<RegistryObject<? extends EntityType<? extends Mob>>> SPECIAL_GRIMM_ENTITIES = List.of(
            SEER);

    public static final List<RegistryObject<EntityType<RWBYMProjectileEntity>>> PROJECTILE_ENTITIES = List.of(
            PROJECTILES, FIREBALL, LARGE_FIREBALL, WEAPON_PROJECTILE);

    private static EntityType<BasicGrimmEntity> grimm(String name, float width, float height) {
        return EntityType.Builder.of(BasicGrimmEntity::new, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<BasicRWBYMNpcEntity> npc(String name, float width, float height) {
        return EntityType.Builder.of(BasicRWBYMNpcEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<BlakeSummonEntity> summon(String name, float width, float height) {
        return EntityType.Builder.of(BlakeSummonEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<RenSummonEntity> renSummon(String name, float width, float height) {
        return EntityType.Builder.of(RenSummonEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<AtlasKnightEntity> atlasKnight(String name, float width, float height) {
        return EntityType.Builder.of(AtlasKnightEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<WinterArmorgeistEntity> winterArmorgeist(String name, float width, float height) {
        return EntityType.Builder.of(WinterArmorgeistEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<WinterSummonEntity> winterSummon(String name, float width, float height) {
        return EntityType.Builder.of(WinterSummonEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<SeerEntity> seer(String name, float width, float height) {
        return EntityType.Builder.of(SeerEntity::new, MobCategory.MONSTER)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<ZweiEntity> zwei(String name, float width, float height) {
        return EntityType.Builder.of(ZweiEntity::new, MobCategory.CREATURE)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<RagoraEntity> ragora(String name, float width, float height) {
        return EntityType.Builder.of(RagoraEntity::new, MobCategory.CREATURE)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static EntityType<RWBYMMerchantEntity> merchant(String name, float width, float height) {
        return EntityType.Builder.of(RWBYMMerchantEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(8)
                .build(name);
    }

    private static RegistryObject<EntityType<RWBYMProjectileEntity>> projectile(String name, float width, float height) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder
                .<RWBYMProjectileEntity>of(RWBYMProjectileEntity::new, MobCategory.MISC)
                .sized(width, height)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(name));
    }

    private RWBYMEntityTypes() {
    }
}
