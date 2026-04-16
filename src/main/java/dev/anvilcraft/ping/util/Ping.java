package dev.anvilcraft.ping.util;

import dev.anvilcraft.ping.network.payload.EntityPingPayload;
import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public record Ping(@Nullable Vec3 position, @Nullable UUID entity, PingType pingType, long endTime) {
    public Ping(PositionPingPayload payload, long endTime) {
        this(payload.position(), null, payload.pingType(), endTime);
    }

    public Ping(EntityPingPayload payload, long endTime) {
        this(null, payload.uuid(), payload.pingType(), endTime);
    }

    public boolean isPosition() {
        return this.position != null;
    }

    public boolean isEntity() {
        return this.entity != null;
    }
}
