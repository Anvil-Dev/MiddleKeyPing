package dev.anvilcraft.ping.client;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.network.PingPayload;
import dev.anvilcraft.ping.util.Ping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

import java.util.Collection;

@Mod(value = MiddleKeyPing.MOD_ID, dist = Dist.CLIENT)
public class MiddleKeyPingClient {
    public static final Multimap<ResourceKey<Level>, Ping> PINGS = MultimapBuilder.hashKeys().arrayListValues().build();

    public MiddleKeyPingClient() {
    }

    public static void handle(PingPayload payload){
        final Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        long gameTime = client.level.getGameTime();
        Collection<Ping> pings = MiddleKeyPingClient.PINGS.get(payload.dimension());
        if (pings.size() > 5) pings.remove(pings.stream().findFirst().get());
        MiddleKeyPingClient.PINGS.put(payload.dimension(), new Ping(payload, gameTime + 300));
        SoundEvent event = switch (payload.pingType()) {
            case GOTO -> SoundEvents.NOTE_BLOCK_PLING.value();
            case WARNING -> SoundEvents.NOTE_BLOCK_CHIME.value();
            case ENEMY -> SoundEvents.NOTE_BLOCK_BIT.value();
            default -> SoundEvents.NOTE_BLOCK_BELL.value();
        };
        //noinspection DataFlowIssue
        client.level.playLocalSound(client.player, event, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
