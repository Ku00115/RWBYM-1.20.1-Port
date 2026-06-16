package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.RWBYM;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class RWBYMNetwork {
    private static final String PROTOCOL = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RWBYM.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    public static void register() {
        CHANNEL.messageBuilder(AppearanceSyncPacket.class, packetId++)
                .encoder(AppearanceSyncPacket::encode)
                .decoder(AppearanceSyncPacket::decode)
                .consumerMainThread(AppearanceSyncPacket::handle)
                .add();
    }

    public static void syncAppearance(Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                AppearanceSyncPacket.from(entity));
    }

    public static void syncAppearanceTo(Entity entity, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), AppearanceSyncPacket.from(entity));
    }

    private RWBYMNetwork() {
    }
}
