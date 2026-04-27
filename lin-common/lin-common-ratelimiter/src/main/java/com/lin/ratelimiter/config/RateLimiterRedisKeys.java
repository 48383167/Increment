package com.lin.ratelimiter.config;

import com.lin.common.redis.prefix.RedisKeyPrefixProvider;
import org.springframework.stereotype.Component;

/**
 * 限流模块 Redis Key 统一管理。
 * <p>
 * 所有限流相关的 Redis Key 均从此处获取，避免各切面/工具类中散落硬编码字符串。
 * Key 最终格式（含全局前缀）：{@code <key-prefix>:rate_limit:<uri>:<dimension>:<bizKey>}
 */
@Component
public class RateLimiterRedisKeys implements RedisKeyPrefixProvider {

    /** 模块前缀，用于跨模块冲突检测 */
    public static final String PREFIX = "rate_limit";

    /** Key 前缀（带冒号分隔），供切面拼接使用 */
    public static final String KEY_PREFIX = PREFIX + ":";

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
