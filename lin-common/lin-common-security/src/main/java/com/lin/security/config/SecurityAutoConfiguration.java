package com.lin.security.config;

import com.lin.security.aspect.RequirePermissionAspect;
import com.lin.security.filter.TokenAuthenticationFilter;
import com.lin.security.interceptor.SecurityInterceptor;
import com.lin.security.provider.JwtTokenProvider;
import com.lin.security.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnProperty(prefix = "lin.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityAutoConfiguration {

    private final SecurityProperties securityProperties;

    @Bean
    @ConditionalOnMissingBean
    public TokenProvider tokenProvider() {
        return new JwtTokenProvider(securityProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public TokenAuthenticationFilter tokenAuthenticationFilter(TokenProvider tokenProvider) {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequirePermissionAspect requirePermissionAspect() {
        return new RequirePermissionAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                    TokenAuthenticationFilter tokenFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("lin-common-security 已启用，认证由 SecurityInterceptor + @Anonymous 控制");

        return http.build();
    }

    /**
     * 注册 SecurityInterceptor，拦截所有 Controller 方法进行认证检查。
     * TokenAuthenticationFilter 负责解析 token，SecurityInterceptor 负责决定放行还是拒绝。
     */
    @Bean
    @ConditionalOnMissingBean(name = "securityWebMvcConfigurer")
    public WebMvcConfigurer securityWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new SecurityInterceptor(securityProperties))
                        .order(0);
            }
        };
    }
}
