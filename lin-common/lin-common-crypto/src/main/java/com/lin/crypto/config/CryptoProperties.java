package com.lin.crypto.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lin.crypto")
public class CryptoProperties {
    private boolean enabled = true;
    private Session session = new Session();

    @Data
    public static class Session {
        /** 会话密钥过期时间（秒），默认与 JWT 过期时间一致 */
        private long ttlSeconds = 7200;
        /** 过期会话清理间隔（秒） */
        private long cleanupIntervalSeconds = 300;
    }
}
