package com.enviouse.maxlogger.util;

import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class FileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
    }

    public static Path resolveConfigPath(String relative) {
        Path base = FMLPaths.CONFIGDIR.get();
        return base.resolve(relative);
    }

    public static Path resolvePath(String path) {
        Path p = Path.of(path);
        return p.isAbsolute() ? p : FMLPaths.GAMEDIR.get().resolve(p);
    }

    public static void createDirectories(Path dir) {
        try {
            if (dir != null) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create directory {}", dir, e);
        }
    }

    public static void copyLatest(Path source, Path latest) {
        try {
            createDirectories(latest.getParent());
            if (!Files.exists(source)) {
                Files.createFile(source);
            }
            Files.copy(source, latest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException e) {
            LOGGER.error("Failed to copy latest log from {} to {}", source, latest, e);
        }
    }

    public static void backup(Path source, int keep) {
        if (!Files.exists(source)) return;
        Path backupDir = source.getParent().resolve("backup");
        createDirectories(backupDir);
        String name = source.getFileName().toString();
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        Path target = backupDir.resolve(name + "." + stamp + ".bak");
        try {
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            pruneBackups(backupDir, name, keep);
        } catch (IOException e) {
            LOGGER.error("Failed to backup {}", source, e);
        }
    }

    private static void pruneBackups(Path backupDir, String namePrefix, int keep) throws IOException {
        try (var stream = Files.list(backupDir)) {
            List<Path> files = stream
                    .filter(p -> p.getFileName().toString().startsWith(namePrefix))
                    .sorted(Comparator.comparing(Path::toString).reversed())
                    .collect(Collectors.toList());
            for (int i = keep; i < files.size(); i++) {
                Files.deleteIfExists(files.get(i));
            }
        }
    }

    public static Path rotateSession(Path directory, String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        Path session = directory.resolve(prefix + "_" + timestamp + ".log");
        createDirectories(session.getParent());
        try {
            Files.createFile(session);
        } catch (IOException e) {
            LOGGER.error("Failed to create rotated session file {}", session, e);
        }
        return session;
    }
}
