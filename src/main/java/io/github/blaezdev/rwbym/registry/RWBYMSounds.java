package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RWBYM.MOD_ID);

    public static final RegistryObject<SoundEvent> CRESCENT_ROSE_SHOOT = register("weapon.crescent_rose.shoot");
    public static final RegistryObject<SoundEvent> GAMBOL_SHROUD_SHOOT = register("weapon.gambol_shroud.shoot");
    public static final RegistryObject<SoundEvent> EMBER_CELICA_SHOOT = register("weapon.ember_celica.shoot");
    public static final RegistryObject<SoundEvent> MYRTENASTER_SHOOT = register("weapon.mytrenaster.shoot");
    public static final RegistryObject<SoundEvent> STORMFLOWER_SHOOT = register("weapon.storm_flower.shoot");
    public static final RegistryObject<SoundEvent> MAGNHILD_SHOOT = register("weapon.magnhild.shoot");
    public static final RegistryObject<SoundEvent> PORT_SHOOT = register("weapon.port.shoot");
    public static final RegistryObject<SoundEvent> JUNIOR_SHOOT = register("weapon.junior.shoot");
    public static final RegistryObject<SoundEvent> TORCHWICK_SHOOT = register("weapon.torchwick.shoot");
    public static final RegistryObject<SoundEvent> RIFLE_SHOOT = register("weapon.rifle.shot");
    public static final RegistryObject<SoundEvent> RIFLE_BOLT_UP = register("weapon.rifle.boltup");
    public static final RegistryObject<SoundEvent> RIFLE_BOLT_BACK = register("weapon.rifle.boltback");
    public static final RegistryObject<SoundEvent> RIFLE_BOLT_FORWARD = register("weapon.rifle.boltforward");
    public static final RegistryObject<SoundEvent> RIFLE_BOLT_DOWN = register("weapon.rifle.boltdown");
    public static final RegistryObject<SoundEvent> GLOCK_DRY = register("weapon.glock.dry");
    public static final RegistryObject<SoundEvent> GLOCK_MAG_IN = register("weapon.glock.mag_in");
    public static final RegistryObject<SoundEvent> GLOCK_MAG_OUT = register("weapon.glock.mag_out");
    public static final RegistryObject<SoundEvent> COLT_SLIDE_BACK = register("weapon.colt.slideback");
    public static final RegistryObject<SoundEvent> COLT_SLIDE_FORWARD = register("weapon.colt.slideforward");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(new ResourceLocation(RWBYM.MOD_ID, name)));
    }

    private RWBYMSounds() {
    }
}
