package dev.anvilcraft.ping.data;

import dev.anvilcraft.lib.v2.config.ConfigData;
import dev.anvilcraft.ping.MiddleKeyPing;
import dev.anvilcraft.ping.client.MiddleKeyPingConfig;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MiddleKeyPingLanguageProvider extends LanguageProvider {
    public MiddleKeyPingLanguageProvider(PackOutput output) {
        super(output, MiddleKeyPing.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        ConfigData.readConfigClass(this, MiddleKeyPingConfig.class);

        this.add("key.middle_key_ping.uniform", "Uniform");
        this.add("key.middle_key_ping.generic", "Generic");
        this.add("key.middle_key_ping.warning", "Warning");
        this.add("key.middle_key_ping.goto", "Goto");
        this.add("key.middle_key_ping.enemy", "Enemy");
        this.add(MiddleKeyPing.of("middle_key_ping").toLanguageKey("key.category"), "Middle Key Ping");
        this.add("chat.middle_key_ping.player", "[%s]");
        this.add("chat.middle_key_ping.generic", "%s Marks a location.");
        this.add("chat.middle_key_ping.warning", "%s Be careful here.");
        this.add("chat.middle_key_ping.goto", "%s Go here.");
        this.add("chat.middle_key_ping.enemy", "%s There are enemy here.");
    }
}
