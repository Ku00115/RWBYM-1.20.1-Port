package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.client.RWBYMClientPacketHandlers;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server-to-client snapshot used to open the RWBYM Scroll GUI with authoritative player data.
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
public record OpenScrollScreenPacket(float auraAmount, float auraMax, int auraExpCost, String semblanceName,
                                     int semblanceLevel, int selectedLevel, List<String> teamMembers,
                                     List<String> receivedRequests, List<String> sentRequests) {
    public static void encode(OpenScrollScreenPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.auraAmount);
        buffer.writeFloat(packet.auraMax);
        buffer.writeInt(packet.auraExpCost);
        buffer.writeUtf(packet.semblanceName);
        buffer.writeInt(packet.semblanceLevel);
        buffer.writeInt(packet.selectedLevel);
        writeStringList(buffer, packet.teamMembers);
        writeStringList(buffer, packet.receivedRequests);
        writeStringList(buffer, packet.sentRequests);
    }

    public static OpenScrollScreenPacket decode(FriendlyByteBuf buffer) {
        return new OpenScrollScreenPacket(buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readUtf(),
                buffer.readInt(), buffer.readInt(), readStringList(buffer), readStringList(buffer),
                readStringList(buffer));
    }

    public static void handle(OpenScrollScreenPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> RWBYMClientPacketHandlers.openScrollScreen(packet)));
        context.get().setPacketHandled(true);
    }

    private static void writeStringList(FriendlyByteBuf buffer, List<String> values) {
        buffer.writeVarInt(values.size());
        for (String value : values) {
            buffer.writeUtf(value);
        }
    }

    private static List<String> readStringList(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(buffer.readUtf());
        }
        return values;
    }
}
