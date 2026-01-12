package com.enviouse.maxlogger.commands;

import com.enviouse.maxlogger.MaxLogger;
import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.data.WhitelistManager;
import com.enviouse.maxlogger.logging.CommandLogger;
import com.enviouse.maxlogger.util.PlayerResolver;
import com.enviouse.maxlogger.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class LoggerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("logger")
                .requires(src -> src.hasPermission(2) || PlayerResolver.isOwner(src, ConfigHolder.COMMON.owner.get()))
                .then(Commands.literal("spy").executes(ctx -> toggleSpy(ctx.getSource())))
                .then(Commands.literal("whitelist")
                        .then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> add(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                        .then(Commands.literal("remove").then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                        .then(Commands.literal("list").executes(ctx -> list(ctx.getSource()))))
                .then(Commands.literal("stats").executes(ctx -> stats(ctx.getSource())))
                .then(Commands.literal("session").executes(ctx -> session(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
        );
    }

    private static int toggleSpy(CommandSourceStack src) {
        if (!(src.getEntity() instanceof ServerPlayer player)) {
            src.sendFailure(net.minecraft.network.chat.Component.literal("Players only"));
            return 0;
        }
        var spyManager = MaxLogger.get().getSpyManager();
        boolean now = spyManager.toggle(player);
        src.sendSuccess(() -> TextUtils.success("Spy mode " + (now ? "enabled" : "disabled")), false);
        return now ? 1 : 0;
    }

    private static int add(CommandSourceStack src, String input) {
        WhitelistManager wl = MaxLogger.get().getLoggerWhitelist();
        return PlayerResolver.resolve(src, input).map(resolved -> {
            boolean added = wl.add(resolved.id(), resolved.name());
            src.sendSuccess(() -> TextUtils.success((added ? "Added " : "Already present ") + resolved.name() + " (" + resolved.id() + ")"), true);
            return 1;
        }).orElseGet(() -> {
            src.sendFailure(TextUtils.error("Player not found"));
            return 0;
        });
    }

    private static int remove(CommandSourceStack src, String input) {
        WhitelistManager wl = MaxLogger.get().getLoggerWhitelist();
        return PlayerResolver.resolve(src, input).map(resolved -> {
            boolean removed = wl.remove(resolved.id());
            src.sendSuccess(() -> TextUtils.success((removed ? "Removed " : "Not present ") + resolved.name() + " (" + resolved.id() + ")"), true);
            return 1;
        }).orElseGet(() -> {
            src.sendFailure(TextUtils.error("Player not found"));
            return 0;
        });
    }

    private static int list(CommandSourceStack src) {
        WhitelistManager wl = MaxLogger.get().getLoggerWhitelist();
        wl.view().forEach((id, name) -> src.sendSuccess(() -> TextUtils.info(name + " (" + id + ")"), false));
        return wl.view().size();
    }

    private static int stats(CommandSourceStack src) {
        CommandLogger logger = MaxLogger.get().getCommandLogger();
        int size = MaxLogger.get().getLoggerWhitelist().view().size();
        src.sendSuccess(() -> TextUtils.info(logger.statsSummary() + ", whitelist size: " + size), false);
        return 1;
    }

    private static int session(CommandSourceStack src) {
        CommandLogger logger = MaxLogger.get().getCommandLogger();
        src.sendSuccess(() -> TextUtils.info(logger.sessionSummary()), false);
        return 1;
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> TextUtils.info("/logger spy | /logger whitelist add|remove|list | /logger stats | /logger session"), false);
        return 1;
    }
}
