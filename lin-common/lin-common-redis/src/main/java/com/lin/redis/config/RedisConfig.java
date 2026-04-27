package com.lin.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.lin.redis.config.properties.RedissonProperties;
import com.lin.redis.handler.KeyPrefixHandler;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.codec.CompositeCodec;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedisConfig {

    @Autowired
    private RedissonProperties redissonProperties;

    @Autowired
    private KeyPrefixHandler keyPrefixHandler;

    @Bean
    @ConditionalOnBean(RedissonClient.class)
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return config -> {
            ObjectMapper om = new ObjectMapper();
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            om.registerModule(javaTimeModule);

            // Keys 用 String 编码，Values 用 Jackson JSON 编码
            config.setCodec(new CompositeCodec(
                    StringCodec.INSTANCE,
                    new TypedJsonJacksonCodec(Object.class, om)));

            // 线程池配置
            if (redissonProperties.getThreads() != null) {
                config.setThreads(redissonProperties.getThreads());
            }
            if (redissonProperties.getNettyThreads() != null) {
                config.setNettyThreads(redissonProperties.getNettyThreads());
            }
            config.setUseScriptCache(true);

            // 单节点模式
            RedissonProperties.SingleServerConfig single = redissonProperties.getSingleServerConfig();
            if (single != null && config.useSingleServer() != null) {
                config.useSingleServer()
                        .setNameMapper(keyPrefixHandler)
                        .setClientName(single.getClientName());
                if (single.getTimeout() != null) {
                    config.useSingleServer().setTimeout(single.getTimeout());
                }
                if (single.getIdleConnectionTimeout() != null) {
                    config.useSingleServer().setIdleConnectionTimeout(single.getIdleConnectionTimeout());
                }
                if (single.getConnectionPoolSize() != null) {
                    config.useSingleServer().setConnectionPoolSize(single.getConnectionPoolSize());
                }
                if (single.getConnectionMinimumIdleSize() != null) {
                    config.useSingleServer().setConnectionMinimumIdleSize(single.getConnectionMinimumIdleSize());
                }
                if (single.getSubscriptionConnectionPoolSize() != null) {
                    config.useSingleServer().setSubscriptionConnectionPoolSize(single.getSubscriptionConnectionPoolSize());
                }
            }

            // 集群模式
            RedissonProperties.ClusterServersConfig cluster = redissonProperties.getClusterServersConfig();
            if (cluster != null && config.useClusterServers() != null) {
                config.useClusterServers()
                        .setNameMapper(keyPrefixHandler)
                        .setClientName(cluster.getClientName());
                if (cluster.getTimeout() != null) {
                    config.useClusterServers().setTimeout(cluster.getTimeout());
                }
                if (cluster.getIdleConnectionTimeout() != null) {
                    config.useClusterServers().setIdleConnectionTimeout(cluster.getIdleConnectionTimeout());
                }
                if (cluster.getSubscriptionConnectionPoolSize() != null) {
                    config.useClusterServers().setSubscriptionConnectionPoolSize(cluster.getSubscriptionConnectionPoolSize());
                }
                if (cluster.getMasterConnectionPoolSize() != null) {
                    config.useClusterServers().setMasterConnectionPoolSize(cluster.getMasterConnectionPoolSize());
                }
                if (cluster.getMasterConnectionMinimumIdleSize() != null) {
                    config.useClusterServers().setMasterConnectionMinimumIdleSize(cluster.getMasterConnectionMinimumIdleSize());
                }
                if (cluster.getSlaveConnectionPoolSize() != null) {
                    config.useClusterServers().setSlaveConnectionPoolSize(cluster.getSlaveConnectionPoolSize());
                }
                if (cluster.getSlaveConnectionMinimumIdleSize() != null) {
                    config.useClusterServers().setSlaveConnectionMinimumIdleSize(cluster.getSlaveConnectionMinimumIdleSize());
                }
            }

            log.info("Redisson 配置初始化完成");
        };
    }
}
