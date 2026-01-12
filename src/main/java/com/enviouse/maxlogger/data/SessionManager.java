package com.enviouse.maxlogger.data;

import com.enviouse.maxlogger.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SessionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final Path directory;
    private final String prefix;
    private Path currentSession;
    private final Path latestFile;
    private final LocalDateTime start;

    public SessionManager(Path directory, String prefix) {
        this.directory = directory;
        this.prefix = prefix;
        FileUtils.createDirectories(directory);
        String timestamp = LocalDateTime.now().format(FORMATTER);
        this.currentSession = directory.resolve(prefix + "_" + timestamp + ".log");
        this.latestFile = directory.resolve(prefix + "_latest.log");
        touch(currentSession);
        touch(latestFile);
        this.start = LocalDateTime.now();
        FileUtils.copyLatest(currentSession, latestFile);
    }

    public Path getSessionFile() {
        return currentSession;
    }

    public Path rotate() {
        currentSession = FileUtils.rotateSession(directory, prefix);
        updateLatest();
        return currentSession;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void updateLatest() {
        FileUtils.copyLatest(currentSession, latestFile);
    }

    private void touch(Path path) {
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create file {}", path, e);
        }
    }
}
