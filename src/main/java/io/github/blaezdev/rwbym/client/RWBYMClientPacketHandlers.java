package io.github.blaezdev.rwbym.client;

import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import io.github.blaezdev.rwbym.network.AppearanceSyncPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public final class RWBYMClientPacketHandlers {
    public static void handleAppearance(AppearanceSyncPacket packet) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (entity != null) {
            entity.getPersistentData().put(RWBYMLimbItem.DATA_KEY, packet.appearance().copy());
        }
    }

    private RWBYMClientPacketHandlers() {
    }
}
