package dev.anvilcraft.ping.data;

import dev.anvilcraft.ping.MiddleKeyPing;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MiddleKeyPing.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MiddleKeyPingDatagen {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        generator.addProvider(event.includeServer(), new MiddleKeyPingLanguageProvider(packOutput));
    }
}
