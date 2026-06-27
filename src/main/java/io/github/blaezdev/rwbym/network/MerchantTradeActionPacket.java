package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.menu.RWBYMMerchantMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client-to-server packet for RWBYM's legacy merchant book quick-trade and payment-slot cleanup actions.
 *
 * <p>Linked files: {@code RWBYMMerchantScreen.java}, {@code RWBYMMerchantMenu.java},
 * and {@code RWBYMMerchantEntity.java}.</p>
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public record MerchantTradeActionPacket(int offerIndex, boolean tradeAll, boolean takeResult) {
    public MerchantTradeActionPacket(int offerIndex, boolean tradeAll) {
        this(offerIndex, tradeAll, true);
    }

    public static void encode(MerchantTradeActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.offerIndex);
        buffer.writeBoolean(packet.tradeAll);
        buffer.writeBoolean(packet.takeResult);
    }

    public static MerchantTradeActionPacket decode(FriendlyByteBuf buffer) {
        return new MerchantTradeActionPacket(buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(MerchantTradeActionPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !(player.containerMenu instanceof RWBYMMerchantMenu merchantMenu)) {
                return;
            }
            if (packet.offerIndex < 0) {
                // Negative index is the modern stand-in for GuiVillager's clear=true MessageTradingData path.
                merchantMenu.returnPaymentSlotsToInventory();
                return;
            }
            if (packet.takeResult) {
                merchantMenu.performBookTrade(player, packet.offerIndex, packet.tradeAll);
            } else {
                merchantMenu.prepareBookTrade(packet.offerIndex);
            }
        });
        context.get().setPacketHandled(true);
    }
}
