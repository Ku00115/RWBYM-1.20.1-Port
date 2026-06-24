package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.client.RWBYMClientPacketHandlers;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server-to-client packet for Semblance capability NBT.
 *
 * <p>This packet mirrors the server capability to the owning client so client-only code
 * can read selected level and active state without making gameplay decisions locally.</p>
 *
 * <p>Linked files: {@code RWBYMNetwork.java}, {@code RWBYMClientPacketHandlers.java},
 * and {@code Semblance.java}.</p>
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public record SemblanceSyncPacket(CompoundTag data) {
    /**
     * Writes the serialized capability NBT to the network buffer.
     */
    public static void encode(SemblanceSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeNbt(packet.data);
    }

    /**
     * Reads capability NBT and guards against null buffers from malformed packets.
     */
    public static SemblanceSyncPacket decode(FriendlyByteBuf buffer) {
        CompoundTag data = buffer.readNbt();
        // Empty fallback keeps the client handler safe even if a packet arrives without a tag.
        return new SemblanceSyncPacket(data == null ? new CompoundTag() : data);
    }

    /**
     * Applies the packet only on the physical client through the central client handler.
     */
    public static void handle(SemblanceSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> RWBYMClientPacketHandlers.handleSemblance(packet)));
        context.get().setPacketHandled(true);
    }
}
