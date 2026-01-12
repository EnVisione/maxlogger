package com.enviouse.maxlogger.data;

import com.enviouse.maxlogger.util.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WhitelistManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private final int backupKeep;
    private final Map<UUID, String> entries = new HashMap<>();

    public WhitelistManager(Path file, int backupKeep) {
        this.file = file;
        this.backupKeep = backupKeep;
    }

    public void load() {
        if (!Files.exists(file)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            WhitelistFile data = GSON.fromJson(reader, WhitelistFile.class);
            entries.clear();
            if (data != null && data.entries != null) {
                entries.putAll(data.entries);
            }
        } catch (IOException e) {
            LOGGER.error("Failed loading whitelist {}", file, e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            FileUtils.backup(file, backupKeep);
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(new WhitelistFile(entries), writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed saving whitelist {}", file, e);
        }
    }

    public boolean add(UUID id, String name) {
        boolean changed = entries.put(id, name) == null;
        if (changed) save();
        return changed;
    }

    public boolean remove(UUID id) {
        boolean changed = entries.remove(id) != null;
        if (changed) save();
        return changed;
    }

    public void clear() {
        entries.clear();
        save();
    }

    public boolean contains(UUID id) {
        return entries.containsKey(id);
    }

    public Map<UUID, String> view() {
        return Collections.unmodifiableMap(entries);
    }

    private static class WhitelistFile {
        Map<UUID, String> entries;

        WhitelistFile(Map<UUID, String> entries) {
            this.entries = entries;
        }
    }
}
