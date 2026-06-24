package io.github.blaezdev.rwbym.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.blaezdev.rwbym.RWBYM;
import io.github.blaezdev.rwbym.item.RWBYMWeaponItem;
import io.github.blaezdev.rwbym.network.RWBYMNetwork;
import io.github.blaezdev.rwbym.network.SpecialGunActionPacket;
import io.github.blaezdev.rwbym.network.SpecialGunActionPacket.Action;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, value = Dist.CLIENT)
public final class RWBYMSpecialGunKeyHandler {
    private static final String CATEGORY = "key.categories.rwbym.special";
    private static final int LEFT_MOUSE = GLFW.GLFW_MOUSE_BUTTON_LEFT;
    private static final int RIGHT_MOUSE = GLFW.GLFW_MOUSE_BUTTON_RIGHT;

    private static final KeyMapping CYCLE_ACTION = key("key.rwbym.special.cycle_action", GLFW.GLFW_KEY_T);
    private static final KeyMapping REMOVE_ROUND = key("key.rwbym.special.remove_round", GLFW.GLFW_KEY_R);
    private static final KeyMapping HAMMER = key("key.rwbym.special.hammer", GLFW.GLFW_KEY_B);
    private static final KeyMapping MAG_RELEASE = key("key.rwbym.special.mag_release", GLFW.GLFW_KEY_G);
    private static final KeyMapping INSERT_MAG = key("key.rwbym.special.insert_mag", GLFW.GLFW_KEY_Z);
    private static final KeyMapping FIRE_SELECT = key("key.rwbym.special.fire_select", GLFW.GLFW_KEY_V);

    private static boolean shootDown;
    private static boolean adsDown;

    @Mod.EventBusSubscriber(modid = RWBYM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ModBus {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(CYCLE_ACTION);
            event.register(REMOVE_ROUND);
            event.register(HAMMER);
            event.register(MAG_RELEASE);
            event.register(INSERT_MAG);
            event.register(FIRE_SELECT);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().screen != null || !holdingSpecialGun()) {
            resetMouseState();
            return;
        }
        while (CYCLE_ACTION.consumeClick()) {
            send(Action.CYCLE_ACTION, true);
        }
        while (REMOVE_ROUND.consumeClick()) {
            send(Action.REMOVE_ROUND, true);
        }
        while (HAMMER.consumeClick()) {
            send(Action.HAMMER, true);
        }
        while (MAG_RELEASE.consumeClick()) {
            send(Action.MAG_RELEASE, true);
        }
        while (INSERT_MAG.consumeClick()) {
            send(Action.INSERT_MAG, true);
        }
        while (FIRE_SELECT.consumeClick()) {
            send(Action.FIRE_SELECT, true);
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton event) {
        if (Minecraft.getInstance().screen != null || !holdingSpecialGun()) {
            return;
        }
        if (event.getButton() == LEFT_MOUSE) {
            boolean down = event.getAction() != GLFW.GLFW_RELEASE;
            if (down != shootDown) {
                shootDown = down;
                send(Action.SHOOT, down);
            }
            event.setCanceled(true);
        } else if (event.getButton() == RIGHT_MOUSE) {
            boolean down = event.getAction() != GLFW.GLFW_RELEASE;
            if (down != adsDown) {
                adsDown = down;
                send(Action.ADS, down);
            }
            event.setCanceled(true);
        }
    }

    private static KeyMapping key(String name, int key) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, key, CATEGORY);
    }

    private static void send(Action action, boolean down) {
        RWBYMNetwork.CHANNEL.sendToServer(new SpecialGunActionPacket(action, InteractionHand.MAIN_HAND, down));
    }

    private static boolean holdingSpecialGun() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return false;
        }
        ItemStack stack = minecraft.player.getMainHandItem();
        return stack.getItem() instanceof RWBYMWeaponItem weapon && weapon.isSpecialMagazineGunItem();
    }

    private static void resetMouseState() {
        if (shootDown) {
            send(Action.SHOOT, false);
            shootDown = false;
        }
        if (adsDown) {
            send(Action.ADS, false);
            adsDown = false;
        }
    }

    private RWBYMSpecialGunKeyHandler() {
    }
}
