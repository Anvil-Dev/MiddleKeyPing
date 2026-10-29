package dev.anvilcraft.ping.wheel;

import dev.anvilcraft.lib.v2.wheel.api.WheelMenuBuilder;
import dev.anvilcraft.lib.v2.wheel.api.WheelMenuModel;
import dev.anvilcraft.ping.util.PingUtil;
import dev.anvilcraft.ping.util.PingType;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class WheelMenu {
    public static final WheelMenuModel PING_WHEEL = create(PingUtil::sendPing);

    /// 按相同的类型、顺序与文案创建轮盘，由 action 决定标点的目标位置和发送方式。
    public static WheelMenuModel create(Consumer<PingType> action) {
        return WheelMenuBuilder.create()
        .slotsPerPage(4)
        .action("generic", Component.translatable("key.middle_key_ping.generic"), ctx -> action.accept(PingType.GENERIC))
        .action("warning", Component.translatable("key.middle_key_ping.warning"), ctx -> action.accept(PingType.WARNING))
        .action("goto", Component.translatable("key.middle_key_ping.goto"), ctx -> action.accept(PingType.GOTO))
        .action("enemy", Component.translatable("key.middle_key_ping.enemy"), ctx -> action.accept(PingType.ENEMY))
        .build();
    }
}
