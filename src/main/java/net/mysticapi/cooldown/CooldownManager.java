package net.mysticapi.cooldown;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void setCooldown(UUID uuid, String key, long duration, TimeUnit unit) {
        long expireAt = System.currentTimeMillis() + unit.toMillis(duration);
        cooldowns.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>()).put(key, expireAt);
    }

    public void setCooldown(Player player, String key, long duration, TimeUnit unit) {
        setCooldown(player.getUniqueId(), key, duration, unit);
    }

    public boolean isOnCooldown(UUID uuid, String key) {
        Long expireAt = getExpiry(uuid, key);
        return expireAt != null && expireAt > System.currentTimeMillis();
    }

    public boolean isOnCooldown(Player player, String key) {
        return isOnCooldown(player.getUniqueId(), key);
    }

    public long getRemaining(UUID uuid, String key, TimeUnit unit) {
        Long expireAt = getExpiry(uuid, key);
        if (expireAt == null) {
            return 0;
        }
        long remainingMillis = expireAt - System.currentTimeMillis();
        return remainingMillis > 0 ? unit.convert(remainingMillis, TimeUnit.MILLISECONDS) : 0;
    }

    public long getRemaining(Player player, String key, TimeUnit unit) {
        return getRemaining(player.getUniqueId(), key, unit);
    }

    public void removeCooldown(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        if (map != null) {
            map.remove(key);
            if (map.isEmpty()) {
                cooldowns.remove(uuid);
            }
        }
    }

    public void removeCooldown(Player player, String key) {
        removeCooldown(player.getUniqueId(), key);
    }

    public void clear(UUID uuid) {
        cooldowns.remove(uuid);
    }

    public void clear(Player player) {
        clear(player.getUniqueId());
    }

    public void clearAll() {
        cooldowns.clear();
    }

    private Long getExpiry(UUID uuid, String key) {
        Map<String, Long> map = cooldowns.get(uuid);
        return map == null ? null : map.get(key);
    }
}
