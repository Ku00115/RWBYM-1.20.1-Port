package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.capability.aura.IAura;
import io.github.blaezdev.rwbym.capability.semblance.ISemblance;
import io.github.blaezdev.rwbym.team.RWBYMTeamData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Central Forge SimpleChannel registration for RWBYM packets.
 *
 * <p>All gameplay packets are registered here during mod construction from {@code RWBYM.java}.
 * Keeping packet ids in one class prevents mismatched registration order between client and server.</p>
 *
 * <p>Linked files: {@code AppearanceSyncPacket.java}, {@code CameraRecoilPacket.java},
 * {@code SpecialGunActionPacket.java}, {@code SemblanceActionPacket.java}, and
 * {@code SemblanceSyncPacket.java}, {@code OpenScrollScreenPacket.java}, and
 * {@code ScrollAuraLevelPacket.java}, and {@code ScrollTeamActionPacket.java}.</p>
 */
public final class RWBYMNetwork {
    private static final String PROTOCOL = "1";
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RWBYM.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    /**
     * Registers every packet type with stable encode/decode/handle functions.
     */
    public static void register() {
        // Packet ids must increment in exactly the same order on both physical sides.
        CHANNEL.messageBuilder(AppearanceSyncPacket.class, packetId++)
                .encoder(AppearanceSyncPacket::encode)
                .decoder(AppearanceSyncPacket::decode)
                .consumerMainThread(AppearanceSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(CameraRecoilPacket.class, packetId++)
                .encoder(CameraRecoilPacket::encode)
                .decoder(CameraRecoilPacket::decode)
                .consumerMainThread(CameraRecoilPacket::handle)
                .add();
        CHANNEL.messageBuilder(SpecialGunActionPacket.class, packetId++)
                .encoder(SpecialGunActionPacket::encode)
                .decoder(SpecialGunActionPacket::decode)
                .consumerMainThread(SpecialGunActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(SemblanceActionPacket.class, packetId++)
                .encoder(SemblanceActionPacket::encode)
                .decoder(SemblanceActionPacket::decode)
                .consumerMainThread(SemblanceActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(SemblanceSyncPacket.class, packetId++)
                .encoder(SemblanceSyncPacket::encode)
                .decoder(SemblanceSyncPacket::decode)
                .consumerMainThread(SemblanceSyncPacket::handle)
                .add();
        CHANNEL.messageBuilder(MerchantTradeActionPacket.class, packetId++)
                .encoder(MerchantTradeActionPacket::encode)
                .decoder(MerchantTradeActionPacket::decode)
                .consumerMainThread(MerchantTradeActionPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenScrollScreenPacket.class, packetId++)
                .encoder(OpenScrollScreenPacket::encode)
                .decoder(OpenScrollScreenPacket::decode)
                .consumerMainThread(OpenScrollScreenPacket::handle)
                .add();
        CHANNEL.messageBuilder(ScrollAuraLevelPacket.class, packetId++)
                .encoder(ScrollAuraLevelPacket::encode)
                .decoder(ScrollAuraLevelPacket::decode)
                .consumerMainThread(ScrollAuraLevelPacket::handle)
                .add();
        CHANNEL.messageBuilder(ScrollTeamActionPacket.class, packetId++)
                .encoder(ScrollTeamActionPacket::encode)
                .decoder(ScrollTeamActionPacket::decode)
                .consumerMainThread(ScrollTeamActionPacket::handle)
                .add();
    }

    /**
     * Sends cosmetic appearance data to the owning client and tracking clients.
     */
    public static void syncAppearance(Entity entity) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity),
                AppearanceSyncPacket.from(entity));
    }

    /**
     * Sends cosmetic appearance data to one tracking player.
     */
    public static void syncAppearanceTo(Entity entity, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), AppearanceSyncPacket.from(entity));
    }

    /**
     * Sends gun recoil feedback to the firing player.
     */
    public static void sendCameraRecoil(ServerPlayer player, float pitch, float yaw) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new CameraRecoilPacket(pitch, yaw));
    }

    /**
     * Sends the server Semblance capability state to the owning client.
     */
    public static void syncSemblance(ServerPlayer player) {
        player.getCapability(RWBYMCapabilities.SEMBLANCE)
                .ifPresent(semblance -> CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new SemblanceSyncPacket(semblance.serialize())));
    }

    /**
     * Sends the authoritative player state needed by the client-only Scroll screen.
     */
    public static void sendOpenScrollScreen(ServerPlayer player) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // The old GuiHandler read local client capabilities; the modern screen receives a server snapshot.
        float auraAmount = player.getCapability(RWBYMCapabilities.AURA).map(IAura::getAmount).orElse(0.0F);
        float auraMax = player.getCapability(RWBYMCapabilities.AURA).map(IAura::getMaxAura).orElse(0.0F);
        int auraExpCost = player.getCapability(RWBYMCapabilities.AURA).map(IAura::getExpToLevel)
                .orElse(Integer.MAX_VALUE);
        String semblanceName = player.getCapability(RWBYMCapabilities.SEMBLANCE).map(ISemblance::getName)
                .orElse("none");
        int semblanceLevel = player.getCapability(RWBYMCapabilities.SEMBLANCE).map(ISemblance::getLevel).orElse(0);
        int selectedLevel = player.getCapability(RWBYMCapabilities.SEMBLANCE).map(ISemblance::getSelectedLevel).orElse(0);
        RWBYMTeamData.Snapshot team = RWBYMTeamData.get(player).snapshot(player);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new OpenScrollScreenPacket(auraAmount, auraMax, auraExpCost, semblanceName, semblanceLevel,
                        selectedLevel, team.teamMembers(), team.receivedRequests(), team.sentRequests()));
    }

    private RWBYMNetwork() {
    }
}
