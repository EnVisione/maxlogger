# MaxLogger AntiCheat (Forge 1.20.1)

Lightweight server-side gamemode enforcer and command logger.

## Features (MVP)
- Enforces survival mode on non-whitelisted players; owner + whitelist exempt.
- Command logging with join/leave, failure flag, spy broadcasts.
- Whitelists stored in `config/anticheat/*.json` with name/UUID pairs.
- Async log writing with size-based rotation copy to `*_latest.log`.
- Admin commands (requires op or configured owner):
  - `/enforcer add|remove|list|clear|stats`
  - `/logger spy` (toggle), `/logger whitelist add|remove|list`, `/logger stats`, `/logger session`

## Config
Generated at `config/maxlogger-common.toml` (forge default). Key values mirror roadmap:
- `general.owner` – owner username with full control
- `enforcer.enabled`, `checkIntervalTicks`, `notifyOps`, `logDirectory`
- `logger.enabled`, `logCommands`, `logJoinLeave`, `logFailedCommands`, `logRotationBytes`

## Building
Requires JDK 17.
```powershell
$env:JAVA_HOME="C:\\path\\to\\jdk17"
./gradlew.bat --no-daemon jar
```

## Runtime paths
- Config: `config/anticheat/`
- Logs: `anticheat/logs/` (configurable)

## Roadmap coverage
Implemented phases 1-6 items (structure, config, whitelists, enforcement loop, command logging, spy, commands, events). Remaining: backups, config reload command, TPS gating, help menus, advanced stats, tests, docs expansion.

