package dev.anvilcraft.ping.network;

import dev.anvilcraft.lib.v2.network.register.NetworkRegistrar;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.mixin.ChunkMapAccessor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.BiPredicate;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID)
public class ModNetworks {
    public static BiPredicate<@Nullable ServerPlayer, ServerPlayer> senderReceiverPredicate = (_, _) -> true;

    @SubscribeEvent
    public static void init(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        NetworkRegistrar.register(registrar, MiddleKeyPing.MOD_ID);
    }

    public static void sendToPlayersTrackingEntityAndSelf(
        @Nullable ServerPlayer sender,
        Entity entity,
        CustomPacketPayload payload,
        CustomPacketPayload... payloads
    ) {
        if (entity.level().isClientSide()) {
            throw new IllegalStateException("Cannot send clientbound payloads on the client");
        } else if (entity.level().getChunkSource() instanceof ServerChunkCache chunkCache) {
            ChunkMap.TrackedEntity trackedEntity = ((ChunkMapAccessor) chunkCache.chunkMap).getEntityMap().get(entity.getId());
            for (ServerPlayerConnection connection : trackedEntity.seenBy) {
                ServerPlayer player = connection.getPlayer();
                if (!senderReceiverPredicate.test(sender, player)) continue;
                PacketDistributor.sendToPlayer(player, payload, payloads);
            }
            if (trackedEntity.entity instanceof ServerPlayer player) {
                if (!senderReceiverPredicate.test(sender, player)) return;
                PacketDistributor.sendToPlayer(player, payload, payloads);
            }
        }
    }

    public static void sendToPlayersInDimension(
        @Nullable ServerPlayer sender,
        ServerLevel level,
        CustomPacketPayload payload,
        CustomPacketPayload... payloads
    ) {
        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
        for (ServerPlayer player : players) {
            if (player.level().dimension() != level.dimension()) {
                continue;
            }
            if (!senderReceiverPredicate.test(sender, player)) continue;
            PacketDistributor.sendToPlayer(player, payload, payloads);
        }
    }
}
