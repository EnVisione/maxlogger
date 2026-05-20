
# AntiCheat Mod - Development Roadmap
## Minecraft 1.20.1 Forge

---

## Project Overview
A lightweight server-side anti-cheat mod that combines gamemode enforcement and command logging with owner-controlled whitelisting and real-time monitoring capabilities.

---

## Phase 1: Project Setup & Foundation
**Estimated Time**: 1-2 days

### 1.1 Project Structure
- [ ] Initialize Forge 1.20.1 MDK project
- [ ] Set up build.gradle with:
  - Forge version: 47.x.x
  - Java 17 compatibility
  - Mod ID: `anticheat`
  - Version: `1.0.0`
- [ ] Configure mods.toml metadata
- [ ] Set up project package structure:
  ```
  com.yourname.anticheat/
  ├── AntiCheat.java (main mod class)
  ├── config/
  │   ├── AntiCheatConfig.java
  │   └── ConfigHolder.java
  ├── enforcement/
  │   ├── GamemodeEnforcer.java
  │   └── EnforcementScheduler.java
  ├── logging/
  │   ├── CommandLogger.java
  │   ├── LogWriter.java
  │   └── SpyManager.java
  ├── commands/
  │   ├── EnforcerCommand.java
  │   └── LoggerCommand.java
  ├── events/
  │   ├── PlayerEventHandler.java
  │   └── CommandEventHandler.java
  ├── data/
  │   ├── WhitelistManager.java
  │   └── SessionManager.java
  └── util/
      ├── FileUtils.java
      └── TextUtils.java
  ```

### 1.2 Development Environment
- [ ] Set up IDE (IntelliJ IDEA recommended)
- [ ] Configure run configurations for client/server testing
- [ ] Set up Git repository
- [ ] Create .gitignore for build artifacts

---

## Phase 2: Core Configuration System
**Estimated Time**: 2-3 days

### 2.1 Config File Structure
- [ ] Create `anticheat-common.toml` in config folder
- [ ] Implement config values:
  ```toml
  [general]
  owner = "BreezilyOnMyMind"

  [enforcer]
  enabled = true
  checkIntervalTicks = 20
  notifyOps = true
  logDirectory = "anticheat/logs"

  [logger]
  enabled = true
  logCommands = true
  logJoinLeave = true
  logFailedCommands = true
  ```

### 2.2 Config Management
- [ ] Implement Forge ConfigSpec system
- [ ] Create ConfigHolder for runtime access
- [ ] Implement config reload command
- [ ] Add config validation on load
- [ ] Handle missing/corrupted config gracefully

---

## Phase 3: Data Management Layer
**Estimated Time**: 2-3 days

### 3.1 File Management
- [ ] Create FileUtils for safe file I/O
- [ ] Implement JSON serialization/deserialization
- [ ] Create data directory structure on first run
- [ ] Implement automatic backup system for whitelists

### 3.2 Whitelist System
- [ ] Create WhitelistManager class
- [ ] Implement separate whitelists:
  - `gamemode_whitelist.json` (enforcer exemptions)
  - `logger_whitelist.json` (logging exemptions)
- [ ] Add UUID + username storage
- [ ] Implement add/remove/clear/list operations
- [ ] Add automatic UUID resolution on name add

### 3.3 Session Management
- [ ] Create SessionManager for log file rotation
- [ ] Generate session IDs on server start
- [ ] Format: `enforcer_YYYY-MM-DD_HH-MM-SS.log`
- [ ] Implement "latest.log" symlink/copy system
- [ ] Add session info tracking (start time, server version, etc.)

---

## Phase 4: Gamemode Enforcement System
**Estimated Time**: 3-4 days

### 4.1 Core Enforcement Logic
- [ ] Create GamemodeEnforcer class
- [ ] Implement accurate gamemode detection:
  - Use `ServerPlayer.gameMode.getGameModeForPlayer()`
  - Handle all gamemodes: survival, creative, spectator, adventure
- [ ] Add owner + whitelist exemption checks
- [ ] Implement forced gamemode change with logging

### 4.2 Enforcement Scheduler
- [ ] Create EnforcementScheduler with TickEvent.ServerTickEvent
- [ ] Implement configurable tick interval (default: 20 ticks)
- [ ] Add join-time enforcement (delayed 10 ticks after login)
- [ ] Implement periodic enforcement loop
- [ ] Add per-player cooldown to prevent spam

### 4.3 Enforcement Logging
- [ ] Log format: `[timestamp] username (uuid) changed from X -> survival [reason] at (x, y, z) in dimension`
- [ ] Implement dual logging (session + latest)
- [ ] Add operator notification system
- [ ] Create enforcement statistics tracking

### 4.4 Enforcer Commands
- [ ] `/enforcer` - Show help menu
- [ ] `/enforcer add <player>` - Add to whitelist
- [ ] `/enforcer remove <player>` - Remove from whitelist
- [ ] `/enforcer clear` - Clear entire whitelist
- [ ] `/enforcer list` - Show all whitelisted players
- [ ] `/enforcer stats` - Show enforcement statistics
- [ ] Add permission check (owner-only)

---

## Phase 5: Command Logging System
**Estimated Time**: 3-4 days

### 5.1 Command Interception
- [ ] Create CommandEventHandler
- [ ] Hook into Forge CommandEvent at MONITOR priority
- [ ] Extract command string from parse results
- [ ] Handle both successful and failed commands
- [ ] Implement player detection and UUID resolution

### 5.2 Log Writing System
- [ ] Create LogWriter with async writing queue
- [ ] Implement thread-safe file writing
- [ ] Format: `[timestamp] username (uuid) at (x, y, z) in dimension: /command`
- [ ] Add join/leave event logging
- [ ] Implement log rotation on size limit

### 5.3 Deduplication System
- [ ] Create per-player, per-tick command cache
- [ ] Implement HashMap<UUID, CommandCache>
- [ ] Auto-cleanup old cache entries every 60 seconds
- [ ] Prevent duplicate logging from multiple event sources

### 5.4 Spy Mode System
- [ ] Create SpyManager class
- [ ] Store spy mode state in player persistent data
- [ ] Implement real-time broadcast to spy users
- [ ] Format: `[Spy] username: /command`
- [ ] Add toggle persistence across sessions

### 5.5 Logger Commands
- [ ] `/logger` - Show help menu
- [ ] `/logger spy` - Toggle spy mode
- [ ] `/logger whitelist add <player>` - Exempt from logging
- [ ] `/logger whitelist remove <player>` - Remove exemption
- [ ] `/logger whitelist list` - Show exempted players
- [ ] `/logger session` - Show current session info
- [ ] `/logger stats` - Show logging statistics
- [ ] Add permission check (owner-only)

---

## Phase 6: Event Handling & Integration
**Estimated Time**: 2-3 days

### 6.1 Player Events
- [ ] Register PlayerEvent.PlayerLoggedInEvent handler
- [ ] Register PlayerEvent.PlayerLoggedOutEvent handler
- [ ] Trigger enforcement check on join
- [ ] Log join/leave events
- [ ] Clean up player data on disconnect

### 6.2 Command Events
- [ ] Register CommandEvent handler
- [ ] Extract command details from all sources
- [ ] Handle chat commands (fallback)
- [ ] Filter out internal/system commands if needed

### 6.3 Tick Events
- [ ] Register TickEvent.ServerTickEvent handler
- [ ] Implement configurable enforcement interval
- [ ] Add server TPS monitoring to pause enforcement if laggy
- [ ] Optimize iteration over online players

---

## Phase 7: User Interface & Feedback
**Estimated Time**: 2 days

### 7.1 Chat Messages
- [ ] Implement colored text formatting helper
- [ ] Create consistent message templates
- [ ] Add operator-only notifications
- [ ] Implement spy mode broadcast formatting

### 7.2 Command Help System
- [ ] Create detailed help menus for both commands
- [ ] Add command usage hints on errors
- [ ] Implement tab completion for player names
- [ ] Add permission denied messages

### 7.3 Statistics Display
- [ ] Track total enforcements
- [ ] Track commands logged
- [ ] Show uptime and session info
- [ ] Display whitelist sizes

---

## Phase 8: Testing & Quality Assurance
**Estimated Time**: 3-4 days

### 8.1 Unit Testing
- [ ] Test gamemode detection accuracy
- [ ] Test whitelist add/remove operations
- [ ] Test file I/O under various conditions
- [ ] Test config loading/saving
- [ ] Test deduplication logic

### 8.2 Integration Testing
- [ ] Test on dedicated server
- [ ] Test with multiple players online
- [ ] Test enforcement with all gamemodes
- [ ] Test command logging for all command types
- [ ] Test with various Forge mods installed

### 8.3 Performance Testing
- [ ] Benchmark enforcement tick performance
- [ ] Test with 50+ players
- [ ] Profile memory usage
- [ ] Test log file I/O performance
- [ ] Optimize hot paths

### 8.4 Edge Case Testing
- [ ] Player disconnects during enforcement
- [ ] Server crash/restart scenarios
- [ ] Corrupted whitelist files
- [ ] Disk full errors
- [ ] Very long commands (command block chains)
- [ ] Rapid gamemode switching

---

## Phase 9: Documentation
**Estimated Time**: 2 days

### 9.1 User Documentation
- [ ] Create README.md with:
  - Installation instructions
  - Configuration guide
  - Command reference
  - Troubleshooting section
- [ ] Add example configurations
- [ ] Create FAQ section

### 9.2 Developer Documentation
- [ ] Document all public APIs
- [ ] Add Javadoc comments
- [ ] Create architecture diagram
- [ ] Document event flow
- [ ] Add contribution guidelines

### 9.3 Change Log
- [ ] Create CHANGELOG.md
- [ ] Document initial release features
- [ ] Set up versioning scheme

---

## Phase 10: Release Preparation
**Estimated Time**: 1-2 days

### 10.1 Build Optimization
- [ ] Configure ProGuard/Minimization if needed
- [ ] Remove debug code
- [ ] Optimize imports
- [ ] Run linter/formatter

### 10.2 Packaging
- [ ] Build final JAR
- [ ] Test JAR on clean server
- [ ] Create release ZIP with:
  - Mod JAR
  - README
  - LICENSE
  - Default config
  - Example whitelists

### 10.3 Distribution
- [ ] Upload to CurseForge
- [ ] Upload to Modrinth
- [ ] Create GitHub release
- [ ] Announce on Discord/forums

---

## Future Enhancements (Post-Release)

### Phase 11: Advanced Features
- [ ] Database backend support (MySQL/SQLite)
- [ ] Web dashboard for log viewing
- [ ] Discord webhook integration
- [ ] Advanced permission system (LuckPerms integration)
- [ ] Custom enforcement actions (kick, ban, etc.)
- [ ] Configurable enforcement delay per player
- [ ] Whitelist expiration dates
- [ ] IP logging
- [ ] Playtime-based auto-whitelist

### Phase 12: Analytics & Reporting
- [ ] Generate daily/weekly reports
- [ ] Command usage statistics
- [ ] Most common violations
- [ ] Export logs to CSV/JSON
- [ ] Grafana/Prometheus metrics

### Phase 13: Multi-Server Support
- [ ] Shared whitelist across servers (Redis)
- [ ] Cross-server enforcement sync
- [ ] Centralized logging server

---

## Technical Specifications

### Dependencies
- **Minecraft**: 1.20.1
- **Forge**: 47.x.x (latest stable)
- **Java**: 17+
- **Gson**: For JSON handling (included in Forge)

### File Structure
```
config/anticheat/
├── anticheat-common.toml
├── gamemode_whitelist.json
└── logger_whitelist.json

logs/anticheat/
├── enforcer_latest.log
├── enforcer_2026-01-11_14-52-33.log
├── command_log_latest.log
└── command_log_2026-01-11_14-52-33.log
```

### Performance Targets
- Enforcement check: < 0.1ms per player
- Command logging: < 0.05ms per command
- File I/O: Async, non-blocking
- Memory overhead: < 10MB for 100 players

### Compatibility Requirements
- Server-side only (no client installation required)
- Compatible with Bukkit/Spigot command plugins
- No conflicts with other security mods
- Works with offline-mode servers

---

## Timeline Summary
- **Total Estimated Time**: 20-30 days (full-time development)
- **Minimum Viable Product (MVP)**: Phases 1-6 (12-18 days)
- **Production Ready**: Phases 1-10 (20-30 days)

---

## Risk Assessment

### High Risk
- Forge API changes between versions
- Performance impact on large servers
- File I/O failures on server crashes

### Medium Risk
- Config parsing errors
- Command event not capturing all commands
- Thread safety issues with concurrent access

### Low Risk
- Whitelist data corruption
- Memory leaks from player tracking
- Compatibility with exotic server setups

### Mitigation Strategies
- Extensive testing across Forge versions
- Implement try-catch blocks around all I/O
- Use thread-safe collections (ConcurrentHashMap)
- Add config validation and auto-repair
- Implement graceful degradation on errors

---

## Success Metrics
- [ ] Zero crashes in 7-day test period
- [ ] < 1% impact on server TPS
- [ ] 100% command capture rate
- [ ] Successful enforcement within 1 second
- [ ] Clean startup with default config
- [ ] Positive feedback from 10+ server owners

---

## Maintenance Plan
- **Bug fixes**: Within 48 hours of report
- **Minecraft version updates**: Within 2 weeks of Forge release
- **Feature requests**: Evaluated monthly
- **Security patches**: Immediate

