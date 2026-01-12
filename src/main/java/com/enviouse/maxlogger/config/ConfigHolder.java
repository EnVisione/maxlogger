package com.enviouse.maxlogger.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public final class ConfigHolder {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static void onLoad(ModConfig config) {
        // Placeholder for potential reload handling.
    }

    public static final class Common {
        public final ForgeConfigSpec.ConfigValue<String> owner;
        public final ForgeConfigSpec.BooleanValue enforcerEnabled;
        public final ForgeConfigSpec.IntValue enforcerIntervalTicks;
        public final ForgeConfigSpec.BooleanValue enforcerNotifyOps;
        public final ForgeConfigSpec.DoubleValue enforcerPauseIfMsptAbove;
        public final ForgeConfigSpec.ConfigValue<String> logDirectory;
        public final ForgeConfigSpec.BooleanValue loggerEnabled;
        public final ForgeConfigSpec.BooleanValue logCommands;
        public final ForgeConfigSpec.BooleanValue logJoinLeave;
        public final ForgeConfigSpec.BooleanValue logFailedCommands;
        public final ForgeConfigSpec.LongValue logRotationBytes;
        public final ForgeConfigSpec.IntValue whitelistBackupKeep;
        public final ForgeConfigSpec.IntValue commandHistorySize;

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            owner = builder.comment("Owner username with full control").define("owner", "BreezilyOnMyMind");
            builder.pop();

            builder.push("enforcer");
            enforcerEnabled = builder.define("enabled", true);
            enforcerIntervalTicks = builder.defineInRange("checkIntervalTicks", 20, 1, 1200);
            enforcerNotifyOps = builder.define("notifyOps", true);
            enforcerPauseIfMsptAbove = builder.comment("Skip enforcement when MSPT is higher than this (server lag protection)").defineInRange("pauseIfMsptAbove", 55.0, 0.0, 200.0);
            logDirectory = builder.define("logDirectory", "anticheat/logs");
            builder.pop();

            builder.push("logger");
            loggerEnabled = builder.define("enabled", true);
            logCommands = builder.define("logCommands", true);
            logJoinLeave = builder.define("logJoinLeave", true);
            logFailedCommands = builder.define("logFailedCommands", true);
            logRotationBytes = builder.comment("Rotate log when reaching size in bytes").defineInRange("logRotationBytes", 5_000_000L, 100_000L, 100_000_000L);
            whitelistBackupKeep = builder.comment("How many whitelist backups to keep").defineInRange("whitelistBackupKeep", 5, 1, 20);
            commandHistorySize = builder.comment("How many recent commands to keep in memory for /logger view/search").defineInRange("commandHistorySize", 5000, 100, 20000);
            builder.pop();
        }
    }
}
