package dev.anvilcraft.ping.wheel;

import dev.anvilcraft.lib.v2.wheel.api.WheelActionContext;
import dev.anvilcraft.lib.v2.wheel.api.WheelOpenMode;
import dev.anvilcraft.ping.util.PingType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WheelMenuTest {
    @Test
    void everyEntryUsesTheProvidedTargetActionInMiddleKeyPingOrder() {
        List<PingType> selected = new ArrayList<>();
        var model = WheelMenu.create(selected::add);
        for (int index = 0; index < model.rootEntries().size(); index++) {
            var entry = model.rootEntries().get(index);
            entry.action().trigger(new WheelActionContext(0, index, entry.id(), WheelOpenMode.HOLD));
        }
        assertEquals(List.of(PingType.GENERIC, PingType.WARNING, PingType.GOTO, PingType.ENEMY), selected);
    }
}
