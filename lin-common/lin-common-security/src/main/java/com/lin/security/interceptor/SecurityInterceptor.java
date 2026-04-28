package com.lin.security.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lin.common.result.Result;
import com.lin.common.result.ResultCode;
import com.lin.security.annotation.Anonymous;
import com.lin.security.config.SecurityProperties;
import com.lin.security.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Slf4j
public class SecurityInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final SecurityProperties properties;

    public SecurityInterceptor(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 静态资源 / 非控制器请求直接放行
        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        // 白名单路径放行
        String path = request.getRequestURI();
        if (properties.getWhitelist().stream().anyMatch(p -> pathMatcher.match(p, path))) {
            return true;
        }

        // @Anonymous 在方法上 → 放行，在类上 → 放行
        if (hm.hasMethodAnnotation(Anonymous.class) ||
                hm.getBeanType().isAnnotationPresent(Anonymous.class)) {
            return true;
        }

        // 已认证（TokenAuthenticationFilter 设置了 UserContext）→ 放行
        if (UserContext.get() != null) {
            return true;
        }

        // 未认证 → 返回 401
        writeJson(response, ResultCode.UNAUTHORIZED.getCode(),
                ResultCode.UNAUTHORIZED.getMsg());
        return false;
    }

    private void writeJson(HttpServletResponse response, Integer code, String msg) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            response.getWriter().write(
                    OBJECT_MAPPER.writeValueAsString(Result.error(code, msg)));
        } catch (Exception e) {
            log.error("写入安全拦截器响应失败", e);
        }
    }
}
