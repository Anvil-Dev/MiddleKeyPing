package dev.anvilcraft.ping.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;

import javax.annotation.Nullable;

public record PingIconRenderState(Matrix3x2f pose, int color, ScreenRectangle bounds) implements GuiElementRenderState {
    public PingIconRenderState(Matrix3x2f pose, int color) {
        this(pose, color, PingIconRenderState.getBounds(pose));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(this.pose, 0, 15).setColor(this.color);
        consumer.addVertexWith2DPose(this.pose, 5, 10).setColor(this.color);
        consumer.addVertexWith2DPose(this.pose, 0, 0).setColor(this.color);
        consumer.addVertexWith2DPose(this.pose, -5, 10).setColor(this.color);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.DEBUG_QUADS;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return null;
    }

    private static ScreenRectangle getBounds(Matrix3x2f pose) {
        return new ScreenRectangle(0, 0, 10, 15).transformMaxBounds(pose);
    }
}
