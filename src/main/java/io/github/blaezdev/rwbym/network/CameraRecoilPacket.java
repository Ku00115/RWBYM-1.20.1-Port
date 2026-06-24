package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.client.RWBYMClientPacketHandlers;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record CameraRecoilPacket(float pitch, float yaw) {
    public static void encode(CameraRecoilPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.pitch);
        buffer.writeFloat(packet.yaw);
    }

    public static CameraRecoilPacket decode(FriendlyByteBuf buffer) {
        return new CameraRecoilPacket(buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(CameraRecoilPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> RWBYMClientPacketHandlers.handleCameraRecoil(packet)));
        context.get().setPacketHandled(true);
    }
}
