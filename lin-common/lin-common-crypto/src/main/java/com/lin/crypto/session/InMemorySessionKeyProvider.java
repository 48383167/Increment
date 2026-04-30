package com.lin.crypto.session;

import com.lin.crypto.core.CryptoService;
import com.lin.crypto.core.SessionKeyProvider;
import com.lin.crypto.config.CryptoProperties;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InMemorySessionKeyProvider implements SessionKeyProvider {

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "crypto-session-cleaner");
        t.setDaemon(true);
        return t;
    });
    private final long ttlMillis;

    public InMemorySessionKeyProvider(CryptoProperties properties) {
        this.ttlMillis = properties.getSession().getTtlSeconds() * 1000;
        long interval = properties.getSession().getCleanupIntervalSeconds();
        cleaner.scheduleAtFixedRate(this::evictExpired, interval, interval, TimeUnit.SECONDS);
        log.info("会话密钥管理器已初始化，TTL={}秒，清理间隔={}秒",
                properties.getSession().getTtlSeconds(), interval);
    }

    @Override
    public String createSession(String userId) {
        SecretKey key = CryptoService.generateKey();
        cache.put(userId, new CacheEntry(key, System.currentTimeMillis()));
        log.debug("为用户 {} 创建会话密钥", userId);
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    @Override
    public SecretKey getKey(String userId) {
        CacheEntry entry = cache.get(userId);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.createdAt > ttlMillis) {
            cache.remove(userId);
            return null;
        }
        return entry.key;
    }

    @Override
    public void removeSession(String userId) {
        cache.remove(userId);
        log.debug("移除用户 {} 的会话密钥", userId);
    }

    private void evictExpired() {
        long now = System.currentTimeMillis();
        int removed = 0;
        for (Iterator<CacheEntry> it = cache.values().iterator(); it.hasNext(); ) {
            if (now - it.next().createdAt > ttlMillis) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("清理了 {} 个过期会话密钥", removed);
        }
    }

    private static class CacheEntry {
        final SecretKey key;
        final long createdAt;

        CacheEntry(SecretKey key, long createdAt) {
            this.key = key;
            this.createdAt = createdAt;
        }
    }
}
