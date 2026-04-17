package dev.anvilcraft.ping.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.anvilcraft.lib.v2.wheel.client.input.WheelScreenController;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.network.payload.EntityPingPayload;
import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.wheel.WheelMenu;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID, value = Dist.CLIENT)
public class PingUtil {
    private static final WheelScreenController CONTROLLER = new WheelScreenController();

    public static void send(Vec3 position, PingType pingType) {
        if (pingType == PingType.UNIFORM) pingType = PingType.GENERIC;
        PacketDistributor.sendToServer(new PositionPingPayload(Component.empty(), position, pingType));
    }


    public static void sendEntity(Entity entity, PingType pingType) {
        if (pingType == PingType.UNIFORM) pingType = PingType.GENERIC;
        PacketDistributor.sendToServer(new EntityPingPayload(Component.empty(), entity.getUUID(), pingType));
    }

    public static final KeyMapping UNIFORM_PING_KEY = new KeyMapping(
        "key.middle_key_ping.uniform",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
        "key.categories.middle_key_ping"
    );

    public static final KeyMapping GENERIC_PING_KEY = new KeyMapping(
        "key.middle_key_ping.generic",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_KP_0,
        "key.categories.middle_key_ping"
    );

    public static final KeyMapping WARNING_PING_KEY = new KeyMapping(
        "key.middle_key_ping.warning",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_KP_1,
        "key.categories.middle_key_ping"
    );

    public static final KeyMapping GOTO_PING_KEY = new KeyMapping(
        "key.middle_key_ping.goto",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_KP_2,
        "key.categories.middle_key_ping"
    );

    public static final KeyMapping ENEMY_PING_KEY = new KeyMapping(
        "key.middle_key_ping.enemy",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_KP_3,
        "key.categories.middle_key_ping"
    );

    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(UNIFORM_PING_KEY);
        event.register(GENERIC_PING_KEY);
        event.register(WARNING_PING_KEY);
        event.register(GOTO_PING_KEY);
        event.register(ENEMY_PING_KEY);
    }


    private static boolean holdUniformPingKeyWasDown = false;

    private static long holdUniformPingKeyTime = -1L;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        long gameTime = client.level.getGameTime();
        if (holdUniformPingKeyTime > 0 && gameTime - holdUniformPingKeyTime > 4) {
            CONTROLLER.onHoldKeyPressed(WheelMenu.PING_WHEEL);
            holdUniformPingKeyWasDown = true;
        }
        MiddleKeyPingClient.PINGS.removeIf(ping -> ping == null || ping.endTime() < gameTime);
        if(PingUtil.hasScreen()) return;
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
        if (client.player == null || !PingUtil.UNIFORM_PING_KEY.matches(event.getKey(), event.getScanCode())) {
            return;
        }
        PingUtil.processPress(client, event.getAction());
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.MouseButton.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !PingUtil.UNIFORM_PING_KEY.matchesMouse(event.getButton())) {
            return;
        }
        PingUtil.processPress(client, event.getAction());
    }

    private static void processPress(Minecraft client, int action) {
        if (client.level == null) return;
        if (action == GLFW.GLFW_RELEASE) {
            if (holdUniformPingKeyWasDown) {
                CONTROLLER.onHoldKeyReleased();
            } else {
                PingUtil.sendPing(PingType.UNIFORM);
            }
            holdUniformPingKeyWasDown = false;
            holdUniformPingKeyTime = -1L;
            return;
        }
        if(PingUtil.hasScreen()) return;
        if (action == GLFW.GLFW_PRESS) {
            if (!holdUniformPingKeyWasDown) {
                holdUniformPingKeyTime = client.level.getGameTime();
            }
        }
    }

    private static boolean hasScreen() {
        return Minecraft.getInstance().screen != null;
    }

    public static void sendPing(PingType key) {
        Minecraft client = Minecraft.getInstance();
        Entity camera = client.getCameraEntity();
        if (camera == null) return;
        HitResult pick = HitUtil.pick(
            camera,
            MiddleKeyPingClient.CONFIG.getMaxPingDistance(),
            MiddleKeyPingClient.CONFIG.isAllowPingEmpty()
        );
        if (pick == null) return;
        Vec3 location = pick.getLocation();
        if (pick.getType() == HitResult.Type.ENTITY && pick instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            if (key == PingType.UNIFORM && !entity.getType().getCategory().isFriendly()) {
                PingUtil.sendEntity(entity, PingType.ENEMY);
                return;
            }
            PingUtil.sendEntity(entity, key);
            return;
        } else {
            location = location.add(camera.getEyePosition().subtract(location).normalize().scale(0.025d));
        }
        PingUtil.send(location, key);
    }
}
