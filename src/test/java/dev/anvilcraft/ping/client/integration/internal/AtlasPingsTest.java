package dev.anvilcraft.ping.client.integration.internal;

import dev.anvilcraft.ping.util.Ping;
import dev.anvilcraft.ping.util.PingType;
import dev.dubhe.map.api.ping.MinimapPingAPI;
import dev.dubhe.map.ping.ClientPings;
import dev.dubhe.map.ping.MapPingData;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AtlasPingsTest {
    @AfterEach
    void clearPings() {
        new AtlasPingsImpl().clear();
        ClientPings.clear();
    }

    @Test
    void fixedPingKeepsIdentityAndGameTimeLifetime() {
        Ping ping = new Ping(new Vec3(24, 70, -32), null, PingType.GOTO, 300);
        add(ping);
        AtlasPingsImpl.sync(List.of(ping), 100, 2000, id -> null);
        MapPingData original = MinimapPingAPI.activePings().getFirst();
        assertEquals(ping.position(), original.position());
        assertEquals(MapPingData.PingIcon.GOTO, original.icon());
        assertFalse(original.expired(12000));
        AtlasPingsImpl.sync(List.of(ping), 100, 7000, id -> null);
        MapPingData paused = MinimapPingAPI.activePings().getFirst();
        assertEquals(original.pingId(), paused.pingId());
        assertEquals(original.createdAtMillis(), paused.createdAtMillis());
        assertFalse(paused.expired(17000));
        AtlasPingsImpl.sync(List.of(ping), 301, 17000, id -> null);
        assertTrue(MinimapPingAPI.activePings().isEmpty());
    }

    @Test
    void entityPingWaitsForTargetThenTracksMovement() {
        UUID entity = UUID.randomUUID();
        Ping ping = new Ping(null, entity, PingType.ENEMY, 300);
        add(ping);
        AtlasPingsImpl.sync(List.of(ping), 100, 2000, id -> null);
        assertTrue(MinimapPingAPI.activePings().isEmpty());
        AtlasPingsImpl.sync(List.of(ping), 101, 2050, id -> new Vec3(24, 70, -32));
        MapPingData original = MinimapPingAPI.activePings().getFirst();
        assertEquals(entity, original.targetEntityId());
        assertEquals(MapPingData.PingIcon.DANGER, original.icon());
        Vec3 moved = new Vec3(48, 75, -64);
        AtlasPingsImpl.sync(List.of(ping), 102, 2100, id -> moved);
        MapPingData updated = MinimapPingAPI.activePings().getFirst();
        assertEquals(original.pingId(), updated.pingId());
        assertEquals(moved, updated.position());
        AtlasPingsImpl.sync(List.of(ping), 103, 30000, id -> null);
        MapPingData unloaded = MinimapPingAPI.activePings().getFirst();
        assertEquals(moved, unloaded.position());
        assertFalse(unloaded.expired(30000));
    }

    @Test
    void trimmingSourcePingsAlsoRemovesTheirMapMarkers() {
        Ping first = new Ping(new Vec3(24, 70, -32), null, PingType.GENERIC, 300);
        Ping second = new Ping(first.position(), null, PingType.GENERIC, 300);
        add(first);
        add(second);
        AtlasPingsImpl.sync(List.of(first, second), 100, 2000, id -> null);
        assertEquals(2, MinimapPingAPI.activePings().size());
        AtlasPingsImpl.sync(List.of(second), 101, 2050, id -> null);
        assertEquals(1, MinimapPingAPI.activePings().size());
        AtlasPingsImpl.sync(List.of(), 102, 2100, id -> null);
        assertTrue(MinimapPingAPI.activePings().isEmpty());
    }

    private static void add(Ping ping) {
        AtlasPingsImpl.add(ping, UUID.randomUUID(), "foo", "minecraft:overworld", 0xFFFFFFFF, 1000);
    }
}
