package com.lin.crypto.annotation;

import java.lang.annotation.*;

/**
 * 标记接口响应需要加密。
 * <p>
 * 用在类上 → 整个 Controller 所有方法加密。<br>
 * 用在方法上 → 仅该方法加密（优先级高于类注解）。
 * <p>
 * 匿名接口（无 UserContext）即使标注此注解也不会加密。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptResponse {
}
