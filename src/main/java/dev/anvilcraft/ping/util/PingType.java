package dev.anvilcraft.ping.util;

import dev.anvilcraft.ping.client.MiddleKeyPingClient;

import java.util.function.Supplier;

@SuppressWarnings("DuplicatedCode")
public enum PingType {
    UNIFORM(),
    GENERIC(),
    WARNING(),
    GOTO(),
    ENEMY();

    PingType() {
    }

    public int color() {
        return switch (this) {
            case GENERIC -> praseColor(MiddleKeyPingClient.CONFIG::getGenericColor, 0xFFDDDDDD);
            case WARNING -> praseColor(MiddleKeyPingClient.CONFIG::getWarningColor, 0xFFDDDDDD);
            case GOTO -> praseColor(MiddleKeyPingClient.CONFIG::getGotoColor, 0xFFDDDDDD);
            case ENEMY -> praseColor(MiddleKeyPingClient.CONFIG::getEnemyColor, 0xFFDDDDDD);
            default -> 0x00000000;
        };
    }

    public int textColor() {
        return switch (this) {
            case GENERIC -> praseColor(MiddleKeyPingClient.CONFIG::getGenericTextColor, 0xFFFFFFFF);
            case WARNING -> praseColor(MiddleKeyPingClient.CONFIG::getWarningTextColor, 0xFFFFFFFF);
            case GOTO -> praseColor(MiddleKeyPingClient.CONFIG::getGotoTextColor, 0xFFFFFFFF);
            case ENEMY -> praseColor(MiddleKeyPingClient.CONFIG::getEnemyTextColor, 0xFFFFFFFF);
            default -> 0x00000000;
        };
    }

    private int praseColor(Supplier<String> colorSupplier, int defaultColor) {
        try {
            String color = colorSupplier.get();
            if (color.startsWith("#")) {
                color = color.substring(1);
            }
            String rgb = color.substring(0, 6);
            String alpha = color.length() > 6 ? color.substring(6) : "FF";
            String argb = alpha + rgb;
            long colorLong = Long.parseLong(argb, 16);
            return (int) colorLong;
        } catch (Exception e) {
            return defaultColor;
        }
    }

    public String description() {
        return "chat.middle_key_ping." + name().toLowerCase();
    }
}
