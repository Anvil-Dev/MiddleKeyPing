package dev.anvilcraft.ping.client.integration.internal;

import dev.anvilcraft.lib.v2.integration.Integration;
import dev.anvilcraft.lib.v2.integration.IntegrationType;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.client.PingIntegration;
import dev.anvilcraft.ping.util.Ping;
import dev.anvilcraft.ping.util.PingType;
import dev.anvilcraft.ping.util.PingUtil;
import dev.anvilcraft.ping.wheel.MapPingWheel;
import dev.dubhe.map.api.ping.MapPingInput;
import dev.dubhe.map.api.ping.MinimapPingAPI;
import dev.dubhe.map.ping.MapPingData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Integration(value = "aleeve_atlas", version = "[0.0.2,)", type = { IntegrationType.CLIENT })
public final class AtlasPingsImpl implements PingIntegration {
    private static final Map<Ping, MapPingData> LINKS = new IdentityHashMap<>();

    public void applyClient() {
        MiddleKeyPingClient.setIntegration(this);
    }

    @Override
    public void initialize() {
        MinimapPingAPI.setWorldInputEnabled(false);
        MinimapPingAPI.setWorldPingsEnabled(false);
        MinimapPingAPI.setMapInput(new MapPingInput() {
            @Override
            public void ping(Vec3 position) {
                PingUtil.send(position, PingType.GENERIC);
            }

            @Override
            public void openWheel(Screen parent, Vec3 position, Runnable onClose) {
                Minecraft.getInstance().setScreen(new MapPingWheel(parent, position, onClose));
            }
        });
    }

    @Override
    public void add(Ping ping, Component sender) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        var connection = client.getConnection();
        var playerInfo = connection == null ? null : connection.getPlayerInfo(sender.getString());
        add(ping, playerInfo == null ? null : playerInfo.getProfile().id(), sender.getString(),
                MapPingData.dimensionId(client.level.dimension()), ping.pingType().color(), System.currentTimeMillis());
        this.tick();
    }

    static void add(Ping ping, @Nullable UUID senderId, String senderName, String dimension, int color, long now) {
        LINKS.put(ping, new MapPingData(UUID.randomUUID(), senderId, senderName, color,
                dimension, ping.position() == null ? Vec3.ZERO : ping.position(), ping.entity(),
                icon(ping.pingType()), now, 0));
    }

    @Override
    public void tick() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        sync(MiddleKeyPingClient.PINGS, client.level.getGameTime(), System.currentTimeMillis(), id -> {
            Entity entity = MiddleKeyPingClient.getEntity(id);
            return entity == null ? null : entity.position();
        });
    }

    static void sync(Collection<Ping> pings, long gameTime, long now,
            Function<UUID, Vec3> entityPosition) {
        Set<Ping> retained = Collections.newSetFromMap(new IdentityHashMap<>());
        retained.addAll(pings);
        LINKS.entrySet().removeIf(entry -> {
            if (!retained.contains(entry.getKey()) || entry.getKey().endTime() < gameTime) {
                MinimapPingAPI.removePing(entry.getValue().pingId());
                return true;
            }
            return false;
        });
        LINKS.replaceAll((ping, data) -> {
            Vec3 position = ping.entity() == null ? ping.position() : entityPosition.apply(ping.entity());
            if (position == null) {
                if (data.lifetimeMillis() == 0) {
                    return data;
                }
                position = data.position();
            }
            long lifetime = now - data.createdAtMillis() + (ping.endTime() - gameTime + 1) * 50;
            MapPingData updated = new MapPingData(data.pingId(), data.senderId(), data.senderName(),
                    data.teamColorArgb(), data.dimensionId(), position, data.targetEntityId(),
                    data.icon(), data.createdAtMillis(), lifetime);
            MinimapPingAPI.registerLocalPing(updated);
            return updated;
        });
    }

    @Override
    public List<Ping> visiblePings() {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return List.of();
        }
        String dimension = MapPingData.dimensionId(client.level.dimension());
        long now = System.currentTimeMillis();
        long gameTime = client.level.getGameTime();
        return MinimapPingAPI.activePings().stream()
                .filter(ping -> ping.dimensionId().equals(dimension) && !ping.expired(now))
                .map(ping -> new Ping(ping.position(), ping.targetEntityId(), type(ping.icon()),
                        gameTime + (ping.lifetimeMillis() - now + ping.createdAtMillis() + 49) / 50))
                .toList();
    }

    @Override
    public void clear() {
        LINKS.values().forEach(ping -> MinimapPingAPI.removePing(ping.pingId()));
        LINKS.clear();
    }

    private static MapPingData.PingIcon icon(PingType type) {
        return switch (type) {
            case ENEMY -> MapPingData.PingIcon.DANGER;
            case WARNING -> MapPingData.PingIcon.WARNING;
            case GOTO -> MapPingData.PingIcon.GOTO;
            case GENERIC, UNIFORM -> MapPingData.PingIcon.GENERAL;
        };
    }

    private static PingType type(MapPingData.PingIcon icon) {
        return switch (icon) {
            case DANGER -> PingType.ENEMY;
            case WARNING -> PingType.WARNING;
            case GOTO -> PingType.GOTO;
            case GENERAL -> PingType.GENERIC;
        };
    }
}
