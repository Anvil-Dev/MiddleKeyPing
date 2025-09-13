package dev.anvilcraft.ping.network;

import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.anvilcraft.ping.util.ByteBufCodecUtils;
import dev.anvilcraft.ping.util.Ping;
import dev.anvilcraft.ping.util.PingType;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Collection;
import java.util.List;

public record PingPayload(ResourceKey<Level> dimension, Vec3 position, PingType pingType) implements CustomPacketPayload {
    public static final Type<PingPayload> TYPE = new Type<>(MiddleKeyPing.of("inspection_state"));

    public static final StreamCodec<ByteBuf, PingPayload> STREAM_CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION),
        PingPayload::dimension,
        ByteBufCodecUtils.VEC3,
        PingPayload::position,
        ByteBufCodecUtils.enumCodec(PingType.class),
        PingPayload::pingType,
        PingPayload::new
    );

    public static final IPayloadHandler<PingPayload> HANDLER = new DirectionalPayloadHandler<>(
        PingPayload::clientHandler,
        PingPayload::serverHandler
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PingPayload.TYPE;
    }

    public void clientHandler(IPayloadContext context) {
        final Minecraft client = Minecraft.getInstance();
        context.enqueueWork(() -> {
            if (client.level == null) return;
            long gameTime = client.level.getGameTime();
            Collection<Ping> pings = MiddleKeyPingClient.PINGS.get(this.dimension);
            if (pings.size() > 5) pings.remove(pings.stream().findFirst().get());
            MiddleKeyPingClient.PINGS.put(this.dimension, new Ping(this, gameTime + 300));
            SoundEvent event = switch (this.pingType) {
                case GOTO -> SoundEvents.NOTE_BLOCK_PLING.value();
                case WARNING -> SoundEvents.NOTE_BLOCK_CHIME.value();
                case ENEMY -> SoundEvents.NOTE_BLOCK_BIT.value();
                default -> SoundEvents.NOTE_BLOCK_BELL.value();
            };
            //noinspection DataFlowIssue
            client.level.playLocalSound(client.player, event, SoundSource.PLAYERS, 1.0F, 1.0F);
        });
    }

    @SuppressWarnings("resource")
    public void serverHandler(IPayloadContext context) {
        final ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            MinecraftServer server = player.getServer();
            if (server == null) return;
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            for (ServerPlayer other : players) {
                PlayerTeam team = player.getTeam();
                PlayerTeam team1 = other.getTeam();
                if (team != null && !team.equals(team1)) return;
                ResourceKey<Level> dimension = player.level().dimension();
                ResourceKey<Level> dimension1 = other.level().dimension();
                if (!dimension.equals(dimension1)) return;
                other.connection.send(new PingPayload(this.dimension, this.position, this.pingType));
                other.sendSystemMessage(Component.translatable(
                    this.pingType.description(),
                    Component.translatable("chat.middle_key_ping.player", player.getDisplayName()).withStyle(ChatFormatting.WHITE)
                ).withColor(this.pingType.color()));
            }
        });
    }
}
