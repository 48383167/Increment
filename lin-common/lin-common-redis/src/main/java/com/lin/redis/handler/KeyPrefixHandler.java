package com.lin.redis.handler;

import com.lin.redis.config.properties.RedissonProperties;
import org.redisson.api.NameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class KeyPrefixHandler implements NameMapper {

    @Autowired
    private RedissonProperties redissonProperties;

    @Override
    public String map(String name) {
        if (redissonProperties.getKeyPrefix() != null && !redissonProperties.getKeyPrefix().isBlank()) {
            return redissonProperties.getKeyPrefix() + ":" + name;
        }
        return name;
    }

    @Override
    public String unmap(String name) {
        if (redissonProperties.getKeyPrefix() != null && !redissonProperties.getKeyPrefix().isBlank()) {
            String prefix = redissonProperties.getKeyPrefix() + ":";
            if (name.startsWith(prefix)) {
                return name.substring(prefix.length());
            }
        }
        return name;
    }
}
