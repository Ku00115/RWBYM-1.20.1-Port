package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.entity.SeerEntity;
import io.github.blaezdev.rwbym.entity.WinterArmorgeistEntity;
import io.github.blaezdev.rwbym.entity.WinterSummonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class RWBYMEntityEvents {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        RWBYMEntityTypes.GRIMM_ENTITIES.forEach(type -> event.put(type.get(), grimmAttributes(type.get()).build()));
        RWBYMEntityTypes.NPC_ENTITIES.forEach(type -> event.put(type.get(), npcAttributes(type.get()).build()));
        event.put(RWBYMEntityTypes.SEER.get(), SeerEntity.createAttributes().build());
        event.put(RWBYMEntityTypes.WINTER_BEOWOLF.get(), winterSummonAttributes("winter_beowolf").build());
        event.put(RWBYMEntityTypes.WINTER_BOARBATUSK.get(), winterSummonAttributes("winter_boarbatusk").build());
        event.put(RWBYMEntityTypes.WINTER_URSA.get(), winterSummonAttributes("winter_ursa").build());
        event.put(RWBYMEntityTypes.WINTER_ARMORGEIST.get(), WinterArmorgeistEntity.createAttributes().build());
    }

    private static AttributeSupplier.Builder grimmAttributes(EntityType<?> type) {
        return switch (EntityType.getKey(type).getPath()) {
            case "beowolf", "winter_beowolf" -> attributes(40.0D, 0.35D, 6.0D);
            case "ursa", "winter_ursa" -> attributes(60.0D, 0.35D, 7.0D);
            case "ursamajor" -> attributes(180.0D, 0.35D, 9.0D);
            case "boarbatusk", "winter_boarbatusk" -> attributes(30.0D, 0.35D, 5.0D, 5.0D, 0.0D);
            case "creep" -> attributes(20.0D, 0.35D, 5.0D);
            case "sabyr" -> attributes(60.0D, 0.35D, 5.0D, 3.0D, 0.0D);
            case "beringle" -> attributes(120.0D, 0.35D, 8.0D);
            case "apathy" -> attributes(30.0D, 0.25D, 4.0D);
            case "deathstalker" -> attributes(300.0D, 0.35D, 8.0D, 5.0D, 1.0D);
            case "mutantdeathstalker" -> attributes(300.0D, 0.35D, 10.0D, 0.0D, 1.0D);
            case "tinyeathstalker" -> attributes(20.0D, 0.35D, 6.0D, 5.0D, 0.0D);
            case "lancer" -> attributes(10.0D, 0.35D, 6.0D, 5.0D, 0.0D);
            case "queenlancer" -> attributes(400.0D, 0.35D, 10.0D, 5.0D, 0.0D);
            case "goliath" -> attributes(1500.0D, 0.19D, 10.0D, 5.0D, 1.0D);
            case "nevermore" -> attributes(20.0D, 0.35D, 4.0D);
            case "giantnevermore" -> attributes(1000.0D, 0.35D, 14.0D, 15.0D, 0.0D);
            case "armorgeist" -> attributes(400.0D, 0.35D, 10.0D, 20.0D, 1.0D);
            case "geist" -> attributes(14.0D, 0.35D, 6.0D);
            case "nuckleeve" -> attributes(350.0D, 0.4D, 8.0D, 0.0D, 1.0D);
            case "wyvern" -> attributes(1000.0D, 0.30D, 12.0D, 20.0D, 0.0D);
            case "ravager" -> attributes(40.0D, 0.38D, 5.0D);
            // Original EntityArachne left its 5-damage override commented out, so it kept EntityMob's 2 damage.
            case "arachne" -> attributes(20.0D, 0.35D, 2.0D, 3.0D, 0.0D);
            case "arachneclone" -> attributes(1.0D, 0.35D, 5.0D);
            case "hollow" -> attributes(120.0D, 0.4D, 5.0D);
            default -> attributes(40.0D, 0.35D, 5.0D);
        };
    }

    private static AttributeSupplier.Builder npcAttributes(EntityType<? extends Mob> type) {
        String kind = EntityType.getKey(type).getPath();
        boolean isMerchant = switch (kind) {
            case "store", "weaponstore", "blackstore", "armorstore", "crowbar" -> true;
            default -> false;
        };
        double health = switch (kind) {
            case "atlasknight" -> 100.0D;
            case "blake", "blakefire", "blakeice", "ren" -> 4.0D;
            case "ragora" -> 10000.0D;
            case "zwei" -> 10.0D;
            default -> 24.0D;
        };
        if (isMerchant) {
            health = 30.0D;
        }
        double speed = switch (kind) {
            case "atlasknight" -> 0.35D;
            case "blake", "blakefire", "blakeice", "ren" -> 0.35D;
            case "ragora" -> 0.35D;
            case "zwei" -> 0.30D;
            default -> 0.30D;
        };
        if (isMerchant) {
            // Original RWBYM shops are stationary vendor NPCs; keep that baseline separate from combat NPCs.
            speed = 0.0D;
        }
        double damage = switch (kind) {
            case "atlasknight" -> 9.0D;
            case "blake", "blakefire", "blakeice", "ren" -> 7.0D;
            case "ragora" -> 5.0D;
            case "zwei" -> 2.0D;
            default -> 3.0D;
        };
        double armor = switch (kind) {
            case "atlasknight" -> 20.0D;
            default -> 0.0D;
        };
        double followRange = switch (kind) {
            case "atlasknight", "blake", "blakefire", "blakeice", "ren" -> 12.0D;
            case "ragora" -> 50.0D;
            default -> 24.0D;
        };
        if (isMerchant) {
            followRange = 60.0D;
        }
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.FOLLOW_RANGE, followRange);
    }

    private static AttributeSupplier.Builder attributes(
            double health, double speed, double damage) {
        return attributes(health, speed, damage, 0.0D, 0.0D);
    }

    private static AttributeSupplier.Builder attributes(
            double health, double speed, double damage, double armor, double knockbackResistance) {
        // Original RWBYConfig.attributes.aggrorange defaults to 50 for Grimm follow range.
        return attributes(health, speed, damage, armor, knockbackResistance, 50.0D);
    }

    private static AttributeSupplier.Builder attributes(
            double health, double speed, double damage, double armor, double knockbackResistance, double followRange) {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.KNOCKBACK_RESISTANCE, knockbackResistance)
                .add(Attributes.FOLLOW_RANGE, followRange);
    }

    private static AttributeSupplier.Builder winterSummonAttributes(String kind) {
        double health = switch (kind) {
            case "winter_boarbatusk" -> 30.0D;
            case "winter_ursa" -> 60.0D;
            default -> 40.0D;
        };
        double armor = "winter_boarbatusk".equals(kind) ? 5.0D : 0.0D;
        return AbstractGolem.createMobAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, armor)
                .add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        RWBYMEntityTypes.GRIMM_ENTITIES.forEach(type -> registerMonsterSpawn(event, type));
        registerMonsterSpawn(event, RWBYMEntityTypes.SEER);
    }

    private static void registerMonsterSpawn(SpawnPlacementRegisterEvent event,
            RegistryObject<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.monster.Monster>> type) {
        String kind = EntityType.getKey(type.get()).getPath();
        if ("wyvern".equals(kind) || "queenlancer".equals(kind)) {
            event.register(type.get(), SpawnPlacements.Type.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, RWBYMEntityEvents::checkSkyborneBossSpawn,
                    SpawnPlacementRegisterEvent.Operation.OR);
            return;
        }
        event.register(type.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    private static boolean checkSkyborneBossSpawn(EntityType<? extends Monster> type, ServerLevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original Wyvern/QueenLancer only natural-spawn under open sky at night unless legacy lancer nests are enabled.
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random)
                && level.getDifficulty() != Difficulty.PEACEFUL
                && !level.getLevel().isDay()
                && level.canSeeSky(pos.above(2));
    }

    private RWBYMEntityEvents() {
    }
}
