package io.github.blaezdev.rwbym.registry;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.item.RWBYMArmorItem;
import io.github.blaezdev.rwbym.item.RWBYMWeaponModifierHelper;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RWBYMCombatEvents {
    private static final Set<String> PEACH_DROP_WEAPONS = Set.of(
            "lichtroze_closedfire",
            "lichtroze_closedice",
            "lichtroze_closedwind");
    private static final Set<String> REMNANT_DROP_WEAPONS = Set.of(
            "heroshield",
            "leafshield",
            "pickaxeshield",
            "rageshield");

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity source = event.getSource().getEntity();
        if (source instanceof Player attacker && RWBYMArmorItem.hasPerk(attacker, RWBYMArmorItem.KINGSPAWN)) {
            if (hasNearbyGambitSupport(attacker)) {
                event.setAmount(event.getAmount() * 1.5F);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            for (ItemEntity drop : event.getDrops()) {
                if (RWBYMWeaponModifierHelper.supportsDefaultModifiers(drop.getItem())) {
                    drop.setItem(RWBYMWeaponModifierHelper.createGeneratedWeaponStack(
                            drop.getItem(), event.getEntity().level().random));
                }
            }
        }
        if (!(event.getEntity() instanceof BasicGrimmEntity)
                || !(event.getSource().getEntity() instanceof Player player)
                || event.getSource().getDirectEntity() != player) {
            return;
        }
        String weaponName = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).getPath();
        BlockPos pos = event.getEntity().blockPosition();
        if (PEACH_DROP_WEAPONS.contains(weaponName)) {
            event.getDrops().add(new ItemEntity(event.getEntity().level(), pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, new ItemStack(RWBYMItems.SIMPLE_ITEMS.get("peach").get())));
        }
        if (REMNANT_DROP_WEAPONS.contains(weaponName)) {
            event.getDrops().add(new ItemEntity(event.getEntity().level(), pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, new ItemStack(RWBYMItems.SIMPLE_ITEMS.get("remnants").get())));
            event.getDrops().add(new ItemEntity(event.getEntity().level(), pos.getX() + 0.5D, pos.getY() + 0.5D,
                    pos.getZ() + 0.5D, new ItemStack(RWBYMItems.SIMPLE_ITEMS.get("remnants").get())));
        }
    }

    private static boolean hasNearbyGambitSupport(Player attacker) {
        double maxDistance = 10.0D * 10.0D;
        for (Player player : attacker.level().players()) {
            if (player != attacker
                    && attacker.distanceToSqr(player) <= maxDistance
                    && RWBYMArmorItem.hasPerk(player, RWBYMArmorItem.KINGSGAMBIT)) {
                return true;
            }
        }
        return false;
    }

    private RWBYMCombatEvents() {
    }
}
