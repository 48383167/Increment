package com.lin.ratelimiter.annotation;

import com.lin.ratelimiter.enums.LimitType;

import java.lang.annotation.*;

/**
 * 限流注解，支持使用Spring el表达式来动态获取方法上的参数值
 * 格式类似于  #code.id #{#code}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {

    /**
     * 限流 key，支持 SpEL 表达式，如 #{#user.id}
     */
    String key() default "";

    /**
     * 限流时间，单位秒
     */
    int time() default 60;

    /**
     * 限流次数
     */
    int count() default 10;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * 提示消息
     */
    String message() default "您的请求过于频繁，请稍候再试";

    /**
     * 限流策略超时时间，默认一天（策略存活时间，会清除已存在的策略数据）
     */
    int timeout() default 86400;

}
