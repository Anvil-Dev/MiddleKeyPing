package dev.anvilcraft.ping.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.anvilcraft.lib.v2.wheel.client.input.WheelScreenController;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.wheel.WheelMenu;
import dev.anvilcraft.ping.util.PingType;
import dev.anvilcraft.ping.util.PingUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID, value = Dist.CLIENT)
public class MiddleKeyPingKeyBinding {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(MiddleKeyPing.of("middle_key_ping"));

    public static final KeyMapping UNIFORM_PING_KEY = new KeyMapping(
        "key.middle_key_ping.uniform",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
        MiddleKeyPingKeyBinding.CATEGORY
    );

    public static final KeyMapping GENERIC_PING_KEY = new KeyMapping(
        "key.middle_key_ping.generic",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        MiddleKeyPingKeyBinding.CATEGORY
    );

    public static final KeyMapping WARNING_PING_KEY = new KeyMapping(
        "key.middle_key_ping.warning",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        MiddleKeyPingKeyBinding.CATEGORY
    );

    public static final KeyMapping GOTO_PING_KEY = new KeyMapping(
        "key.middle_key_ping.goto",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        MiddleKeyPingKeyBinding.CATEGORY
    );

    public static final KeyMapping ENEMY_PING_KEY = new KeyMapping(
        "key.middle_key_ping.enemy",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UNKNOWN,
        MiddleKeyPingKeyBinding.CATEGORY
    );

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.registerCategory(MiddleKeyPingKeyBinding.CATEGORY);
        event.register(MiddleKeyPingKeyBinding.UNIFORM_PING_KEY);
        event.register(MiddleKeyPingKeyBinding.GENERIC_PING_KEY);
        event.register(MiddleKeyPingKeyBinding.WARNING_PING_KEY);
        event.register(MiddleKeyPingKeyBinding.GOTO_PING_KEY);
        event.register(MiddleKeyPingKeyBinding.ENEMY_PING_KEY);
    }

    private static final WheelScreenController CONTROLLER = new WheelScreenController();

    private static long holdUniformPingKeyTime = -1L;

    private static final long HOLD_TIME = 4L;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        long gameTime = client.level.getGameTime();
        if (MiddleKeyPingKeyBinding.UNIFORM_PING_KEY.isDown()) {
            if (
                MiddleKeyPingKeyBinding.holdUniformPingKeyTime < 0
                && !MiddleKeyPingKeyBinding.hasScreen()
            ) {
                MiddleKeyPingKeyBinding.holdUniformPingKeyTime = client.level.getGameTime();
            } else if (
                MiddleKeyPingKeyBinding.holdUniformPingKeyTime > 0
                && gameTime - MiddleKeyPingKeyBinding.holdUniformPingKeyTime > MiddleKeyPingKeyBinding.HOLD_TIME
            ) {
                MiddleKeyPingKeyBinding.CONTROLLER.onHoldKeyPressed(WheelMenu.PING_WHEEL);
            }
        } else if (
            MiddleKeyPingKeyBinding.holdUniformPingKeyTime >= 0
            && gameTime - MiddleKeyPingKeyBinding.holdUniformPingKeyTime <= MiddleKeyPingKeyBinding.HOLD_TIME
            && !MiddleKeyPingKeyBinding.hasScreen()
        ) {
            PingUtil.sendPing(PingType.UNIFORM);
            MiddleKeyPingKeyBinding.holdUniformPingKeyTime = -1L;
        }
        MiddleKeyPingClient.PINGS.removeIf(ping -> ping == null || ping.endTime() < gameTime);
        if (MiddleKeyPingKeyBinding.hasScreen()) return;
        PingType key = null;
        if (GENERIC_PING_KEY.consumeClick()) {
            key = PingType.GENERIC;
        } else if (WARNING_PING_KEY.consumeClick()) {
            key = PingType.WARNING;
        } else if (GOTO_PING_KEY.consumeClick()) {
            key = PingType.GOTO;
        } else if (ENEMY_PING_KEY.consumeClick()) {
            key = PingType.ENEMY;
        }
        if (key == null) return;
        PingUtil.sendPing(key);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft client = Minecraft.getInstance();
        if (
            client.player == null
            || !MiddleKeyPingKeyBinding.UNIFORM_PING_KEY.matches(event.getKeyEvent())
        ) {
            return;
        }
        MiddleKeyPingKeyBinding.processPress(client, event.getAction());
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.MouseButton.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (
            client.player == null
            || !MiddleKeyPingKeyBinding.UNIFORM_PING_KEY.matchesMouse(new MouseButtonEvent(0, 0, event.getMouseButtonInfo()))
        ) {
            return;
        }
        MiddleKeyPingKeyBinding.processPress(client, event.getAction());
    }

    private static void processPress(Minecraft client, int action) {
        if (client.level == null) return;
        if (action == GLFW.GLFW_RELEASE && MiddleKeyPingKeyBinding.holdUniformPingKeyTime >= MiddleKeyPingKeyBinding.HOLD_TIME) {
            MiddleKeyPingKeyBinding.CONTROLLER.onHoldKeyReleased();
        }
    }

    private static boolean hasScreen() {
        return Minecraft.getInstance().screen != null;
    }
}
