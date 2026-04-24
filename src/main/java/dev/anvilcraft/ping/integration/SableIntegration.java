package dev.anvilcraft.ping.integration;

import dev.anvilcraft.lib.v2.integration.Integration;
import dev.anvilcraft.lib.v2.integration.IntegrationType;
import dev.anvilcraft.ping.client.MiddleKeyPingClient;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Slf4j
@Integration(value = "sable", type = IntegrationType.CLIENT)
public class SableIntegration {
    public void applyClient() {
        MiddleKeyPingClient.positionConverter = SableIntegration::convertPosition;
        log.info("Initialized Sable integration for Middle Key Ping");
    }

    public static Vec3 convertPosition(Level level, Vec3 pos) {
        BlockPos containing = BlockPos.containing(pos);
        final SubLevel subLevel = Sable.HELPER.getContaining(level, containing);
        if (!(subLevel instanceof ClientSubLevel clientSubLevel)) return pos;
        return clientSubLevel.logicalPose().transformPosition(pos);
    }
}
