package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.effect.AuraRegenEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RWBYMMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, RWBYM.MOD_ID);

    public static final RegistryObject<MobEffect> AURA_REGEN =
            MOB_EFFECTS.register("aura_regen", AuraRegenEffect::new);

    private RWBYMMobEffects() {
    }
}
