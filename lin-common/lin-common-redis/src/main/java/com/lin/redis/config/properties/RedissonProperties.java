package com.lin.redis.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "redisson")
public class RedissonProperties {

    /**
     * Redis key 全局前缀，如 "myapp" 则所有 key 自动加上 "myapp:"
     */
    private String keyPrefix;

    /**
     * Redisson 工作线程数，默认 = CPU 核数 * 2
     */
    private Integer threads;

    /**
     * Netty 线程数，默认 = CPU 核数 * 2
     */
    private Integer nettyThreads;

    /**
     * 单节点模式配置
     */
    private SingleServerConfig singleServerConfig;

    @Data
    @NoArgsConstructor
    public static class SingleServerConfig {
        private String clientName;
        private Integer connectionMinimumIdleSize;
        private Integer connectionPoolSize;
        private Integer idleConnectionTimeout;
        private Integer timeout;
        private Integer subscriptionConnectionPoolSize;
    }

    /**
     * 集群模式配置
     */
    private ClusterServersConfig clusterServersConfig;

    @Data
    @NoArgsConstructor
    public static class ClusterServersConfig {
        private String clientName;
        private Integer idleConnectionTimeout;
        private Integer timeout;
        private Integer subscriptionConnectionPoolSize;
        private Integer masterConnectionMinimumIdleSize;
        private Integer masterConnectionPoolSize;
        private Integer slaveConnectionMinimumIdleSize;
        private Integer slaveConnectionPoolSize;
    }
}
