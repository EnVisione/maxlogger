package com.enviouse.maxlogger.events;

import com.enviouse.maxlogger.logging.CommandLogger;
import net.minecraft.commands.CommandSourceStack;
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
        Object sourceObj = event.getParseResults().getContext().getSource();
        if (!(sourceObj instanceof CommandSourceStack)) return;
        CommandSourceStack source = (CommandSourceStack) sourceObj;
        String raw = event.getParseResults().getReader().getString();
        boolean success = event.getException() == null;
        logger.logCommand(source, raw.startsWith("/") ? raw.substring(1) : raw, success);
    }
}
