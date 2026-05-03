package dev.anvilcraft.ping.integration;

import dev.anvilcraft.lib.v2.integration.Integration;
import dev.anvilcraft.ping.network.ModNetworks;
import dev.dubhe.bonded.team.Team;
import dev.dubhe.bonded.team.TeamManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@Integration("bonded_peaks")
public class BondedPeaksIntegration {
    public void apply() {
        ModNetworks.senderReceiverPredicate = BondedPeaksIntegration::testPlayer;
    }

    public static boolean testPlayer(@Nullable ServerPlayer sender, ServerPlayer receiver) {
        if (sender == null) return true;
        if (Objects.equals(sender, receiver)) return true;
        MinecraftServer server = sender.level().getServer();
        TeamManager teamManager = TeamManager.get(server);
        Optional<Team> senderTeam = teamManager.getTeamFor(sender.getUUID());
        if (senderTeam.isEmpty()) return false;
        Optional<Team> receiverTeam = teamManager.getTeamFor(receiver.getUUID());
        return receiverTeam.filter(team -> Objects.equals(senderTeam.get(), team)).isPresent();
    }
}
