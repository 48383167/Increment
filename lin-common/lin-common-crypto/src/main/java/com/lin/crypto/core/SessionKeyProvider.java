package com.lin.crypto.core;

import javax.crypto.SecretKey;

/**
 * 会话密钥管理接口。
 * 登录时生成密钥并返回给前端，后续加密响应时通过 userId 查找对应密钥。
 */
public interface SessionKeyProvider {

    /**
     * 为用户创建会话密钥。
     *
     * @param userId 用户标识
     * @return base64 编码的 AES 密钥（前端保存此值用于解密）
     */
    String createSession(String userId);

    /**
     * 获取用户的当前会话密钥。
     *
     * @param userId 用户标识
     * @return 会话密钥，不存在时返回 null
     */
    SecretKey getKey(String userId);

    /**
     * 移除用户会话密钥（登出时调用）。
     */
    void removeSession(String userId);
}
