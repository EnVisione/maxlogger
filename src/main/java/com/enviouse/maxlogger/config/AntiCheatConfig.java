package com.enviouse.maxlogger.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class AntiCheatConfig {
    private AntiCheatConfig() {}

    public static void bake(ModValues values) {
        values.owner = ConfigHolder.COMMON.owner.get();
        values.enforcerEnabled = ConfigHolder.COMMON.enforcerEnabled.get();
        values.enforcerInterval = ConfigHolder.COMMON.enforcerIntervalTicks.get();
        values.enforcerNotifyOps = ConfigHolder.COMMON.enforcerNotifyOps.get();
        values.logDirectory = ConfigHolder.COMMON.logDirectory.get();
        values.loggerEnabled = ConfigHolder.COMMON.loggerEnabled.get();
        values.logCommands = ConfigHolder.COMMON.logCommands.get();
        values.logJoinLeave = ConfigHolder.COMMON.logJoinLeave.get();
        values.logFailedCommands = ConfigHolder.COMMON.logFailedCommands.get();
        values.rotationBytes = ConfigHolder.COMMON.logRotationBytes.get();
    }

    public static final class ModValues {
        public String owner;
        public boolean enforcerEnabled;
        public int enforcerInterval;
        public boolean enforcerNotifyOps;
        public String logDirectory;
        public boolean loggerEnabled;
        public boolean logCommands;
        public boolean logJoinLeave;
        public boolean logFailedCommands;
        public long rotationBytes;
    }
}

