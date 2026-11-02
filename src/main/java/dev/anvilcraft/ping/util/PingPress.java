package dev.anvilcraft.ping.util;

final class PingPress {
    enum Release {
        NONE,
        PING,
        WHEEL
    }

    private long started = -1;
    private boolean wheelOpened;

    void start(long gameTime) {
        if (this.started < 0) {
            this.started = gameTime;
        }
    }

    boolean openWheel(long gameTime, boolean blocked) {
        if (blocked) {
            this.cancel();
            return false;
        }
        if (this.started >= 0 && !this.wheelOpened && gameTime - this.started > 4) {
            this.wheelOpened = true;
            return true;
        }
        return false;
    }

    Release release(boolean blocked) {
        Release action = this.started < 0 || blocked ? Release.NONE
                : this.wheelOpened ? Release.WHEEL : Release.PING;
        this.cancel();
        return action;
    }

    void cancel() {
        this.started = -1;
        this.wheelOpened = false;
    }
}
