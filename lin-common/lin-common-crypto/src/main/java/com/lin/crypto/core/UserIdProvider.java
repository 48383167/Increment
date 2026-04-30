package com.lin.crypto.core;

/**
 * 当前用户 ID 提供者。
 * 由安全模块或业务模块实现，供 EncryptResponseBodyAdvice 获取当前用户标识。
 */
@FunctionalInterface
public interface UserIdProvider {
    String getCurrentUserId();
}
