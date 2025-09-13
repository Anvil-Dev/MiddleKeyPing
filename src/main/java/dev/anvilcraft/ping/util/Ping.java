package dev.anvilcraft.ping.util;

import com.mojang.blaze3d.platform.InputConstants;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.network.PingPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID, value = Dist.CLIENT)
public record Ping(Vec3 position, PingType pingType, long endTime) {
    public Ping(PingPayload payload, long endTime) {
        this(payload.position(), payload.pingType(), endTime);
    }

    public static void send(Vec3 position, PingType pingType) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        ClientPacketListener connection = client.getConnection();
        if (connection == null) return;
        ResourceKey<Level> dimension = client.level.dimension();
        connection.send(new PingPayload(dimension, position, pingType));
    }

    public static void sendGenric(Vec3 position) {
        Ping.send(position, PingType.GENRIC);
    }

    public static void sendWarning(Vec3 position) {
        Ping.send(position, PingType.WARNING);
    }

    public static void sendGoto(Vec3 position) {
        Ping.send(position, PingType.GOTO);
    }

    public static void sendEnemy(Vec3 position) {
        Ping.send(position, PingType.ENEMY);
    }


    public static final KeyMapping GENRIC_PING_KEY = new KeyMapping(
        "key.middle_key_ping.genric",
        InputConstants.Type.MOUSE,
        GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
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

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(GENRIC_PING_KEY);
        event.register(WARNING_PING_KEY);
        event.register(GOTO_PING_KEY);
        event.register(ENEMY_PING_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        long gameTime = client.level.getGameTime();
        MiddleKeyPingClient.PINGS.values().removeIf(ping -> ping == null || ping.endTime() < gameTime);
        int key = -1;
        if (GENRIC_PING_KEY.consumeClick()) {
            key = 0;
        } else if (WARNING_PING_KEY.consumeClick()) {
            key = 1;
        } else if (GOTO_PING_KEY.consumeClick()) {
            key = 2;
        } else if (ENEMY_PING_KEY.consumeClick()) {
            key = 3;
        }
        if (key < 0) return;
        Entity camera = client.getCameraEntity();
        if (camera == null) return;
        HitResult pick = HitUtil.pick(camera, 200.0d);
        Vec3 location = pick.getLocation();
        if (pick.getType() == HitResult.Type.ENTITY && pick instanceof EntityHitResult entityHitResult) {
            Entity entity = entityHitResult.getEntity();
            location = entity.getEyePosition().add(0.0, 0.3d, 0.0);
            if (key == 0 && !entity.getType().getCategory().isFriendly()) {
                Ping.sendEnemy(location);
                return;
            }
        } else {
            location = location.add(camera.getEyePosition().subtract(location).normalize().scale(0.025d));
        }
        switch (key) {
            case 0 -> Ping.sendGenric(location);
            case 1 -> Ping.sendWarning(location);
            case 2 -> Ping.sendGoto(location);
            case 3 -> Ping.sendEnemy(location);
        }
    }
}
