package io.github.blaezdev.rwbym.capability.semblance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Common contract for RWBYM player Semblance state.
 *
 * <p>The 1.12.2 mod stored each character Semblance as a separate Forge capability.
 * The 1.20.1 port keeps one player capability and dispatches by Semblance name in
 * {@link Semblance}, while preserving the original activation, selected level, cooldown,
 * Aura drain, and NBT persistence concepts.</p>
 *
 * <p>Linked files: {@code RWBYMCapabilityEvents.java}, {@code SemblanceActionPacket.java},
 * {@code SemblanceSyncPacket.java}, and {@code SemblanceCoinItem.java}.</p>
 */
public interface ISemblance {
    /**
     * Runs server-side Semblance upkeep once per player tick.
     */
    void tick(Player player);

    /**
     * Handles the press edge from the Semblance key and starts the relevant ability.
     */
    boolean activate(Player player);

    /**
     * Handles the release edge from the Semblance key for abilities that stop on release.
     */
    boolean deactivate(Player player);

    /**
     * Cycles the original selected-level mechanic used by multi-level Semblances.
     */
    boolean cycleSelectedLevel(Player player);

    String getName();

    void setName(String name);

    boolean isActive();

    void setActive(boolean active);

    int getLevel();

    void setLevel(int level);

    int getSelectedLevel();

    void setSelectedLevel(int level);

    boolean isMovementBlocked();

    /**
     * Copies persistent state during death respawn or dimension clone transfer.
     */
    void copyFrom(ISemblance other);

    /**
     * Serializes all gameplay state needed by the server capability and client sync packet.
     */
    CompoundTag serialize();

    /**
     * Restores gameplay state from player capability storage or {@code SemblanceSyncPacket}.
     */
    void deserialize(CompoundTag tag);
}
