package dev.anvilcraft.ping.network;

import dev.anvilcraft.ping.MiddleKeyPing;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID)
public class ModNetworks {
    @SubscribeEvent
    public static void init(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(
            PingPayload.TYPE,
            PingPayload.STREAM_CODEC,
            PingPayload.HANDLER
        );
    }
}
