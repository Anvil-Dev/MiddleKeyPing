package dev.anvilcraft.ping.client;

import com.mojang.blaze3d.platform.Window;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.mixin.GameRendererAccessor;
import dev.anvilcraft.ping.mixin.GuiGraphicsAccessor;
import dev.anvilcraft.ping.util.Ping;
import dev.anvilcraft.ping.util.PingType;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderPipelines;
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
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
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
    public static void onLevelRender(RenderLevelStageEvent.AfterParticles event) {
        PingPointRenderer.collectProjectedPings(event.getModelViewMatrix());
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

    private static void collectProjectedPings(Matrix4f modelViewMatrix) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            PROJECTED_PINGS.clear();
            return;
        }

        Camera camera = client.gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.position();
        TickRateManager tickRateManager = client.level.tickRateManager();
        PROJECTED_PINGS.clear();

        DeltaTracker partialTick = client.getDeltaTracker();
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
                    PingPointRenderer.getProjectionMatrix(camera, partialTick, client.gameRenderer)
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
                    PingPointRenderer.getProjectionMatrix(camera, partialTick, client.gameRenderer)
                );
                if (projected != null) {
                    PROJECTED_PINGS.add(projected);
                }
            }
        }
    }

    private static Matrix4f getProjectionMatrix(Camera camera, DeltaTracker partialTick, GameRenderer gameRenderer) {
        return gameRenderer.getProjectionMatrix(((GameRendererAccessor) gameRenderer).invokeGetFov(
            camera,
            partialTick.getGameTimeDeltaTicks(),
            true
        ));
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
        Matrix3x2fStack pose = guiGraphics.pose();
        pose.pushMatrix();
        pose.translate(ping.screenX, ping.screenY);
        pose.scale(ping.scale, ping.scale);

        if (!ping.stack.isEmpty()) {
            guiGraphics.renderItem(ping.stack, -8, -8);
        } else {
            PingPointRenderer.renderDefaultPingIcon(guiGraphics, ping.pingType.color());
        }

        PingPointRenderer.renderDistanceTag(client, guiGraphics, ping);

        pose.popMatrix();
    }

    private static void renderDistanceTag(
        Minecraft client,
        GuiGraphics guiGraphics,
        ProjectedPing ping
    ) {
        Component distanceComponent = Component.literal("%,.1f m".formatted(ping.distance));
        int textWidth = client.font.width(distanceComponent);
        int x = (int) (- textWidth / 2.0F);
        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, -25);
        guiGraphics.fill(
            RenderPipelines.DEBUG_QUADS,
            -1,
            -1,
            textWidth,
            client.font.lineHeight,
            ping.pingType.color() & 0x66FFFFFF
        );
        ((GuiGraphicsAccessor) guiGraphics).getGuiRenderState().submitText(new GuiTextRenderState(
            client.font,
            distanceComponent.getVisualOrderText(),
            new Matrix3x2f(poseStack),
            0,
            0,
            ping.pingType.textColor(),
            0, // backgroundColor貌似不生效
            false,
            true,
            null
        ));
        poseStack.popMatrix();
    }

    private static void renderDefaultPingIcon(GuiGraphics graphics, int color) {
        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.scale(1, -1);
        graphics.submitGuiElementRenderState(new PingIconRenderState(new Matrix3x2f(pose), color));
        pose.popMatrix();
    }
}
