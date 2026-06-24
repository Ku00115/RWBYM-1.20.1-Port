package io.github.blaezdev.rwbym.item;

import io.github.blaezdev.rwbym.entity.BasicGrimmEntity;
import io.github.blaezdev.rwbym.registry.RWBYMEntityTypes;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Restores legacy RWBYM right-click entity summon items such as Arma Gigas, Atlas Knight, and Zwei.
 */
public class RWBYMSummonItem extends Item {
    private final String summonName;

    public RWBYMSummonItem(String summonName, Properties properties) {
        super(properties);
        this.summonName = summonName;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            Mob summon = createSummon(level);
            if (summon != null) {
                summon.moveTo(player.blockPosition(), 0.0F, 0.0F);
                level.addFreshEntity(summon);
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.summonName.equals("zwei")) {
            tooltip.add(Component.literal("a Wild Zwei has Appeared use bones to tame him")
                    .withStyle(ChatFormatting.BLUE));
        }
    }

    private Mob createSummon(Level level) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Original RWBYItem spawned these entities directly from item right-click.
        return switch (this.summonName) {
            case "armagigas" -> {
                BasicGrimmEntity armorgeist = RWBYMEntityTypes.ARMORGEIST.get().create(level);
                if (armorgeist != null) {
                    armorgeist.igniteArmorgeist();
                }
                yield armorgeist;
            }
            case "atlasknight" -> RWBYMEntityTypes.ATLAS_KNIGHT.get().create(level);
            case "zwei" -> RWBYMEntityTypes.ZWEI.get().create(level);
            default -> null;
        };
    }
}
