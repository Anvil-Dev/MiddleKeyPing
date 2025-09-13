package dev.anvilcraft.ping.util;

public enum PingType {
    GENRIC(0xFF999999), WARNING(0xFFFFFF00), GOTO(0xFF00FF00), ENEMY(0xFFFF0000);

    private final int color;

    PingType(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }

    public String description() {
        return "chat.middle_key_ping." + name().toLowerCase();
    }
}
