package com.enviouse.maxlogger.commands;

import com.enviouse.maxlogger.MaxLogger;
import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.enforcement.EnforcementScheduler;
import com.enviouse.maxlogger.data.WhitelistManager;
import com.enviouse.maxlogger.util.PlayerResolver;
import com.enviouse.maxlogger.util.TextUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class EnforcerCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("enforcer")
                .requires(src -> src.hasPermission(2) || PlayerResolver.isOwner(src, com.enviouse.maxlogger.config.ConfigHolder.COMMON.owner.get()))
                .then(Commands.literal("add").then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> add(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("remove").then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> remove(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
                .then(Commands.literal("clear").executes(ctx -> clear(ctx.getSource())))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource())))
                .then(Commands.literal("stats").executes(ctx -> stats(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("check").then(Commands.argument("player", StringArgumentType.word()).executes(ctx -> check(ctx.getSource(), StringArgumentType.getString(ctx, "player")))))
        );
    }

    private static int add(CommandSourceStack src, String input) {
        WhitelistManager wl = MaxLogger.get().getGamemodeWhitelist();
        return PlayerResolver.resolve(src, input).map(resolved -> {
            boolean added = wl.add(resolved.id(), resolved.name());
            src.sendSuccess(() -> TextUtils.success((added ? "Added " : "Already present ") + resolved.name() + " (" + resolved.id() + ")"), true);
            return 1;
        }).orElseGet(() -> {
            src.sendFailure(net.minecraft.network.chat.Component.literal("Player not found"));
            return 0;
        });
    }

    private static int remove(CommandSourceStack src, String input) {
        WhitelistManager wl = MaxLogger.get().getGamemodeWhitelist();
        return PlayerResolver.resolve(src, input).map(resolved -> {
            boolean removed = wl.remove(resolved.id());
            src.sendSuccess(() -> TextUtils.success((removed ? "Removed " : "Not present ") + resolved.name() + " (" + resolved.id() + ")"), true);
            return 1;
        }).orElseGet(() -> {
            src.sendFailure(net.minecraft.network.chat.Component.literal("Player not found"));
            return 0;
        });
    }

    private static int clear(CommandSourceStack src) {
        MaxLogger.get().getGamemodeWhitelist().clear();
        src.sendSuccess(() -> TextUtils.success("Cleared enforcer whitelist"), true);
        return 1;
    }

    private static int list(CommandSourceStack src) {
        WhitelistManager wl = MaxLogger.get().getGamemodeWhitelist();
        wl.view().forEach((id, name) -> src.sendSuccess(() -> TextUtils.info(name + " (" + id + ")"), false));
        return wl.view().size();
    }

    private static int stats(CommandSourceStack src) {
        EnforcementScheduler scheduler = MaxLogger.get().getEnforcementScheduler();
        int size = MaxLogger.get().getGamemodeWhitelist().view().size();
        src.sendSuccess(() -> TextUtils.info("Total enforcements: " + scheduler.getTotalEnforcements()
                + ", interval: " + ConfigHolder.COMMON.enforcerIntervalTicks.get() + " ticks"
                + ", whitelist size: " + size), false);
        return 1;
    }

    private static int help(CommandSourceStack src) {
        src.sendSuccess(() -> TextUtils.info("/enforcer add|remove|list|clear|stats|check <player>"), false);
        return 1;
    }

    private static int check(CommandSourceStack src, String input) {
        WhitelistManager wl = MaxLogger.get().getGamemodeWhitelist();
        return PlayerResolver.resolve(src, input).map(resolved -> {
            boolean exempt = wl.contains(resolved.id()) || (ConfigHolder.COMMON.owner.get() != null && ConfigHolder.COMMON.owner.get().equalsIgnoreCase(resolved.name()));
            src.sendSuccess(() -> TextUtils.info(resolved.name() + " exempt=" + exempt), false);
            return 1;
        }).orElseGet(() -> {
            src.sendFailure(TextUtils.error("Player not found"));
            return 0;
        });
    }
}
