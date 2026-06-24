package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.team.RWBYMTeamData;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client-to-server Scroll team action request for sending, accepting, declining, and leaving teams.
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public record ScrollTeamActionPacket(Action action, String targetName) {
    public static void encode(ScrollTeamActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
        buffer.writeUtf(packet.targetName);
    }

    public static ScrollTeamActionPacket decode(FriendlyByteBuf buffer) {
        return new ScrollTeamActionPacket(buffer.readEnum(Action.class), buffer.readUtf());
    }

    public static void handle(ScrollTeamActionPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            RWBYMTeamData teams = RWBYMTeamData.get(player);
            switch (packet.action) {
                case SEND -> teams.sendRequest(player, packet.targetName);
                case ACCEPT -> teams.acceptRequest(player, packet.targetName);
                case DECLINE -> teams.denyIncoming(player, packet.targetName);
                case REMOVE_SENT -> teams.removeSent(player, packet.targetName);
                case LEAVE -> teams.leaveTeam(player);
            }
            RWBYMNetwork.sendOpenScrollScreen(player);
        });
        context.get().setPacketHandled(true);
    }

    public enum Action {
        SEND,
        ACCEPT,
        DECLINE,
        REMOVE_SENT,
        LEAVE
    }
}
