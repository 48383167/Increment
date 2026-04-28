package com.lin.security.provider;

import com.lin.security.config.SecurityProperties;
import com.lin.security.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
public class JwtTokenProvider implements TokenProvider {

    private final SecretKey key;
    private final long expireSeconds;

    public JwtTokenProvider(SecurityProperties properties) {
        this.expireSeconds = properties.getToken().getExpireSeconds();
        this.key = buildKey(properties.getToken().getSecret());
        if (properties.getToken().getSecret().isBlank()) {
            log.warn("lin.security.token.secret 未配置，使用随机密钥。令牌在重启后将失效，生产环境请务必配置固定密钥！");
        }
    }

    private SecretKey buildKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            try {
                MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                keyBytes = sha256.digest(keyBytes);
            } catch (Exception e) {
                throw new RuntimeException("JWT 密钥初始化失败", e);
            }
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String createToken(LoginUser user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getUserId())
                .claim("username", user.getUsername())
                .claim("permissions", user.getPermissions() != null
                        ? new ArrayList<>(user.getPermissions())
                        : Collections.emptyList())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)
                .compact();
    }

    @Override
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        LoginUser user = new LoginUser();
        user.setUserId(claims.getSubject());
        user.setUsername(claims.get("username", String.class));

        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get("permissions", List.class);
        user.setPermissions(permissions != null ? new HashSet<>(permissions) : Collections.emptySet());

        return user;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.debug("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }
}
