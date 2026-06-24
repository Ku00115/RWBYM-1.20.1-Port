package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.capability.aura.IAura;
import io.github.blaezdev.rwbym.capability.semblance.ISemblance;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client-to-server request from the Scroll GUI to spend XP on Aura max-level upgrades.
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public record ScrollAuraLevelPacket(boolean levelAll) {
    public static void encode(ScrollAuraLevelPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBoolean(packet.levelAll);
    }

    public static ScrollAuraLevelPacket decode(FriendlyByteBuf buffer) {
        return new ScrollAuraLevelPacket(buffer.readBoolean());
    }

    public static void handle(ScrollAuraLevelPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            player.getCapability(RWBYMCapabilities.AURA).ifPresent(aura ->
                    player.getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(semblance ->
                            spendLevels(player, aura, semblance, packet.levelAll)));
            RWBYMNetwork.sendOpenScrollScreen(player);
        });
        context.get().setPacketHandled(true);
    }

    private static void spendLevels(ServerPlayer player, IAura aura, ISemblance semblance, boolean levelAll) {
        int guard = levelAll ? 256 : 1;
        while (guard-- > 0) {
            int cost = aura.getExpToLevel();
            if (cost <= 0 || cost == Integer.MAX_VALUE || player.totalExperience < cost) {
                return;
            }
            player.giveExperiencePoints(-cost);
            aura.addToMax("jaune".equals(semblance.getName()) ? 2.0F : 1.0F);
            if (!levelAll) {
                return;
            }
        }
    }
}
