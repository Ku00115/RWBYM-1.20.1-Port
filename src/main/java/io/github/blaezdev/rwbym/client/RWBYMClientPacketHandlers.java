package io.github.blaezdev.rwbym.client;

import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.client.screen.RWBYMScrollScreen;
import io.github.blaezdev.rwbym.network.AppearanceSyncPacket;
import io.github.blaezdev.rwbym.network.CameraRecoilPacket;
import io.github.blaezdev.rwbym.network.OpenScrollScreenPacket;
import io.github.blaezdev.rwbym.network.SemblanceSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Client-only packet application helpers for RWBYM gameplay state.
 *
 * <p>Network packet records stay in {@code network/}; this class keeps Minecraft-client
 * access behind DistExecutor-safe calls so common packet classes do not directly reference
 * client-only state on a dedicated server.</p>
 *
 * <p>Linked files: {@code AppearanceSyncPacket.java}, {@code CameraRecoilPacket.java},
 * {@code SemblanceSyncPacket.java}, {@code OpenScrollScreenPacket.java}, and
 * {@code RWBYMNetwork.java}.</p>
 */
public final class RWBYMClientPacketHandlers {
    /**
     * Applies cosmetic appearance NBT to the tracked entity on the client.
     */
    public static void handleAppearance(AppearanceSyncPacket packet) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (entity != null) {
            // Appearance is entity persistent data so render layers can read it without another cache.
            entity.getPersistentData().put(RWBYMLimbItem.DATA_KEY, packet.appearance().copy());
        }
    }

    /**
     * Applies server-authoritative camera recoil for visible gun feedback.
     */
    public static void handleCameraRecoil(CameraRecoilPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        float yaw = minecraft.player.getYRot() + packet.yaw();
        // Clamp pitch to vanilla camera bounds so recoil cannot flip the player view.
        float pitch = net.minecraft.util.Mth.clamp(minecraft.player.getXRot() + packet.pitch(), -90.0F, 90.0F);
        minecraft.player.setYRot(yaw);
        minecraft.player.setXRot(pitch);
        minecraft.player.setYHeadRot(yaw);
        minecraft.player.yBodyRot = yaw;
        minecraft.player.yRotO += packet.yaw();
        minecraft.player.yHeadRotO += packet.yaw();
        minecraft.player.yBodyRotO += packet.yaw();
        minecraft.player.xRotO = net.minecraft.util.Mth.clamp(minecraft.player.xRotO + packet.pitch(), -90.0F, 90.0F);
    }

    /**
     * Rehydrates the client-side Semblance capability from server NBT.
     */
    public static void handleSemblance(SemblanceSyncPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.player.getCapability(RWBYMCapabilities.SEMBLANCE)
                .ifPresent(semblance -> semblance.deserialize(packet.data()));
    }

    /**
     * Opens the Scroll GUI from the server-provided player snapshot.
     */
    public static void openScrollScreen(OpenScrollScreenPacket packet) {
        // AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
        // Screen creation stays client-only while the packet class remains safe to register on both sides.
        Minecraft.getInstance().setScreen(new RWBYMScrollScreen(packet));
    }

    private RWBYMClientPacketHandlers() {
    }
}
