package com.lin.common.redis.prefix;

/**
 * Redis Key 前缀提供者 — 各模块的 Redis Keys 管理类实现此接口，
 * 由 {@code RedisKeyConflictChecker} 在启动时自动收集并校验前缀无冲突。
 * <p>
 * 定义在 lin-common-core 而非 lin-common-redis，是为了让不依赖 redis 模块的
 * 模块也能实现此接口（通过 Spring 自动发现），不增加额外耦合。
 */
@FunctionalInterface
public interface RedisKeyPrefixProvider {

    /**
     * 返回该模块在 Redis 中的 Key 前缀（命名空间），
     * 如 "rate_limit"、"cache"、"queue" 等。
     * 不同模块的前缀不能重复。
     */
    String getPrefix();
}
