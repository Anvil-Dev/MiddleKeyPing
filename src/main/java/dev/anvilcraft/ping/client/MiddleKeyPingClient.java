package dev.anvilcraft.ping.client;

import dev.anvilcraft.lib.v2.config.ConfigManager;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.network.IPingPayload;
import dev.anvilcraft.ping.network.payload.EntityPingPayload;
import dev.anvilcraft.ping.network.payload.PositionPingPayload;
import dev.anvilcraft.ping.util.Ping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

@Mod(value = MiddleKeyPing.MOD_ID, dist = Dist.CLIENT)
public class MiddleKeyPingClient {
    public static final Collection<Ping> PINGS = Collections.synchronizedList(new LinkedList<>());
    private static final Map<UUID, Entity> ENTITY_CACHE = Collections.synchronizedMap(new HashMap<>());
    public static final MiddleKeyPingConfig CONFIG = ConfigManager.register(MiddleKeyPing.MOD_ID, MiddleKeyPingConfig::new);

    public MiddleKeyPingClient() {
    }

    public static void handle(PositionPingPayload payload) {
        MiddleKeyPingClient.handle(payload, Ping::new);
    }


    public static void handle(EntityPingPayload payload) {
        MiddleKeyPingClient.handle(payload, Ping::new);
    }

    public static <T extends IPingPayload> void handle(T payload, BiFunction<T, Long, Ping> pingFactory) {
        final Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;
        long gameTime = client.level.getGameTime();
        if (PINGS.size() > 5) PINGS.remove(PINGS.stream().findFirst().get());
        MiddleKeyPingClient.PINGS.add(pingFactory.apply(payload, gameTime + 300));
        SoundEvent event = switch (payload.pingType()) {
            case GOTO -> SoundEvents.NOTE_BLOCK_PLING.value();
            case WARNING -> SoundEvents.NOTE_BLOCK_CHIME.value();
            case ENEMY -> SoundEvents.NOTE_BLOCK_BIT.value();
            default -> SoundEvents.NOTE_BLOCK_BELL.value();
        };
        client.level.playLocalSound(client.player, event, SoundSource.PLAYERS, 1.0F, 1.0F);
        MutableComponent tip = Component.translatable(
                payload.pingType().description(),
                Component.translatable("chat.middle_key_ping.player", payload.sender())
            )
            .withColor(payload.pingType().color());
        client.getChatListener().handleSystemMessage(tip, false);
    }

    public static @Nullable Entity getEntity(UUID entity) {
        final Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            ENTITY_CACHE.clear();
            return null;
        }
        if (ENTITY_CACHE.containsKey(entity)) {
            Entity entity1 = ENTITY_CACHE.get(entity);
            if (!entity1.isAlive()) {
                ENTITY_CACHE.remove(entity);
                return null;
            }
            return entity1;
        }
        Iterable<Entity> entities = client.level.entitiesForRendering();
        for (Entity entity1 : entities) {
            if (!entity1.isAlive() || !entity1.getUUID().equals(entity)) continue;
            ENTITY_CACHE.put(entity1.getUUID(), entity1);
            return entity1;
        }
        return null;
    }
}
