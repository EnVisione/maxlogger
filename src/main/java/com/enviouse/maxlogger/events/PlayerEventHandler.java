package com.enviouse.maxlogger.events;

import com.enviouse.maxlogger.MaxLogger;
import com.enviouse.maxlogger.enforcement.EnforcementScheduler;
import com.enviouse.maxlogger.logging.CommandLogger;
import com.enviouse.maxlogger.logging.SpyManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerEventHandler {
    private final EnforcementScheduler scheduler;
    private final CommandLogger commandLogger;
    private final SpyManager spyManager;

    public PlayerEventHandler(EnforcementScheduler scheduler, CommandLogger commandLogger) {
        this.scheduler = scheduler;
        this.commandLogger = commandLogger;
        this.spyManager = MaxLogger.get().getSpyManager();
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            scheduler.onJoin(player);
            spyManager.load(player);
            commandLogger.logJoin(player);
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            scheduler.onLeave(player);
            commandLogger.logLeave(player);
        }
    }
}
