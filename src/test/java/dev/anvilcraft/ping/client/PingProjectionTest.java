package dev.anvilcraft.ping.client;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingProjectionTest {
    @AfterEach
    void clearFrame() {
        PingProjection.clear();
    }

    @Test
    void usesWorldBobbingAndCopiesTheSuppliedProjection() {
        Matrix4f projection = perspective(70).translate(0.2f, -0.1f, 0).rotateZ(0.05f);
        PingProjection.capture(projection);
        var projected = PingProjection.project(new Vec3(0, 0, -10), new Matrix4f());
        assertTrue(projected.x > 0);
        assertTrue(projected.y < 0);
        projection.identity();
        var after = PingProjection.project(new Vec3(0, 0, -10), new Matrix4f());
        assertEquals(projected.x, after.x);
        assertEquals(projected.y, after.y);
    }

    @Test
    void followsDynamicFieldOfViewAndCameraRotation() {
        PingProjection.capture(perspective(70));
        float normal = PingProjection.project(new Vec3(2, 0, -10), new Matrix4f()).x;
        PingProjection.capture(perspective(100));
        float wide = PingProjection.project(new Vec3(2, 0, -10), new Matrix4f()).x;
        assertTrue(wide < normal);
        var centered = PingProjection.project(new Vec3(10, 0, 0),
                new Matrix4f().rotateY((float) Math.PI / 2));
        assertEquals(0, centered.x, 0.00001);
    }

    @Test
    void hidesPointsBehindCameraAndInvalidFrames() {
        PingProjection.clear();
        assertNull(PingProjection.project(new Vec3(0, 0, -10), new Matrix4f()));
        PingProjection.capture(perspective(70));
        assertNull(PingProjection.project(new Vec3(0, 0, 10), new Matrix4f()));
        PingProjection.clear();
        assertNull(PingProjection.project(new Vec3(0, 0, -10), new Matrix4f()));
    }

    private static Matrix4f perspective(float degrees) {
        return new Matrix4f().perspective((float) Math.toRadians(degrees), 16f / 9, 0.05f, 1000);
    }
}
