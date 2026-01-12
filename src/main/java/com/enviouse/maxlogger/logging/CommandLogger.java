package com.enviouse.maxlogger.logging;

import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.data.WhitelistManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CommandLogger {
    private final LogWriter writer;
    private final WhitelistManager whitelist;
    private final SpyManager spyManager;
    private final Map<UUID, Integer> dedupTicks = new ConcurrentHashMap<>();
    private final AtomicLong commandsLogged = new AtomicLong();
    private final AtomicLong failedCommands = new AtomicLong();
    private final LocalDateTime sessionStart = LocalDateTime.now();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int cleanupTicker = 0;

    public CommandLogger(LogWriter writer, WhitelistManager whitelist, SpyManager spyManager) {
        this.writer = writer;
        this.whitelist = whitelist;
        this.spyManager = spyManager;
    }

    public void tick() {
        cleanupTicker++;
        // only clean every 20 ticks (1s) and fully purge every 60s
        if (cleanupTicker % 20 != 0) return;
        dedupTicks.replaceAll((id, ticks) -> ticks - 20);
        if (cleanupTicker % 1200 == 0) {
            dedupTicks.entrySet().removeIf(e -> e.getValue() <= 0);
            cleanupTicker = 0;
        }
    }

    public void logCommand(ServerPlayer player, String command, boolean success) {
        if (!ConfigHolder.COMMON.loggerEnabled.get() || !ConfigHolder.COMMON.logCommands.get()) return;
        if (!success && !ConfigHolder.COMMON.logFailedCommands.get()) return;
        if (whitelist.contains(player.getUUID())) return;
        int cooldown = dedupTicks.getOrDefault(player.getUUID(), 0);
        if (cooldown > 0) return;
        dedupTicks.put(player.getUUID(), 2);
        String line = format(player, command, success);
        writer.log(line);
        commandsLogged.incrementAndGet();
        if (!success) failedCommands.incrementAndGet();
        broadcastSpy(player, command);
    }

    public void logJoinLeave(ServerPlayer player, boolean join) {
        if (!ConfigHolder.COMMON.loggerEnabled.get() || !ConfigHolder.COMMON.logJoinLeave.get()) return;
        String line = (join ? "JOIN " : "LEAVE ") + player.getGameProfile().getName() + " (" + player.getUUID() + ")";
        writer.log(line);
    }

    private String format(ServerPlayer player, String command, boolean success) {
        Vec3 pos = player.position();
        String dim = player.level().dimension().location().toString();
        return player.getGameProfile().getName() + " (" + player.getUUID() + ") at (" + pos.x + ", " + pos.y + ", " + pos.z + ") in " + dim + ": /" + command + (success ? "" : " [failed]");
    }

    private void broadcastSpy(ServerPlayer sender, String command) {
        MinecraftServer server = sender.server;
        if (server == null) return;
        server.getPlayerList().getPlayers().stream()
                .filter(p -> spyManager.isSpying(p.getUUID()))
                .forEach(p -> p.sendSystemMessage(net.minecraft.network.chat.Component.literal("[Spy] " + sender.getGameProfile().getName() + ": /" + command)));
    }

    public String statsSummary() {
        return "Commands logged: " + commandsLogged.get() + ", failed: " + failedCommands.get();
    }

    public String sessionSummary() {
        return "Session since " + fmt.format(sessionStart) + ", file: " + writer.getSessionFileName();
    }
}
