package io.github.blaezdev.rwbym.registry;

import com.mojang.serialization.Codec;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.loot.LeafshieldPlantLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, RWBYM.MOD_ID);

    public static final RegistryObject<Codec<? extends IGlobalLootModifier>> LEAFSHIELD_PLANT_DOUBLE =
            LOOT_MODIFIERS.register("leafshield_plant_double", () -> LeafshieldPlantLootModifier.CODEC);

    private RWBYMLootModifiers() {
    }
}
