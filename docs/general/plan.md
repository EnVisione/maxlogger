# MaxLogger Implementation Plan

## Current State

MaxLogger targets Minecraft 1.20.1 with Forge 47.4.13 and Java 17. It provides server side command logging, gamemode enforcement, persistent whitelists, asynchronous log writing, and operator commands. The current workflow migration branch adopts the shared organization verification workflows.

Linux verification exposed a case sensitive source layout defect. The public `MaxLogger` class was stored in `Maxlogger.java`. Windows accepted the path, while Linux correctly rejected it because a public Java class must use an exactly matching filename.

## Phase 1. Workflow Migration Repair

### Scope

- Preserve the shared quality and release validation callers.
- Rename the mod entrypoint source file to `MaxLogger.java` without changing its package, class, mod identifier, or behavior.
- Keep Minecraft, Forge, Java, mappings, Gradle, configuration, commands, and persisted file formats unchanged.
- Ignore local CodeGraph data and repository instructions.
- Document the verified architecture and build process.

### Verification

1. Run the checked in Gradle Wrapper with Java 17.
2. Run `gradlew.bat build` on Windows or `./gradlew build` on Linux.
3. Confirm that Forge resource expansion succeeds.
4. Inspect the final JAR for the `MaxLogger` entrypoint and required metadata.
5. Confirm the complete diff contains no build output, run data, credentials, or local indexes.
6. Require the shared GitHub quality checks to pass before merge.

### Acceptance Criteria

- Java compilation succeeds on a case sensitive filesystem.
- The entrypoint remains `com.enviouse.maxlogger.MaxLogger`.
- The mod identifier remains `maxlogger`.
- The shared workflow migration checks pass without weakening any gate.
- Documentation reflects the implementation that exists on the branch.
