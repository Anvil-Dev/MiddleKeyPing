package dev.anvilcraft.ping.client;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.util.Ping;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = MiddleKeyPing.MOD_ID, dist = Dist.CLIENT)
public class MiddleKeyPingClient {
    public static final Multimap<ResourceKey<Level>, Ping> PINGS = MultimapBuilder.hashKeys().arrayListValues().build();

    public MiddleKeyPingClient() {
    }
}
