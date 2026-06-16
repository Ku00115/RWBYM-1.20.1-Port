package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SemblanceCoinItem extends Item {
    private final String semblance;

    public SemblanceCoinItem(String itemName, Properties properties) {
        super(properties);
        this.semblance = semblanceFor(itemName);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.semblance == null) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide()) {
            player.getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(capability -> {
                if (this.semblance.equals(capability.getName())) {
                    capability.setLevel(capability.getLevel() + 1);
                } else {
                    capability.setName(this.semblance);
                    capability.setLevel(1);
                    capability.setActive(false);
                }
                player.sendSystemMessage(Component.literal("Semblance set to " + capability.getName()
                        + " level " + capability.getLevel()));
            });
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.semblance != null) {
            tooltip.add(Component.literal("Sets or upgrades " + displayName(this.semblance) + " semblance")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    private static String displayName(String name) {
        return name.isBlank() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

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
            case "coinraven" -> "raven";
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
