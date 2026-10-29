package dev.anvilcraft.ping.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.anvilcraft.ping.client.render.PingPointRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    private float spinningEffectTime;
    @Shadow
    private float spinningEffectSpeed;

    @Shadow
    protected abstract void bobHurt(CameraRenderState cameraState, PoseStack poseStack);

    @Shadow
    protected abstract void bobView(CameraRenderState cameraState, PoseStack poseStack);

    @Inject(method = "extractCamera", at = @At("TAIL"))
    private void middle_key_ping$prepareProjection(
        DeltaTracker deltaTracker, float partialTick, float cameraPartialTick, CallbackInfo ci
    ) {
        Minecraft client = Minecraft.getInstance();
        var state = client.gameRenderer.getGameRenderState();
        CameraRenderState camera = state.levelRenderState.cameraRenderState;
        Matrix4f projection = new Matrix4f(camera.projectionMatrix);
        PoseStack bob = new PoseStack();
        this.bobHurt(camera, bob);
        if (state.optionsRenderState.bobView) {
            this.bobView(camera, bob);
        }
        projection.mul(bob.last().pose());

        float screenEffectScale = state.optionsRenderState.screenEffectScale;
        float portalIntensity = Mth.lerp(partialTick, client.player.oPortalEffectIntensity,
            client.player.portalEffectIntensity);
        float nauseaIntensity = client.player.getEffectBlendFactor(MobEffects.NAUSEA, partialTick);
        float intensity = Math.max(portalIntensity, nauseaIntensity) * (screenEffectScale * screenEffectScale);
        if (intensity > 0) {
            float skew = 5 / (intensity * intensity + 5) - intensity * 0.04f;
            skew *= skew;
            Vector3f axis = new Vector3f(0, Mth.SQRT_OF_TWO / 2, Mth.SQRT_OF_TWO / 2);
            float angle = (this.spinningEffectTime + partialTick * this.spinningEffectSpeed)
                * (float) (Math.PI / 180);
            projection.rotate(angle, axis).scale(1 / skew, 1, 1).rotate(-angle, axis);
        }

        // GUI 在世界绘制前提取，必须使用刚提取的相机状态计算同一帧的投影。
        PingPointRenderer.prepareFrame(camera.viewRotationMatrix, projection);
    }
}
