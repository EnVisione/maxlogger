package com.enviouse.maxlogger.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public final class PlayerResolver {
    private PlayerResolver() {}

    public static Optional<ResolvedPlayer> resolve(CommandSourceStack src, String input) {
        MinecraftServer server = src.getServer();
        if (server == null) return Optional.empty();

        UUID parsed = tryParseUUID(input);
        if (parsed != null) {
            String name = findNameByUUID(server, parsed).orElse(input);
            return Optional.of(new ResolvedPlayer(parsed, name));
        }

        // Try online player exact match (case-insensitive)
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getGameProfile().getName().equalsIgnoreCase(input)) {
                return Optional.of(new ResolvedPlayer(p.getUUID(), p.getGameProfile().getName()));
            }
        }

        // Try profile cache (offline lookup)
        return server.getProfileCache().get(input).map(profile -> new ResolvedPlayer(profile.getId(), profile.getName()));
    }

    private static Optional<String> findNameByUUID(MinecraftServer server, UUID id) {
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (p.getUUID().equals(id)) {
                return Optional.of(p.getGameProfile().getName());
            }
        }
        Optional<GameProfile> cached = server.getProfileCache().get(id);
        return cached.map(GameProfile::getName);
    }

    public static boolean isOwner(CommandSourceStack src, String ownerName) {
        if (ownerName == null || ownerName.isBlank()) return false;
        if (src.hasPermission(3)) return true;
        return src.getEntity() instanceof ServerPlayer player && ownerName.equalsIgnoreCase(player.getGameProfile().getName());
    }

    private static UUID tryParseUUID(String input) {
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public record ResolvedPlayer(UUID id, String name) {}
}
