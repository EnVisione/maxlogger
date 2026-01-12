package com.enviouse.maxlogger.logging;

import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.data.WhitelistManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CommandLogger {
    private record CommandEntry(UUID id, String name, String dimension, Vec3 pos, String command, boolean success, LocalDateTime time, boolean isConsole) {}

    private final LogWriter writer;
    private final WhitelistManager whitelist;
    private final SpyManager spyManager;
    private final Map<UUID, Integer> dedupTicks = new ConcurrentHashMap<>();
    private final AtomicLong commandsLogged = new AtomicLong();
    private final AtomicLong failedCommands = new AtomicLong();
    private final LocalDateTime sessionStart = LocalDateTime.now();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int cleanupTicker = 0;
    private final ArrayDeque<CommandEntry> history = new ArrayDeque<>();
    private final Set<String> spyFilter = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Vec3> lastLoginPos = new ConcurrentHashMap<>();
    private final Map<UUID, Vec3> lastLogoutPos = new ConcurrentHashMap<>();

    public CommandLogger(LogWriter writer, WhitelistManager whitelist, SpyManager spyManager) {
        this.writer = writer;
        this.whitelist = whitelist;
        this.spyManager = spyManager;
    }

    public void tick() {
        cleanupTicker++;
        if (cleanupTicker % 20 != 0) return;
        dedupTicks.replaceAll((id, ticks) -> ticks - 20);
        if (cleanupTicker % 1200 == 0) {
            dedupTicks.entrySet().removeIf(e -> e.getValue() <= 0);
            cleanupTicker = 0;
        }
    }

    public void logCommand(CommandSourceStack source, String command, boolean success) {
        if (!ConfigHolder.COMMON.loggerEnabled.get() || !ConfigHolder.COMMON.logCommands.get()) return;
        boolean isPlayer = source.getEntity() instanceof ServerPlayer;
        UUID id = isPlayer ? ((ServerPlayer) source.getEntity()).getUUID() : UUID.nameUUIDFromBytes("console".getBytes());
        String name = isPlayer ? ((ServerPlayer) source.getEntity()).getGameProfile().getName() : "CONSOLE";
        if (isPlayer && whitelist.contains(id)) return;
        int cooldown = dedupTicks.getOrDefault(id, 0);
        if (cooldown > 0) return;
        dedupTicks.put(id, 2);

        Vec3 pos = isPlayer ? ((ServerPlayer) source.getEntity()).position() : source.getPosition();
        String dim = source.getLevel().dimension().location().toString();
        String line = format(name, id, pos, dim, command, success, isPlayer ? lastLoginPos.get(id) : null, isPlayer ? lastLogoutPos.get(id) : null, !isPlayer);
        writer.log(line);
        writer.flushLatest();
        commandsLogged.incrementAndGet();
        if (!success) failedCommands.incrementAndGet();
        historyAdd(new CommandEntry(id, name, dim, pos, command, success, LocalDateTime.now(), !isPlayer));
        broadcastSpy(source.getServer(), name, command, isPlayer ? id : null);
    }

    public void logJoin(ServerPlayer player) {
        lastLoginPos.put(player.getUUID(), player.position());
        if (!ConfigHolder.COMMON.loggerEnabled.get() || !ConfigHolder.COMMON.logJoinLeave.get()) return;
        writer.log("JOIN " + player.getGameProfile().getName() + " (" + player.getUUID() + ") at " + coords(player.position()));
        writer.flushLatest();
    }

    public void logLeave(ServerPlayer player) {
        lastLogoutPos.put(player.getUUID(), player.position());
        if (!ConfigHolder.COMMON.loggerEnabled.get() || !ConfigHolder.COMMON.logJoinLeave.get()) return;
        writer.log("LEAVE " + player.getGameProfile().getName() + " (" + player.getUUID() + ") at " + coords(player.position()));
        writer.flushLatest();
    }

    private void historyAdd(CommandEntry entry) {
        synchronized (history) {
            history.addLast(entry);
            int max = ConfigHolder.COMMON.commandHistorySize.get();
            while (history.size() > max) {
                history.removeFirst();
            }
        }
    }

    public List<String> viewPlayer(UUID id) {
        List<CommandEntry> snapshot;
        synchronized (history) {
            snapshot = new ArrayList<>(history);
        }
        return snapshot.stream()
                .filter(e -> e.id.equals(id))
                .map(this::entryToString)
                .collect(Collectors.toList());
    }

    public List<String> searchCommand(String needle) {
        String lower = needle.toLowerCase();
        List<CommandEntry> snapshot;
        synchronized (history) {
            snapshot = new ArrayList<>(history);
        }
        return snapshot.stream()
                .filter(e -> e.command.toLowerCase().contains(lower))
                .map(this::entryToString)
                .collect(Collectors.toList());
    }

    public boolean spyFilterAdd(String term) {
        return spyFilter.add(cleanTerm(term));
    }

    public boolean spyFilterRemove(String term) {
        return spyFilter.remove(cleanTerm(term));
    }

    public Set<String> spyFilterList() {
        return Set.copyOf(spyFilter);
    }

    private String entryToString(CommandEntry e) {
        return fmt.format(e.time) + " " + e.name + " (" + e.id + ") at " + coords(e.pos) + " in " + e.dimension + ": /" + e.command + (e.success ? "" : " [failed]") + (e.isConsole ? " [console]" : "");
    }

    private void broadcastSpy(MinecraftServer server, String name, String command, UUID playerId) {
        if (server == null) return;
        String normalized = cleanTerm(command);
        if (spyFilter.stream().anyMatch(normalized::startsWith)) return;
        Component msg = Component.literal("[Spy] " + name + ": /" + command);
        server.getPlayerList().getPlayers().stream()
                .filter(p -> spyManager.isSpying(p.getUUID()))
                .forEach(p -> p.sendSystemMessage(msg));
    }

    private String cleanTerm(String term) {
        String t = term == null ? "" : term.trim();
        if (t.startsWith("/")) t = t.substring(1);
        return t.toLowerCase();
    }

    private String format(String name, UUID id, Vec3 pos, String dim, String command, boolean success, Vec3 lastLogin, Vec3 lastLogout, boolean isConsole) {
        String extra = "";
        if (lastLogin != null) extra += " lastLogin=" + coords(lastLogin);
        if (lastLogout != null) extra += " lastLogout=" + coords(lastLogout);
        if (isConsole) extra += " [console]";
        return name + " (" + id + ") at " + coords(pos) + " in " + dim + ": /" + command + (success ? "" : " [failed]") + extra;
    }

    private String coords(Vec3 pos) {
        return "(" + pos.x + ", " + pos.y + ", " + pos.z + ")";
    }

    public String statsSummary() {
        return "Commands logged: " + commandsLogged.get() + ", failed: " + failedCommands.get();
    }

    public String sessionSummary() {
        return "Session since " + fmt.format(sessionStart) + ", file: " + writer.getSessionFileName();
    }
}
