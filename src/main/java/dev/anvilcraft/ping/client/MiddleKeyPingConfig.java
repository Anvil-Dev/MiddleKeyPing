package dev.anvilcraft.ping.client;

import dev.anvilcraft.lib.v2.config.BoundedDiscrete;
import dev.anvilcraft.lib.v2.config.Comment;
import dev.anvilcraft.lib.v2.config.Config;
import dev.anvilcraft.ping.MiddleKeyPing;
import lombok.Getter;
import net.neoforged.fml.config.ModConfig;

@Getter
@Config(name = MiddleKeyPing.MOD_ID, type = ModConfig.Type.CLIENT)
public class MiddleKeyPingConfig {
    public String genericColor = "#999999FF";
    public String genericTextColor = "#DDDDDDFF";
    public String warningColor = "#FFFF00FF";
    public String warningTextColor = "#0000FFFF";
    public String gotoColor = "#00FF00FF";
    public String gotoTextColor = "#FF00FFFF";
    public String enemyColor = "#FF0000FF";
    public String enemyTextColor = "#00FFFFFF";

    @Comment("If set to false, points that are beyond the specified distance or are not selected will not be marked")
    public boolean allowPingEmpty = true;

    @Comment("The maximum distance allowed for the creation of punctuation marks")
    @BoundedDiscrete(min = 16, max = 1024)
    public int maxPingDistance = 200;
}
