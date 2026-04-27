package com.lin.redis.config;

import com.lin.common.redis.prefix.RedisKeyPrefixProvider;
import org.springframework.stereotype.Component;

/**
 * Redis 模块自身 Key 管理。
 * <p>
 * 此处集中管理 redis 模块内部产生的 Redis Key（如缓存、分布式锁等场景）。
 * 各业务模块应参考此模板自行创建 {@code XxxRedisKeys} 并实现 {@link RedisKeyPrefixProvider}。
 */
@Component
public class RedisKeys implements RedisKeyPrefixProvider {

    /** 模块前缀 */
    public static final String PREFIX = "cache";

    // ─── 通用缓存 Key ───

    public static final String CACHE_OBJECT = PREFIX + ":object:";

    // ─── Key 构建工具方法 ───

    /** 业务缓存 key */
    public static String cacheKey(String name) {
        return PREFIX + ":object:" + name;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
