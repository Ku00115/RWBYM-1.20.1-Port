package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.SpawnPlacements;
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
        event.put(RWBYMEntityTypes.BEOWOLF.get(), attributes(30.0D, 0.28D, 5.0D).build());
        event.put(RWBYMEntityTypes.URSA.get(), attributes(45.0D, 0.23D, 7.0D).build());
        event.put(RWBYMEntityTypes.BOARBATUSK.get(), attributes(24.0D, 0.32D, 4.0D).build());
        event.put(RWBYMEntityTypes.CREEP.get(), attributes(22.0D, 0.30D, 4.0D).build());
        event.put(RWBYMEntityTypes.SABYR.get(), attributes(28.0D, 0.34D, 5.0D).build());
        event.put(RWBYMEntityTypes.BERINGLE.get(), attributes(55.0D, 0.22D, 8.0D).build());
        event.put(RWBYMEntityTypes.APATHY.get(), attributes(20.0D, 0.18D, 3.0D).build());
        event.put(RWBYMEntityTypes.DEATHSTALKER.get(), attributes(65.0D, 0.24D, 9.0D).build());
        event.put(RWBYMEntityTypes.LANCER.get(), attributes(18.0D, 0.35D, 4.0D).build());
    }

    private static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder attributes(
            double health, double speed, double damage) {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, health)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
        registerMonsterSpawn(event, RWBYMEntityTypes.BEOWOLF);
        registerMonsterSpawn(event, RWBYMEntityTypes.URSA);
        registerMonsterSpawn(event, RWBYMEntityTypes.BOARBATUSK);
        registerMonsterSpawn(event, RWBYMEntityTypes.CREEP);
        registerMonsterSpawn(event, RWBYMEntityTypes.SABYR);
        registerMonsterSpawn(event, RWBYMEntityTypes.BERINGLE);
        registerMonsterSpawn(event, RWBYMEntityTypes.APATHY);
        registerMonsterSpawn(event, RWBYMEntityTypes.DEATHSTALKER);
        registerMonsterSpawn(event, RWBYMEntityTypes.LANCER);
    }

    private static void registerMonsterSpawn(SpawnPlacementRegisterEvent event,
            RegistryObject<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.monster.Monster>> type) {
        event.register(type.get(), SpawnPlacements.Type.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                SpawnPlacementRegisterEvent.Operation.OR);
    }

    private RWBYMEntityEvents() {
    }
}
