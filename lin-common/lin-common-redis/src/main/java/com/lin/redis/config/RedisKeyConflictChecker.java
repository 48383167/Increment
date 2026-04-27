package com.lin.redis.config;

import com.lin.common.redis.prefix.RedisKeyPrefixProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis Key 前缀冲突自动检测器。
 * <p>
 * 在 Spring 容器刷新完成后，收集所有实现了 {@link RedisKeyPrefixProvider} 的 Bean，
 * 检查各模块的 Key 前缀是否有重复。如有冲突，日志输出 ERROR 级别告警。
 * <p>
 * 检测逻辑不阻塞启动——冲突不合理的键可能导致运行时覆盖，但不会让应用起不来。
 */
@Slf4j
@Component
public class RedisKeyConflictChecker {

    @Autowired(required = false)
    private List<RedisKeyPrefixProvider> providers;

    @EventListener(ContextRefreshedEvent.class)
    public void check() {
        if (providers == null || providers.isEmpty()) {
            log.debug("未发现任何 RedisKeyPrefixProvider 实现，跳过 Key 冲突检测");
            return;
        }

        log.info("检测到 {} 个 Redis Key 前缀提供者，开始冲突检测", providers.size());
        Set<String> seen = new HashSet<>();
        boolean conflict = false;

        for (RedisKeyPrefixProvider provider : providers) {
            String prefix = provider.getPrefix();
            String className = provider.getClass().getName();

            if (!seen.add(prefix)) {
                conflict = true;
                String offenders = providers.stream()
                        .filter(p -> p.getPrefix().equals(prefix))
                        .map(p -> p.getClass().getName())
                        .collect(Collectors.joining(", "));
                log.error(
                    "【Redis Key 前缀冲突】前缀 '{}' 被以下多个模块同时使用: [{}]。"
                        + "请修改其中一个模块的 PREFIX 常量，避免运行时 Key 覆盖。",
                    prefix, offenders);
            } else {
                log.info("  ✓ 前缀 '{}' → {}", prefix, className);
            }
        }

        if (conflict) {
            log.error("⚠ Redis Key 前缀冲突检测到问题，请尽快修复");
        } else {
            log.info("✓ Redis Key 前缀冲突检测通过，共 {} 个模块", providers.size());
        }
    }
}
