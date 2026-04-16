package dev.anvilcraft.ping.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.util.Ping;
import dev.anvilcraft.ping.util.PingType;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("DuplicatedCode")
@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID, value = Dist.CLIENT)
public class PingPointRenderer {
    private static final List<ProjectedPing> PROJECTED_PINGS = new ArrayList<>();

    private record ProjectedPing(
        float screenX,
        float screenY,
        double distance,
        float scale,
        PingType pingType,
        ItemStack stack,
        boolean behindCamera
    ) {
    }

    @SubscribeEvent
    public static void onLevelRender(RenderLevelStageEvent event) {
        if (!event.getStage().equals(RenderLevelStageEvent.Stage.AFTER_PARTICLES)) return;

        PingPointRenderer.collectProjectedPings(
            event.getCamera(),
            event.getPartialTick(),
            event.getModelViewMatrix(),
            event.getProjectionMatrix()
        );
    }

    @SubscribeEvent
    public static void onGuiRender(RenderGuiLayerEvent.Pre event) {
        if (PROJECTED_PINGS.isEmpty()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;
        GuiGraphics guiGraphics = event.getGuiGraphics();
        Window window = client.getWindow();

        for (ProjectedPing ping : PROJECTED_PINGS) {
            if (ping.behindCamera) continue;
            if (ping.screenX < 0 || ping.screenX > window.getGuiScaledWidth()) continue;
            if (ping.screenY < 0 || ping.screenY > window.getGuiScaledHeight()) continue;
            PingPointRenderer.renderProjectedPing(client, guiGraphics, ping);
        }
    }

    private static void collectProjectedPings(
        Camera camera,
        DeltaTracker partialTick,
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix
    ) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            PROJECTED_PINGS.clear();
            return;
        }

        Vec3 cameraPosition = camera.getPosition();
        TickRateManager tickRateManager = client.level.tickRateManager();
        PROJECTED_PINGS.clear();

        Iterator<Ping> iterator = MiddleKeyPingClient.PINGS.iterator();
        while (iterator.hasNext()) {
            Ping ping = iterator.next();
            if (ping == null) {
                iterator.remove();
                continue;
            }

            if (ping.isPosition() && ping.position() != null) {
                ProjectedPing projected = PingPointRenderer.projectToScreen(
                    cameraPosition,
                    ping.position(),
                    ping.pingType(),
                    ItemStack.EMPTY,
                    modelViewMatrix,
                    projectionMatrix
                );
                if (projected != null) {
                    PROJECTED_PINGS.add(projected);
                }
            } else if (ping.isEntity() && ping.entity() != null) {
                Entity entity = MiddleKeyPingClient.getEntity(ping.entity());
                if (entity == null) {
                    iterator.remove();
                    continue;
                }

                float entityPartialTick = partialTick.getGameTimeDeltaPartialTick(!tickRateManager.isEntityFrozen(entity));
                Vec3 worldPosition;
                ItemStack stack = ItemStack.EMPTY;
                if (entity instanceof ItemEntity itemEntity) {
                    worldPosition = entity.getPosition(entityPartialTick).add(0, 0.5, 0);
                    stack = itemEntity.getItem();
                } else {
                    worldPosition = entity.getEyePosition(entityPartialTick).add(0, 0.25, 0);
                }

                ProjectedPing projected = PingPointRenderer.projectToScreen(
                    cameraPosition,
                    worldPosition,
                    ping.pingType(),
                    stack,
                    modelViewMatrix,
                    projectionMatrix
                );
                if (projected != null) {
                    PROJECTED_PINGS.add(projected);
                }
            }
        }
    }

    private static @Nullable ProjectedPing projectToScreen(
        Vec3 cameraPosition,
        Vec3 worldPosition,
        PingType pingType,
        ItemStack stack,
        Matrix4f modelViewMatrix,
        Matrix4f projectionMatrix
    ) {
        Minecraft client = Minecraft.getInstance();
        Window window = client.getWindow();

        Vec3 relative = worldPosition.subtract(cameraPosition);
        Vector4f clipSpace = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1.0F);
        clipSpace.mul(modelViewMatrix);
        clipSpace.mul(projectionMatrix);

        float depth = clipSpace.w;
        if (depth == 0.0F) {
            return null;
        }

        clipSpace.div(depth);
        float screenX = window.getGuiScaledWidth() * (0.5F + clipSpace.x * 0.5F);
        float screenY = window.getGuiScaledHeight() * (0.5F - clipSpace.y * 0.5F);
        double distance = relative.length();
        float scale = (float) (Math.max(1.0, 2.0 / Math.pow(distance, 0.3)) * 0.5);

        return new ProjectedPing(screenX, screenY, distance, scale, pingType, stack.copy(), depth < 0.0F);
    }

    private static void renderProjectedPing(
        Minecraft client,
        GuiGraphics guiGraphics,
        ProjectedPing ping
    ) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(ping.screenX, ping.screenY, -9999.0F);
        poseStack.scale(ping.scale, ping.scale, 1.0F);

        if (!ping.stack.isEmpty()) {
            guiGraphics.renderItem(ping.stack, -8, -8);
        } else {
            PingPointRenderer.renderDefaultPingIcon(guiGraphics, ping.pingType.color());
        }

        PingPointRenderer.renderDistanceTag(client, guiGraphics, ping);

        poseStack.popPose();
    }

    private static void renderDistanceTag(
        Minecraft client,
        GuiGraphics guiGraphics,
        ProjectedPing ping
    ) {
        Component distanceComponent = Component.literal("%,.1f m".formatted(ping.distance));
        int textWidth = client.font.width(distanceComponent);
        int x = (int) (- textWidth / 2.0F);
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, -25, 0);
        client.font.drawInBatch(
            distanceComponent,
            (float) 0,
            (float) 0,
            ping.pingType.textColor(),
            false,
            poseStack.last().pose(),
            guiGraphics.bufferSource(),
            Font.DisplayMode.NORMAL,
            ping.pingType.color() & 0x66FFFFFF,
            LightTexture.FULL_BRIGHT
        );
        guiGraphics.flush();
        poseStack.popPose();
    }

    private static void renderDefaultPingIcon(GuiGraphics guiGraphics, int color) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.scale(1, -1, 1);
        PoseStack.Pose last = poseStack.last();
        RenderBuffers renderBuffers = Minecraft.getInstance().renderBuffers();
        MultiBufferSource.BufferSource bufferSource = renderBuffers.bufferSource();
        VertexConsumer vertex = bufferSource.getBuffer(RenderType.DEBUG_QUADS);
        vertex.addVertex(last.pose(), 0, 15, 0).setColor(color);
        vertex.addVertex(last.pose(), 5, 10, 0).setColor(color);
        vertex.addVertex(last.pose(), 0, 0, 0).setColor(color);
        vertex.addVertex(last.pose(), -5, 10, 0).setColor(color);
        poseStack.popPose();
    }
}
