package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.WeaponModifierEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public final class RWBYMEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, RWBYM.MOD_ID);

    public static final Map<String, RegistryObject<Enchantment>> WEAPON_MODIFIERS = new LinkedHashMap<>();

    static {
        register("double_shot");
        register("knock_shot");
        register("poison_shot");
        register("flare_frost_shot");
        register("aura_siphon");
        register("scavenger");
        register("lucky_hit");
        register("trickster");
        register("attuned_frame");
        register("heavy_weight_frame");
        register("light_weight_frame");
        register("precision_frame");
        register("rapid_fire_frame");
        register("arrow_break_barrel");
        register("chambered_compensator_barrel");
        register("cork_screw_rifling");
        register("extended_barrel");
        register("fluted_barrel");
        register("full_bore_barrel");
        register("home_forged_rifling");
        register("polygonal_rifling");
        register("small_bore_barrel");
    }

    private static void register(String name) {
        WEAPON_MODIFIERS.put(name, ENCHANTMENTS.register(name, WeaponModifierEnchantment::new));
    }

    private RWBYMEnchantments() {
    }
}
