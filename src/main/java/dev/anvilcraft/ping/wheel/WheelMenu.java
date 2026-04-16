package dev.anvilcraft.ping.wheel;

import dev.anvilcraft.lib.v2.wheel.api.WheelMenuBuilder;
import dev.anvilcraft.lib.v2.wheel.api.WheelMenuModel;
import dev.anvilcraft.ping.util.PingUtil;
import dev.anvilcraft.ping.util.PingType;
import net.minecraft.network.chat.Component;

public class WheelMenu {
    public static final WheelMenuModel PING_WHEEL = WheelMenuBuilder.create()
        .slotsPerPage(4)
        .action("generic", Component.translatable("key.middle_key_ping.generic"), ctx -> PingUtil.sendPing(PingType.GENERIC))
        .action("warning", Component.translatable("key.middle_key_ping.warning"), ctx -> PingUtil.sendPing(PingType.WARNING))
        .action("goto", Component.translatable("key.middle_key_ping.goto"), ctx -> PingUtil.sendPing(PingType.GOTO))
        .action("enemy", Component.translatable("key.middle_key_ping.enemy"), ctx -> PingUtil.sendPing(PingType.ENEMY))
        .build();
}
