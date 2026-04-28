package com.lin.security.annotation;

import java.lang.annotation.*;

/**
 * 标记接口为公开访问（无需认证）。
 * 用在类上 → 整个 Controller 所有方法公开。
 * 用在方法上 → 仅该方法公开（优先级高于类注解）。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Anonymous {
}
