package com.lin.controller;

import com.lin.common.result.Result;
import com.lin.security.annotation.Anonymous;
import com.lin.security.annotation.RequirePermission;
import com.lin.security.context.LoginUser;
import com.lin.security.context.UserContext;
import com.lin.security.provider.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Tag(name = "认证模块", description = "登录与权限验证")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenProvider tokenProvider;

    public AuthController(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Anonymous
    @Operation(summary = "登录", description = "演示登录接口，返回 JWT 令牌")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest request) {
        LoginUser user = new LoginUser();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPermissions(Set.of("user:read", "user:write"));

        String token = tokenProvider.createToken(user);
        return Result.success(Map.of("token", token));
    }

    @Operation(summary = "获取当前用户信息", description = "需要有效的 JWT 令牌")
    @GetMapping("/info")
    public Result<LoginUser> info() {
        return Result.success(UserContext.get());
    }

    @Operation(summary = "需要写权限", description = "演示 @RequirePermission 权限控制")
    @RequirePermission("user:write")
    @GetMapping("/write")
    public Result<String> write() {
        return Result.success("你有写权限，可以执行此操作");
    }

    @Data
    static class LoginRequest {
        private String username;
        private String password;
    }
}
