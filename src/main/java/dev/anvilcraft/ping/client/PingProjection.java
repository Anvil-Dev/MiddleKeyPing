package dev.anvilcraft.ping.client;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;

import javax.annotation.Nullable;

/// 保存本帧世界渲染的最终投影，供客户端渲染线程投影标点。
public final class PingProjection {
    private static final Matrix4f PROJECTION = new Matrix4f();
    private static boolean ready;

    private PingProjection() {
    }

    /// 在世界渲染入口复制投影，保留视野角、晃动和屏幕特效变换。
    public static void capture(Matrix4fc projection) {
        PROJECTION.set(projection);
        ready = true;
    }

    /// 世界卸载时清理本帧投影，避免使用上一世界的数据。
    public static void clear() {
        ready = false;
    }

    /// 把相机相对坐标变换到标准化设备坐标，后方目标或无效帧返回空值。
    public static @Nullable Vector4f project(Vec3 relative, Matrix4fc modelView) {
        if (!ready) {
            return null;
        }
        Vector4f clip = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, 1);
        clip.mul(modelView).mul(PROJECTION);
        if (clip.w <= 0) {
            return null;
        }
        return clip.div(clip.w);
    }
}
