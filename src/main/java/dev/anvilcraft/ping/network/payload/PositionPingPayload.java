package dev.anvilcraft.ping.network.payload;

import dev.anvilcraft.lib.v2.codec.StreamCodecUtil;
import dev.anvilcraft.lib.v2.network.packet.ISensitiveBiPacket;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.network.IPingPayload;
import dev.anvilcraft.ping.network.ModNetworks;
import dev.anvilcraft.ping.util.PingType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public record PositionPingPayload(Component sender, Vec3 position, PingType pingType) implements IPingPayload, ISensitiveBiPacket {
    public static final Type<PositionPingPayload> TYPE = new Type<>(MiddleKeyPing.of("position_inspection_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PositionPingPayload> STREAM_CODEC = StreamCodec.composite(
        ComponentSerialization.STREAM_CODEC,
        PositionPingPayload::sender,
        StreamCodecUtil.VEC3,
        PositionPingPayload::position,
        StreamCodecUtil.enumStreamCodec(PingType.class),
        PositionPingPayload::pingType,
        PositionPingPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PositionPingPayload.TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        MiddleKeyPingClient.handle(this);
    }

    @Override
    public void handleOnServer(Player player) {
        final ServerPlayer serverPlayer = (ServerPlayer) player;
        ModNetworks.sendToPlayersInDimension(
            serverPlayer,
            serverPlayer.level(),
            new PositionPingPayload(player.getName(), this.position(), this.pingType())
        );
    }
}
