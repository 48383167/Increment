package com.lin.ratelimiter.aspect;

import com.lin.core.exception.BusinessException;
import com.lin.ratelimiter.annotation.RateLimiter;
import com.lin.ratelimiter.config.RateLimiterRedisKeys;
import com.lin.ratelimiter.enums.LimitType;
import com.lin.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RateType;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;

@Slf4j
@Aspect
public class RateLimiterAspect implements ApplicationContextAware {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParserContext parserContext = new TemplateParserContext();
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Before("@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) {
        int time = rateLimiter.time();
        int count = rateLimiter.count();
        int timeout = rateLimiter.timeout();
        try {
            String combineKey = getCombineKey(rateLimiter, point);
            RateType rateType = rateLimiter.limitType() == LimitType.CLUSTER
                ? RateType.PER_CLIENT : RateType.OVERALL;
            long number = RedisUtils.rateLimiter(combineKey, rateType, count, time, timeout);
            if (number == -1) {
                throw new BusinessException(rateLimiter.message());
            }
            log.info("限制令牌 => {}, 剩余令牌 => {}, 缓存key => '{}'", count, number, combineKey);
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new RuntimeException("服务器限流异常，请稍候再试", e);
            }
        }
    }

    private String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
        String key = rateLimiter.key();
        if (key != null && !key.isEmpty() && key.contains("#")) {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method targetMethod = signature.getMethod();
            Object[] args = point.getArgs();
            MethodBasedEvaluationContext context =
                new MethodBasedEvaluationContext(null, targetMethod, args, pnd);
            context.setBeanResolver(new BeanFactoryResolver(applicationContext));
            Expression expression;
            if (key.startsWith(parserContext.getExpressionPrefix())
                && key.endsWith(parserContext.getExpressionSuffix())) {
                expression = parser.parseExpression(key, parserContext);
            } else {
                expression = parser.parseExpression(key);
            }
            try {
                key = expression.getValue(context, String.class);
            } catch (Exception e) {
                log.warn("限流 SpEL 解析失败, key: {}, error: {}", key, e.getMessage());
            }
        }

        HttpServletRequest request = ((ServletRequestAttributes)
            RequestContextHolder.getRequestAttributes()).getRequest();

        StringBuilder sb = new StringBuilder(RateLimiterRedisKeys.KEY_PREFIX);
        sb.append(request.getRequestURI()).append(":");
        if (rateLimiter.limitType() == LimitType.IP) {
            sb.append(getClientIP(request)).append(":");
        } else if (rateLimiter.limitType() == LimitType.CLUSTER) {
            sb.append(RedisUtils.getClientId()).append(":");
        }
        return sb.append(key).toString();
    }

    private String getClientIP(HttpServletRequest request) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
            "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
}
