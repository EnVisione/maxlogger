# MaxLogger Technical Documentation

## Platform

MaxLogger targets Minecraft 1.20.1 with Forge 47.4.13, official mappings, Java 17, and the checked in Gradle Wrapper. The mod is server oriented and registers through `com.enviouse.maxlogger.MaxLogger`.

## Initialization

Forge constructs `MaxLogger` through the `@Mod` entrypoint. The constructor registers the common configuration, lifecycle listener, and Forge event subscriber. Common setup schedules bootstrap work that loads whitelists, creates logging sessions, starts asynchronous log writers, and registers command, player, command event, enforcement, tick, and server shutdown handlers.

## State and Storage

Gamemode and command logger whitelists are stored as JSON under `config/anticheat/`. Session logs use the configured log directory. `LogWriter` drains a blocking queue on a daemon thread and rotates a session when its configured byte threshold is reached.

Server state remains authoritative. Commands and event handlers use the singleton mod instance only after common setup has initialized the required managers.

## Commands and Permissions

The `/enforcer` and `/logger` command trees are registered during `RegisterCommandsEvent`. Administrative actions are restricted by the command implementations to operators or the configured owner. Command behavior and configuration keys are summarized in the root README.

## Build and Verification

Use Java 17 and the checked in wrapper.

```powershell
gradlew.bat build
```

```bash
./gradlew build
```

A successful build compiles the public `MaxLogger` class from the exactly matching `MaxLogger.java` path, expands Forge metadata, runs configured tests, creates the mod JAR, and reobfuscates it. CI additionally runs shared documentation, secret, dependency, and CodeQL checks when supported.

## Failure Diagnosis

If Linux reports that `MaxLogger` must be declared in `MaxLogger.java`, verify the source filename casing in Git rather than renaming only through a case insensitive filesystem. If Forge metadata expansion fails, compare the placeholders in `mods.toml` with `gradle.properties`. If logging cannot create files, verify the configured directory and server process permissions.
