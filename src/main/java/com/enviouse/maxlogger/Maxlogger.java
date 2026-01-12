package com.enviouse.maxlogger;

import com.enviouse.maxlogger.commands.EnforcerCommand;
import com.enviouse.maxlogger.commands.LoggerCommand;
import com.enviouse.maxlogger.config.ConfigHolder;
import com.enviouse.maxlogger.data.SessionManager;
import com.enviouse.maxlogger.data.WhitelistManager;
import com.enviouse.maxlogger.enforcement.EnforcementScheduler;
import com.enviouse.maxlogger.events.CommandEventHandler;
import com.enviouse.maxlogger.events.PlayerEventHandler;
import com.enviouse.maxlogger.logging.CommandLogger;
import com.enviouse.maxlogger.logging.LogWriter;
import com.enviouse.maxlogger.logging.SpyManager;
import com.enviouse.maxlogger.util.FileUtils;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.nio.file.Path;

@Mod(MaxLogger.MOD_ID)
public class MaxLogger {
    public static final String MOD_ID = "maxlogger";

    private static final Logger LOGGER = LogUtils.getLogger();

    private static MaxLogger INSTANCE;

    private WhitelistManager gamemodeWhitelist;
    private WhitelistManager loggerWhitelist;
    private SessionManager enforcementSession;
    private SessionManager commandSession;
    private LogWriter enforcementWriter;
    private LogWriter commandWriter;
    private SpyManager spyManager;
    private EnforcementScheduler enforcementScheduler;
    private CommandLogger commandLogger;

    public MaxLogger() {
        INSTANCE = this;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::onCommonSetup);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static MaxLogger get() {
        return INSTANCE;
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(this::bootstrap);
    }

    private void bootstrap() {
        Path dataDir = FileUtils.resolveConfigPath("anticheat");
        Path logDir = FileUtils.resolvePath(ConfigHolder.COMMON.logDirectory.get());

        this.gamemodeWhitelist = new WhitelistManager(dataDir.resolve("gamemode_whitelist.json"), ConfigHolder.COMMON.whitelistBackupKeep.get());
        this.loggerWhitelist = new WhitelistManager(dataDir.resolve("logger_whitelist.json"), ConfigHolder.COMMON.whitelistBackupKeep.get());
        this.gamemodeWhitelist.load();
        this.loggerWhitelist.load();

        this.enforcementSession = new SessionManager(logDir, "enforcer");
        this.commandSession = new SessionManager(logDir, "command_log");
        this.enforcementWriter = new LogWriter(enforcementSession, ConfigHolder.COMMON.logRotationBytes.get());
        this.commandWriter = new LogWriter(commandSession, ConfigHolder.COMMON.logRotationBytes.get());

        this.spyManager = new SpyManager();
        this.enforcementScheduler = new EnforcementScheduler(enforcementWriter, gamemodeWhitelist);
        this.commandLogger = new CommandLogger(commandWriter, loggerWhitelist, spyManager);

        MinecraftForge.EVENT_BUS.register(new PlayerEventHandler(enforcementScheduler, commandLogger));
        MinecraftForge.EVENT_BUS.register(new CommandEventHandler(commandLogger));
        MinecraftForge.EVENT_BUS.register(enforcementScheduler);
        MinecraftForge.EVENT_BUS.register(new CommandRegistrar());
        MinecraftForge.EVENT_BUS.register(new TickHandler());

        LOGGER.info("MaxLogger anti-cheat initialized: owner={}, logsDir={}", ConfigHolder.COMMON.owner.get(), logDir);
    }

    public WhitelistManager getGamemodeWhitelist() {
        return gamemodeWhitelist;
    }

    public WhitelistManager getLoggerWhitelist() {
        return loggerWhitelist;
    }

    public SpyManager getSpyManager() {
        return spyManager;
    }

    public EnforcementScheduler getEnforcementScheduler() {
        return enforcementScheduler;
    }

    public CommandLogger getCommandLogger() {
        return commandLogger;
    }

    private static class CommandRegistrar {
        @SubscribeEvent
        public void onRegisterCommands(RegisterCommandsEvent event) {
            EnforcerCommand.register(event.getDispatcher());
            LoggerCommand.register(event.getDispatcher());
        }
    }

    private class TickHandler {
        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            if (commandLogger != null) {
                commandLogger.tick();
            }
        }

        @SubscribeEvent
        public void onServerStopping(ServerStoppingEvent event) {
            if (enforcementScheduler != null) {
                enforcementScheduler.cleanup();
            }
        }
    }
}
