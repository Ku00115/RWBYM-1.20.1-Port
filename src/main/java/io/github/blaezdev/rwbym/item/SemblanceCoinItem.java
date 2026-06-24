package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Item that assigns or upgrades the player's Semblance capability.
 *
 * <p>The original mod used coin items to set the active character Semblance. This 1.20.1
 * item writes into the unified {@code ISemblance} capability and syncs the result to the
 * owning client through {@code RWBYMNetwork.syncSemblance}.</p>
 *
 * <p>Linked files: {@code Semblance.java}, {@code RWBYMItems.java}, and
 * {@code SemblanceSyncPacket.java}.</p>
 */
public class SemblanceCoinItem extends Item {
    private final String semblance;

    public SemblanceCoinItem(String itemName, Properties properties) {
        super(properties);
        this.semblance = semblanceFor(itemName);
    }

    /**
     * Assigns the matching Semblance or increases its level when the same coin is reused.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.semblance == null) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide()) {
            player.getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(capability -> {
                if (this.semblance.equals(capability.getName())) {
                    // Reusing a matching coin mirrors the original level-up behavior.
                    capability.setLevel(capability.getLevel() + 1);
                } else {
                    // Changing Semblance clears active state so old movement/summon state cannot leak.
                    capability.setName(this.semblance);
                    capability.setLevel(1);
                    capability.setActive(false);
                }
                player.sendSystemMessage(Component.literal("Semblance set to " + capability.getName()
                        + " level " + capability.getLevel()));
                if (player instanceof ServerPlayer serverPlayer) {
                    // Coins are server-side items; sync immediately so client HUD/key logic sees the change.
                    RWBYMNetwork.syncSemblance(serverPlayer);
                }
            });
            if (!player.getAbilities().instabuild) {
                // Consume only on the server to avoid client-side stack desync.
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Adds a short Semblance explanation to item tooltips.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.semblance != null) {
            tooltip.add(Component.literal("           "));
            tooltip.add(Component.literal("Semblance Info:"));
            addLegacySemblanceInfo(tooltip, this.semblance);
            tooltip.add(Component.literal("           "));
        }
    }

    private static String displayName(String name) {
        return name.isBlank() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static void addLegacySemblanceInfo(List<Component> tooltip, String semblance) {
        switch (semblance) {
            case "ruby" -> {
                addBlue(tooltip, "Lv 1 Allows Faster Movement on Ground");
                addBlue(tooltip, "Lv 2 Allows Flight");
                addBlue(tooltip, "Lv 3 Allows Flight and Deals AOE Damage");
            }
            case "weiss" -> {
                addBlue(tooltip, "Lv 1 Summon Boarbatusk");
                addBlue(tooltip, "Lv 2 Summon Beowolf");
                addBlue(tooltip, "Lv 3 Summon Ursa");
                addBlue(tooltip, "Lv 4 Summon ArmaGigas");
            }
            case "blake" -> {
                addBlue(tooltip, "Lv 1 Summon a Shadow, Can be combined with dust in offhand to apply effects");
                addBlue(tooltip, "Lv 2 Reduce Cost");
                addBlue(tooltip, "Lv 3 Reduce Cost Further");
            }
            case "yang" -> {
                addBlue(tooltip, "Lv 1 Passive Effect Increases Damage based off Missing Aura");
                addBlue(tooltip, "Activating the Semblance Drains Aura and Provides a Stronger Buff");
                addBlue(tooltip, "Lv 2-3 Increases Strength Buff");
            }
            case "ren" -> {
                addBlue(tooltip, "Lv 1 Summons a Distraction and Makes yourself Invisible");
                addBlue(tooltip, "Lv 2 Reduces Cost");
                addBlue(tooltip, "Lv 3 Reduces Cost");
            }
            case "ragora" -> {
                addBlue(tooltip, "Lv 1 Summons a Familiar that Follows");
                addBlue(tooltip, "and Protects you draining aura while active");
                addBlue(tooltip, "Lv 2 Reduce Cost");
                addBlue(tooltip, "Lv 3 Reduce Cost");
            }
            case "jaune" -> addBlue(tooltip, "Lv 1 Doubles Aura Gains per level and Aura Level Cap");
            case "nora" -> {
                addBlue(tooltip, "Lv 1 Speed and Strength Boost when Active(requires offhand lightning dust gem)");
                addBlue(tooltip, "or Being shot by lighting dust will trigger it involuntarily.");
                addBlue(tooltip, "Lv 2 Boosts Improved");
            }
            case "qrow" -> {
                addBlue(tooltip, "Lv 1 Modifier will Halve Damage or Increase it Up to 1.5");
                addBlue(tooltip, "Lv 2 Modifier will Halve Damage or Increase it Up to 1.6");
                addBlue(tooltip, "Lv 3 Modifier will Halve Damage or Increase it Up to 1.7");
            }
            case "lysette" -> {
                addBlue(tooltip, "Lv 1 while holding will act as frost walker and inflict slowness on melee hits while held");
                addBlue(tooltip, "Lv 2 increases size of frostwalker ice and duration of slowness");
            }
            case "clover" -> {
                addBlue(tooltip, "Lv 1 Damage Modifier 1.2");
                addBlue(tooltip, "Lv 2 Damage Modifier 1.3");
                addBlue(tooltip, "Lv 3 Damage Modifier 1.4");
            }
            case "harriet" -> {
                addBlue(tooltip, "Lv 1 Boosts Speed");
                addBlue(tooltip, "Lv 2 Increased Boost to Speed");
                addBlue(tooltip, "Lv 3 Increased Boost to Speed");
            }
            case "pyrrha" -> {
                addBlue(tooltip, "Lv 1 acts as Loyalty for Thrown Weapons");
                addBlue(tooltip, "Lv 2 Reduces Cost and Increases Speed of Return");
                addBlue(tooltip, "Lv 3 Reduces Cost and Increases Speed of Return");
            }
            case "valour" -> {
                addBlue(tooltip, "Lv 1 Allows Short Range Teleportation Directly Infront of Player");
                addBlue(tooltip, "Lv 2 Increases Range");
                addBlue(tooltip, "Lv 3 Increases Range");
            }
            default -> addBlue(tooltip, "Sets or upgrades " + displayName(semblance) + " semblance");
        }
    }

    private static void addBlue(List<Component> tooltip, String text) {
        tooltip.add(Component.literal(text).withStyle(ChatFormatting.BLUE));
    }

    /**
     * Converts legacy coin registry ids to Semblance ids.
     */
    private static String semblanceFor(String itemName) {
        return switch (itemName) {
            case "coinr" -> "ruby";
            case "coinw" -> "weiss";
            case "coinb" -> "blake";
            case "coiny" -> "yang";
            case "coinjaune", "coinjuane" -> "jaune";
            case "coinnora" -> "nora";
            case "coin_ren" -> "ren";
            case "coin_lysette" -> "lysette";
            case "coinqrow" -> "qrow";
            case "coinraven", "coin_raven" -> "raven";
            case "coinfall" -> "fall";
            case "coin_ragora" -> "ragora";
            case "coin_clover" -> "clover";
            case "coin_harriet" -> "harriet";
            case "coin_pyrrha" -> "pyrrha";
            case "coin_valour" -> "valour";
            case "coin_penny" -> "penny";
            default -> null;
        };
    }
}
