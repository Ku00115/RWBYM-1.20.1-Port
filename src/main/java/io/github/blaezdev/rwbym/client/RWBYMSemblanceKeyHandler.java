package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.capability.RWBYMCapabilities;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import io.github.blaezdev.rwbym.network.SemblanceActionPacket;
import io.github.blaezdev.rwbym.network.SemblanceActionPacket.Action;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * Client key handler for Semblance activation and selected-level cycling.
 *
 * <p>The original 1.12.2 mod sent separate activate/deactivate and cycle packets from
 * {@code KeyInputHandler}. This 1.20.1 port uses modern {@link KeyMapping} registration
 * and sends {@code SemblanceActionPacket} intents to the server.</p>
 *
 * <p>Linked files: {@code SemblanceActionPacket.java}, {@code RWBYMNetwork.java},
 * {@code Semblance.java}, and {@code RWBYMSpecialGunKeyHandler.java}.</p>
 */
// AI generated port code for 1.20.1 Forge, original logic reference Blaez_Dev source
@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, value = Dist.CLIENT)
public final class RWBYMSemblanceKeyHandler {
    private static final String CATEGORY = "key.categories.rwbym";
    private static final KeyMapping ACTIVATE = key("key.rwbym.activatesemblance", GLFW.GLFW_KEY_G);
    private static final KeyMapping CYCLE_LEVEL = key("key.rwbym.cyclelevel", GLFW.GLFW_KEY_H);

    private static boolean activateDown;

    @Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        /**
         * Registers Semblance key mappings on the mod event bus.
         */
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(ACTIVATE);
            event.register(CYCLE_LEVEL);
        }
    }

    @SubscribeEvent
    /**
     * Polls key state and sends edge-triggered activation packets.
     */
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || minecraft.player == null || minecraft.screen != null
                || holdingSpecialGun()) {
            // Release when GUI/special-gun context takes over so the server does not keep an ability held.
            releaseIfNeeded();
            return;
        }
        blockMovementIfNeeded(minecraft);

        boolean down = ACTIVATE.isDown();
        if (down != activateDown) {
            // Edge detection avoids sending a packet every client tick while the key is held.
            activateDown = down;
            send(down ? Action.ACTIVATE : Action.DEACTIVATE);
        }
        while (CYCLE_LEVEL.consumeClick()) {
            send(Action.CYCLE_LEVEL);
        }
    }

    /**
     * Restores the original client-side key suppression used by movement-blocking Semblances.
     */
    private static void blockMovementIfNeeded(Minecraft minecraft) {
        minecraft.player.getCapability(RWBYMCapabilities.SEMBLANCE).ifPresent(semblance -> {
            if (!semblance.isMovementBlocked()) {
                return;
            }
            minecraft.options.keyUp.setDown(false);
            minecraft.options.keyDown.setDown(false);
            minecraft.options.keyLeft.setDown(false);
            minecraft.options.keyRight.setDown(false);
            minecraft.options.keyJump.setDown(false);
            minecraft.options.keyShift.setDown(false);
        });
    }

    private static KeyMapping key(String name, int key) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, key, CATEGORY);
    }

    private static void send(Action action) {
        RWBYMNetwork.CHANNEL.sendToServer(new SemblanceActionPacket(action));
    }

    /**
     * Sends a synthetic release if the client stops polling the active key context.
     */
    private static void releaseIfNeeded() {
        if (activateDown) {
            activateDown = false;
            send(Action.DEACTIVATE);
        }
    }

    /**
     * Prevents the original G-key Semblance bind from fighting special gun magazine controls.
     */
    private static boolean holdingSpecialGun() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }
        ItemStack stack = minecraft.player.getMainHandItem();
        return stack.getItem() instanceof RWBYMWeaponItem weapon && weapon.isSpecialMagazineGunItem();
    }

    private RWBYMSemblanceKeyHandler() {
    }
}
