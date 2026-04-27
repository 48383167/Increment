package com.lin.redis.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.RedisException;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RedisUtils {

    private static RedissonClient CLIENT;

    public static void setClient(RedissonClient redissonClient) {
        RedisUtils.CLIENT = redissonClient;
    }

    public static RedissonClient getClient() {
        return CLIENT;
    }

    // ──────────────────── 通用操作 ────────────────────

    public static <T> void setCacheObject(String key, T value) {
        CLIENT.getBucket(key).set(value);
    }

    public static <T> void setCacheObject(String key, T value, Duration duration) {
        CLIENT.getBucket(key).set(value, duration);
    }

    public static <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit) {
        CLIENT.getBucket(key).set(value, timeout, unit);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getCacheObject(String key) {
        RBucket<T> bucket = CLIENT.getBucket(key);
        return bucket.isExists() ? bucket.get() : null;
    }

    public static boolean deleteObject(String key) {
        return CLIENT.getBucket(key).delete();
    }

    public static long deleteObjects(Collection<String> keys) {
        return CLIENT.getKeys().delete(keys.toArray(new String[0]));
    }

    public static boolean isExistsObject(String key) {
        return CLIENT.getBucket(key).isExists();
    }

    public static boolean expire(String key, long timeout, TimeUnit unit) {
        return CLIENT.getBucket(key).expire(timeout, unit);
    }

    public static long getTimeToLive(String key) {
        return CLIENT.getBucket(key).remainTimeToLive();
    }

    // ──────────────────── 键扫描 ────────────────────

    public static Iterable<String> keys(String pattern) {
        return CLIENT.getKeys().getKeysByPattern(pattern);
    }

    public static long deleteKeys(String pattern) {
        return CLIENT.getKeys().deleteByPattern(pattern);
    }

    // ──────────────────── List 操作 ────────────────────

    public static <T> void setCacheList(String key, List<T> list) {
        RList<T> rList = CLIENT.getList(key);
        rList.delete();
        rList.addAll(list);
    }

    public static <T> List<T> getCacheList(String key) {
        RList<T> rList = CLIENT.getList(key);
        return rList.isExists() ? new ArrayList<>(rList) : new ArrayList<>();
    }

    public static <T> void addCacheList(String key, T value) {
        CLIENT.getList(key).add(value);
    }

    // ──────────────────── Set 操作 ────────────────────

    public static <T> void setCacheSet(String key, Set<T> set) {
        RSet<T> rSet = CLIENT.getSet(key);
        rSet.delete();
        rSet.addAll(set);
    }

    public static <T> Set<T> getCacheSet(String key) {
        RSet<T> rSet = CLIENT.getSet(key);
        return rSet.isExists() ? new HashSet<>(rSet) : new HashSet<>();
    }

    public static <T> void addCacheSet(String key, T value) {
        CLIENT.getSet(key).add(value);
    }

    // ──────────────────── Map 操作 ────────────────────

    public static <K, V> void setCacheMap(String key, Map<K, V> map) {
        RMap<K, V> rMap = CLIENT.getMap(key);
        rMap.delete();
        rMap.putAll(map);
    }

    public static <K, V> Map<K, V> getCacheMap(String key) {
        RMap<K, V> rMap = CLIENT.getMap(key);
        return rMap.isExists() ? new HashMap<>(rMap) : new HashMap<>();
    }

    public static <K, V> void setCacheMapValue(String key, K mapKey, V value) {
        CLIENT.<K, V>getMap(key).put(mapKey, value);
    }

    public static <K, V> V getCacheMapValue(String key, K mapKey) {
        RMap<K, V> rMap = CLIENT.getMap(key);
        return rMap.isExists() ? rMap.get(mapKey) : null;
    }

    public static <K, V> long delCacheMapValue(String key, K mapKey) {
        return CLIENT.<K, V>getMap(key).fastRemove(mapKey);
    }

    // ──────────────────── 原子操作 ────────────────────

    public static void setAtomicValue(String key, long value) {
        CLIENT.getAtomicLong(key).set(value);
    }

    public static long getAtomicValue(String key) {
        return CLIENT.getAtomicLong(key).get();
    }

    public static long incrAtomicValue(String key) {
        return CLIENT.getAtomicLong(key).incrementAndGet();
    }

    public static long decrAtomicValue(String key) {
        return CLIENT.getAtomicLong(key).decrementAndGet();
    }

    // ──────────────────── 限流操作 ────────────────────

    /**
     * 分布式限流
     *
     * @param key      限流 key
     * @param rateType 限流类型（OVERALL 全局计数 / PER_CLIENT 单节点计数）
     * @param count    时间窗口内允许次数
     * @param time     时间窗口（秒）
     * @param timeout  限流规则过期时间（秒）
     * @return 剩余令牌数，-1 表示被限流
     */
    public static long rateLimiter(String key, RateType rateType, int count, int time, int timeout) {
        if (CLIENT == null) {
            throw new RedisException("RedissonClient 未初始化，请检查 Redis 配置");
        }
        RRateLimiter rateLimiter = CLIENT.getRateLimiter(key);
        boolean setRate = rateLimiter.trySetRate(rateType, count, time, RateIntervalUnit.SECONDS);
        if (setRate) {
            rateLimiter.expireAsync(Duration.ofSeconds(timeout));
        }
        if (!rateLimiter.tryAcquire()) {
            return -1;
        }
        return rateLimiter.availablePermits();
    }

    /**
     * 获取 Redisson 客户端 ID（集群模式下限流 key 拼接用）
     */
    public static String getClientId() {
        return CLIENT != null ? CLIENT.getId() : "";
    }
}
