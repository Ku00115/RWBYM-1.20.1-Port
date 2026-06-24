package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client-to-server packet carrying Semblance key intent.
 *
 * <p>The server owns all gameplay effects, Aura costs, cooldowns, and summons. This packet
 * only reports that the client pressed, released, or cycled the Semblance key.</p>
 *
 * <p>Linked files: {@code RWBYMSemblanceKeyHandler.java}, {@code Semblance.java},
 * and {@code RWBYMNetwork.java}.</p>
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public record SemblanceActionPacket(Action action) {
    /**
     * Server-side action requested by the client key handler.
     */
    public enum Action {
        ACTIVATE,
        DEACTIVATE,
        CYCLE_LEVEL
    }

    public static void encode(SemblanceActionPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.action);
    }

    /**
     * Decodes the enum value sent by the client key handler.
     */
    public static SemblanceActionPacket decode(FriendlyByteBuf buffer) {
        return new SemblanceActionPacket(buffer.readEnum(Action.class));
    }

    /**
     * Executes Semblance input on the logical server and syncs changed state back to the player.
     */
    public static void handle(SemblanceActionPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            player.getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(semblance -> {
                // Dispatch to the capability instead of trusting the client with gameplay decisions.
                boolean changed = switch (packet.action) {
                    case ACTIVATE -> semblance.activate(player);
                    case DEACTIVATE -> semblance.deactivate(player);
                    case CYCLE_LEVEL -> semblance.cycleSelectedLevel(player);
                };
                if (changed) {
                    // Immediate sync lets client HUD/key state see selected level and active changes.
                    RWBYMNetwork.syncSemblance(player);
                }
            });
        });
        context.get().setPacketHandled(true);
    }
}
