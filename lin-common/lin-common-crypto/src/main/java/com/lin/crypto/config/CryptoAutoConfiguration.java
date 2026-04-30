package com.lin.crypto.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lin.crypto.advice.EncryptResponseBodyAdvice;
import com.lin.crypto.core.SessionKeyProvider;
import com.lin.crypto.core.UserIdProvider;
import com.lin.crypto.session.InMemorySessionKeyProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(CryptoProperties.class)
@ConditionalOnProperty(prefix = "lin.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CryptoAutoConfiguration {

    private final CryptoProperties cryptoProperties;

    @Bean
    @ConditionalOnMissingBean
    public SessionKeyProvider sessionKeyProvider() {
        return new InMemorySessionKeyProvider(cryptoProperties);
    }

    @Bean
    @ConditionalOnClass(name = "com.lin.security.context.UserContext")
    @ConditionalOnMissingBean
    public UserIdProvider userIdProvider() {
        return () -> {
            try {
                Class<?> clazz = Class.forName("com.lin.security.context.UserContext");
                return (String) clazz.getMethod("getUserId").invoke(null);
            } catch (Exception e) {
                return null;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptResponseBodyAdvice encryptResponseBodyAdvice(SessionKeyProvider sessionKeyProvider,
                                                                ObjectMapper objectMapper,
                                                                UserIdProvider userIdProvider) {
        log.info("lin-common-crypto 已启用，响应加密就绪");
        return new EncryptResponseBodyAdvice(sessionKeyProvider, objectMapper, userIdProvider);
    }
}
