package com.lin.crypto.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lin.common.result.Result;
import com.lin.crypto.annotation.EncryptResponse;
import com.lin.crypto.core.CryptoService;
import com.lin.crypto.core.SessionKeyProvider;
import com.lin.crypto.core.UserIdProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.crypto.SecretKey;

@Slf4j
@ControllerAdvice
@ConditionalOnProperty(prefix = "lin.crypto", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class EncryptResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final SessionKeyProvider sessionKeyProvider;
    private final ObjectMapper objectMapper;
    private final UserIdProvider userIdProvider;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return returnType.hasMethodAnnotation(EncryptResponse.class)
                || returnType.getContainingClass().isAnnotationPresent(EncryptResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (!(body instanceof Result<?> result)) {
            return body;
        }

        if (result.getData() == null) {
            return body;
        }

        String userId = userIdProvider.getCurrentUserId();
        if (userId == null) {
            log.warn("@EncryptResponse 接口无用户上下文，跳过加密: {}", request.getURI());
            return body;
        }

        SecretKey key = sessionKeyProvider.getKey(userId);
        if (key == null) {
            log.warn("用户 {} 无会话密钥，跳过加密: {}", userId, request.getURI());
            return body;
        }

        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(result.getData());
            String encrypted = CryptoService.encrypt(jsonBytes, key);
            result.setData(coerce(encrypted));
            response.getHeaders().set("X-Encrypted", "true");
        } catch (Exception e) {
            log.error("响应加密失败: {}", e.getMessage(), e);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T coerce(String value) {
        return (T) value;
    }
}
