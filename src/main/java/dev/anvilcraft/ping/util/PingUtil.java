package dev.anvilcraft.ping.util;

import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.network.payload.EntityPingPayload;
import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class PingUtil {
    public static void send(Vec3 position, PingType pingType) {
        if (pingType == PingType.UNIFORM) pingType = PingType.GENERIC;
        ClientPacketDistributor.sendToServer(new PositionPingPayload(Component.empty(), position, pingType));
    }


    public static void sendEntity(Entity entity, PingType pingType) {
        if (pingType == PingType.UNIFORM) pingType = PingType.GENERIC;
        ClientPacketDistributor.sendToServer(new EntityPingPayload(Component.empty(), entity.getUUID(), pingType));
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
