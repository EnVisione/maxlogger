package com.enviouse.maxlogger.enforcement;

import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.data.WhitelistManager;
import com.enviouse.maxlogger.logging.LogWriter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import java.util.UUID;

public class GamemodeEnforcer {
    private final LogWriter writer;
    private final WhitelistManager whitelist;

    private long totalEnforcements = 0;

    public GamemodeEnforcer(LogWriter writer, WhitelistManager whitelist) {
        this.writer = writer;
        this.whitelist = whitelist;
    }

    public boolean enforce(ServerPlayer player, String reason) {
        if (isExempt(player)) return false;
        GameType current = player.gameMode.getGameModeForPlayer();
        if (current == GameType.SURVIVAL) return false;
        player.setGameMode(GameType.SURVIVAL);
        totalEnforcements++;
        String line = formatLine(player, current, reason);
        writer.log(line);
        notifyOps(player, line);
        return true;
    }

    public long getTotalEnforcements() {
        return totalEnforcements;
    }

    private boolean isExempt(ServerPlayer player) {
        String owner = ConfigHolder.COMMON.owner.get();
        if (owner != null && owner.equalsIgnoreCase(player.getGameProfile().getName())) return true;
        UUID id = player.getUUID();
        return whitelist.contains(id);
    }

    private void notifyOps(ServerPlayer player, String line) {
        if (!ConfigHolder.COMMON.enforcerNotifyOps.get()) return;
        if (player.server == null) return;
        player.server.getPlayerList().getPlayers().stream()
                .filter(p -> p.hasPermissions(2))
                .forEach(p -> p.sendSystemMessage(Component.literal("[Enforcer] " + line)));
    }

    private String formatLine(ServerPlayer player, GameType from, String reason) {
        String dim = player.level().dimension().location().toString();
        return player.getGameProfile().getName() + " (" + player.getUUID() + ") changed from " + from.getName() + " -> survival [" + reason + "] at "
                + coords(player) + " in " + dim;
    }

    private String coords(ServerPlayer player) {
        return "(" + player.getX() + ", " + player.getY() + ", " + player.getZ() + ")";
    }
}

