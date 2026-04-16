package dev.anvilcraft.ping.network;

import dev.anvilcraft.ping.util.PingType;
import net.minecraft.network.chat.Component;

public interface IPingPayload {
    Component sender();

    PingType pingType();
}
