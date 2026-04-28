package com.lin.api.controller;

import com.lin.common.result.Result;
import com.lin.security.annotation.Anonymous;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Anonymous
@RestController
@RequestMapping("/api/public")
public class ApiTestController {

    @GetMapping("/hello")
    public Result<Map<String, Object>> hello() {
        return Result.success(Map.of(
                "message", "这是一个对外开放的接口，无需认证",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("API 服务运行正常");
    }
}
