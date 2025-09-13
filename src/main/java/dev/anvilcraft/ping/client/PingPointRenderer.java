package dev.anvilcraft.ping.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.util.Ping;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID, value = Dist.CLIENT)
public class PingPointRenderer {
    @SubscribeEvent
    public static void onLevelRender(RenderLevelStageEvent event) {
        if (!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) return;

        PingPointRenderer.render(event.getPoseStack(), event.getCamera());
    }

    public static void render(PoseStack poseStack, Camera camera) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        Vec3 cameraPosition = camera.getPosition();
        for (Ping ping : MiddleKeyPingClient.PINGS.get(client.level.dimension())) {
            if (ping == null) return;
            float scale = (float) ping.position().subtract(cameraPosition).length() * 0.015f;
            float x = (float) (ping.position().x() - cameraPosition.x());
            float y = (float) (ping.position().y() - cameraPosition.y());
            float z = (float) (ping.position().z() - cameraPosition.z());
            poseStack.pushPose();
            PoseStack.Pose last = poseStack.last();
            RenderBuffers renderBuffers = client.renderBuffers();
            MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
            VertexConsumer vertex = bufferSource.getBuffer(RenderType.DEBUG_QUADS);
            poseStack.translate(x, y, z);
            poseStack.mulPose(camera.rotation());
            vertex.addVertex(last.pose(), 0, 0 + scale * 3, 0).setColor(ping.pingType().color());
            vertex.addVertex(last.pose(), 0 + scale, 0 + scale * 2, 0).setColor(ping.pingType().color());
            vertex.addVertex(last.pose(), 0, 0, 0).setColor(ping.pingType().color());
            vertex.addVertex(last.pose(), 0 - scale, 0 + scale * 2, 0).setColor(ping.pingType().color());
            poseStack.popPose();
        }
    }
}
