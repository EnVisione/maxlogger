package com.enviouse.maxlogger.enforcement;

import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.data.WhitelistManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class EnforcementScheduler {
    private final GamemodeEnforcer enforcer;
    private final Map<UUID, Integer> cooldownTicks = new HashMap<>();

    public EnforcementScheduler(com.enviouse.maxlogger.logging.LogWriter writer, WhitelistManager whitelist) {
        this.enforcer = new GamemodeEnforcer(writer, whitelist);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ConfigHolder.COMMON.enforcerEnabled.get()) return;
        double mspt = event.getServer().getAverageTickTime();
        if (ConfigHolder.COMMON.enforcerPauseIfMsptAbove.get() > 0 && mspt > ConfigHolder.COMMON.enforcerPauseIfMsptAbove.get()) return;
        int interval = ConfigHolder.COMMON.enforcerIntervalTicks.get();
        if (interval <= 0) return;
        if (event.getServer().getTickCount() % interval != 0) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            int cd = cooldownTicks.getOrDefault(player.getUUID(), 0);
            if (cd > 0) {
                cooldownTicks.put(player.getUUID(), cd - interval);
                continue;
            }
            boolean enforced = enforcer.enforce(player, "periodic");
            if (enforced) {
                cooldownTicks.put(player.getUUID(), interval * 2);
            }
        }
    }

    public void onJoin(ServerPlayer player) {
        cooldownTicks.put(player.getUUID(), 10); // 10 ticks (~0.5s) delay
    }

    public void onLeave(ServerPlayer player) {
        cooldownTicks.remove(player.getUUID());
    }

    public void cleanup() {
        for (Iterator<Map.Entry<UUID, Integer>> it = cooldownTicks.entrySet().iterator(); it.hasNext();) {
            Map.Entry<UUID, Integer> e = it.next();
            if (e.getValue() <= 0) it.remove();
        }
    }

    public long getTotalEnforcements() {
        return enforcer.getTotalEnforcements();
    }

    public boolean enforceNow(ServerPlayer player, String reason) {
        return enforcer.enforce(player, reason);
    }
}
