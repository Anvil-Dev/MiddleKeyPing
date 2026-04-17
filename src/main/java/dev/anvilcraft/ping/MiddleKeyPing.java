package dev.anvilcraft.ping;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(MiddleKeyPing.MOD_ID)
public class MiddleKeyPing {
    public static final String MOD_ID = "middle_key_ping";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MiddleKeyPing(IEventBus modEventBus, ModContainer modContainer) {
    }

    public static Identifier of(String path) {
        return Identifier.fromNamespaceAndPath(MiddleKeyPing.MOD_ID, path);
    }
}
