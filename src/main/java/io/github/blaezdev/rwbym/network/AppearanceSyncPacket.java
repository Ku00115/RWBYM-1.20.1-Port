package io.github.blaezdev.rwbym.network;

import io.github.blaezdev.rwbym.client.RWBYMClientPacketHandlers;
import io.github.blaezdev.rwbym.item.RWBYMLimbItem;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public record AppearanceSyncPacket(int entityId, CompoundTag appearance) {
    public static AppearanceSyncPacket from(Entity entity) {
        CompoundTag source = entity.getPersistentData().getCompound(RWBYMLimbItem.DATA_KEY);
        return new AppearanceSyncPacket(entity.getId(), source.copy());
    }

    public static void encode(AppearanceSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeNbt(packet.appearance);
    }

    public static AppearanceSyncPacket decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        CompoundTag appearance = buffer.readNbt();
        return new AppearanceSyncPacket(entityId, appearance == null ? new CompoundTag() : appearance);
    }

    public static void handle(AppearanceSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> RWBYMClientPacketHandlers.handleAppearance(packet)));
        context.get().setPacketHandled(true);
    }
}
