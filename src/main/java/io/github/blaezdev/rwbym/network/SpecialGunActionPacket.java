package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public record SpecialGunActionPacket(Action action, InteractionHand hand, boolean down) {
    public enum Action {
        SHOOT,
        ADS,
        CYCLE_ACTION,
        REMOVE_ROUND,
        HAMMER,
        MAG_RELEASE,
        INSERT_MAG,
        FIRE_SELECT
    }

    public static void encode(SpecialGunActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
        buffer.writeEnum(packet.hand);
        buffer.writeBoolean(packet.down);
    }

    public static SpecialGunActionPacket decode(FriendlyByteBuf buffer) {
        return new SpecialGunActionPacket(buffer.readEnum(Action.class), buffer.readEnum(InteractionHand.class),
                buffer.readBoolean());
    }

    public static void handle(SpecialGunActionPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getItemInHand(packet.hand);
            if (stack.getItem() instanceof RWBYMWeaponItem weapon) {
                weapon.handleSpecialGunAction(stack, player, packet.hand, packet.action, packet.down);
            }
        });
        context.get().setPacketHandled(true);
    }
}
