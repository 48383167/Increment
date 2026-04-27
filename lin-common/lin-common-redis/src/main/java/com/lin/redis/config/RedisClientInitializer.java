package com.lin.redis.config;

import com.lin.redis.utils.RedisUtils;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(RedissonClient.class)
public class RedisClientInitializer implements InitializingBean {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void afterPropertiesSet() {
        RedisUtils.setClient(redissonClient);
    }
}
