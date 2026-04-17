package dev.anvilcraft.ping;

import com.mojang.logging.LogUtils;
import dev.anvilcraft.ping.util.PingUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MiddleKeyPing.MOD_ID)
public class MiddleKeyPing {
    public static final String MOD_ID = "middle_key_ping";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MiddleKeyPing(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(PingUtil::registerKeyMapping);
    }

    public static ResourceLocation of(String path) {
        return ResourceLocation.fromNamespaceAndPath(MiddleKeyPing.MOD_ID, path);
    }
}
