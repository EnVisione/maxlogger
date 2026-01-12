package com.enviouse.maxlogger.events;

import com.enviouse.maxlogger.logging.CommandLogger;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CommandEventHandler {
    private final CommandLogger logger;

    public CommandEventHandler(CommandLogger logger) {
        this.logger = logger;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCommand(CommandEvent event) {
        Object source = event.getParseResults().getContext().getSource();
        if (!(source instanceof CommandSourceStack)) return;
        CommandSourceStack stack = (CommandSourceStack) source;
        if (!(stack.getEntity() instanceof ServerPlayer)) return;
        ServerPlayer player = (ServerPlayer) stack.getEntity();
        String raw = event.getParseResults().getReader().getString();
        boolean success = event.getException() == null;
        logger.logCommand(player, raw.startsWith("/") ? raw.substring(1) : raw, success);
    }
}
