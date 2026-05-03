package dev.anvilcraft.ping.network.payload;

import dev.anvilcraft.lib.v2.codec.StreamCodecUtil;
import dev.anvilcraft.lib.v2.network.packet.ISensitiveBiPacket;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.network.IPingPayload;
import dev.anvilcraft.ping.network.ModNetworks;
import dev.anvilcraft.ping.util.PingType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public record EntityPingPayload(Component sender, UUID uuid, PingType pingType) implements IPingPayload, ISensitiveBiPacket {
    public static final Type<EntityPingPayload> TYPE = new Type<>(MiddleKeyPing.of("entity_inspection_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityPingPayload> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.STREAM_CODEC,
        EntityPingPayload::sender,
        UUIDUtil.STREAM_CODEC,
        EntityPingPayload::uuid,
        StreamCodecUtil.enumStreamCodec(PingType.class),
        EntityPingPayload::pingType,
        EntityPingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return EntityPingPayload.TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        MiddleKeyPingClient.handle(this);
    }

    @Override
    public void handleOnServer(Player player) {
        final ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = serverPlayer.level();
        Entity entity = level.getEntity(this.uuid());
        if (entity == null || !entity.isAlive()) return;
        ModNetworks.sendToPlayersTrackingEntityAndSelf(
            serverPlayer,
            entity,
            new EntityPingPayload(player.getName(), entity.getUUID(), this.pingType())
        );
    }
}
