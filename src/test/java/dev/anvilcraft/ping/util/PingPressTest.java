package dev.anvilcraft.ping.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PingPressTest {
    @Test
    void shortPressCreatesOnePingAndUnmatchedReleaseCreatesNone() {
        PingPress press = new PingPress();
        assertEquals(PingPress.Release.NONE, press.release(false));
        press.start(0);
        assertFalse(press.openWheel(4, false));
        assertEquals(PingPress.Release.PING, press.release(false));
        assertEquals(PingPress.Release.NONE, press.release(false));
    }

    @Test
    void holdOpensWheelOnceAndReleaseSelectsIt() {
        PingPress press = new PingPress();
        press.start(0);
        assertTrue(press.openWheel(5, false));
        assertFalse(press.openWheel(6, false));
        assertEquals(PingPress.Release.WHEEL, press.release(false));
    }

    @Test
    void switchingToMapCancelsPendingWorldPress() {
        PingPress press = new PingPress();
        press.start(100);
        assertFalse(press.openWheel(105, true));
        assertFalse(press.openWheel(106, false));
        assertEquals(PingPress.Release.NONE, press.release(false));
        press.start(200);
        assertEquals(PingPress.Release.NONE, press.release(true));
    }

    @Test
    void replacingAnOpenWheelCancelsItsRelease() {
        PingPress press = new PingPress();
        press.start(100);
        assertTrue(press.openWheel(105, false));
        assertFalse(press.openWheel(106, true));
        assertEquals(PingPress.Release.NONE, press.release(false));
        press.start(200);
        assertEquals(PingPress.Release.PING, press.release(false));
    }
}
