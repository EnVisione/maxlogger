package com.enviouse.maxlogger.logging;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpyManager {
    private final Map<UUID, Boolean> spies = new ConcurrentHashMap<>();

    public boolean toggle(ServerPlayer player) {
        boolean now = spies.merge(player.getUUID(), true, (oldVal, unused) -> !oldVal);
        saveToTag(player, now);
        return now;
    }

    public void load(ServerPlayer player) {
        boolean flag = readFromTag(player);
        if (flag) spies.put(player.getUUID(), true);
    }

    private void saveToTag(ServerPlayer player, boolean value) {
        CompoundTag persistent = player.getPersistentData();
        CompoundTag data = persistent.contains("maxlogger", 10) ? persistent.getCompound("maxlogger") : new CompoundTag();
        data.putBoolean("spy", value);
        persistent.put("maxlogger", data);
    }

    private boolean readFromTag(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains("maxlogger", 10)) return false;
        CompoundTag data = persistent.getCompound("maxlogger");
        return data.getBoolean("spy");
    }

    public boolean isSpying(UUID uuid) {
        return spies.getOrDefault(uuid, false);
    }
}
