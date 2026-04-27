package com.lin.controller;

import com.lin.common.result.Result;
import com.lin.core.exception.BusinessException;
import com.lin.model.vo.BigNumberTestVO;
import com.lin.model.vo.DateTestVO;
import com.lin.ratelimiter.annotation.RateLimiter;
import com.lin.ratelimiter.enums.LimitType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "固定测试接口", description = "已预置测试数据，直接调用即可验证框架功能")
@RestController
@RequestMapping("/test/fixed")
public class TestFixedController {

    // ──────────────────── 统一返回 & 异常处理 ────────────────────

    @Operation(summary = "成功返回", description = "验证统一返回格式 Result<T>")
    @GetMapping("/success")
    public Result<Map<String, Object>> success() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是固定测试数据");
        data.put("timestamp", System.currentTimeMillis());
        return Result.success(data);
    }

    @Operation(summary = "业务异常", description = "验证 BusinessException → GlobalExceptionHandler")
    @GetMapping("/business-error")
    public Result<Void> businessError() {
        throw new BusinessException("固定测试：主动抛出的业务异常");
    }

    @Operation(summary = "系统异常", description = "验证未知异常被 GlobalExceptionHandler 兜底处理")
    @GetMapping("/system-error")
    public Result<Void> systemError() {
        int a = 1 / 0;
        return Result.success();
    }

    // ──────────────────── 大数字序列化 ────────────────────

    @Operation(summary = "大数字序列化", description = "Long/BigInteger/BigDecimal 超出 JS 安全范围时序列化为字符串")
    @GetMapping("/big-number")
    public Result<BigNumberTestVO> bigNumber() {
        BigNumberTestVO vo = BigNumberTestVO.builder()
                .overflowLong(9007199254740992L)      // > MAX_SAFE_INTEGER，→ 字符串
                .underflowLong(-9007199254740992L)    // < MIN_SAFE_INTEGER，→ 字符串
                .normalLong(100L)                      // 安全范围内，→ 数字
                .bigInteger(new BigInteger("12345678901234567890"))  // → 字符串
                .bigDecimal(new BigDecimal("12345678901234567890.12345")) // → 字符串
                .maxSafeInteger(9007199254740991L)     // JS 最大安全整数参考
                .minSafeInteger(-9007199254740991L)    // JS 最小安全整数参考
                .build();
        return Result.success(vo);
    }

    // ──────────────────── 日期格式化 ────────────────────

    @Operation(summary = "日期格式化", description = "LocalDateTime 按 yyyy-MM-dd HH:mm:ss 序列化，Date 序列化为时间戳")
    @GetMapping("/datetime")
    public Result<DateTestVO> datetime() {
        DateTestVO vo = DateTestVO.builder()
                .localDateTime(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
                .date(new Date())
                .note("LocalDateTime → 'yyyy-MM-dd HH:mm:ss'，Date → 时间戳（数字）")
                .build();
        return Result.success(vo);
    }

    // ──────────────────── 限流测试 ────────────────────

    @Operation(summary = "限流测试（默认）", description = "10秒内最多2次，超出返回 '请求过于频繁'")
    @RateLimiter(time = 10, count = 2, message = "固定测试：请求太频繁，请稍后再试")
    @GetMapping("/rate-limit")
    public Result<String> rateLimit() {
        return Result.success("请求成功 — 限流未触发");
    }

    @Operation(summary = "IP 限流测试", description = "按客户端 IP 限流，10秒内每 IP 最多2次")
    @RateLimiter(time = 10, count = 2, limitType = LimitType.IP, message = "固定测试：此 IP 请求太频繁")
    @GetMapping("/rate-limit-ip")
    public Result<String> rateLimitIp() {
        return Result.success("请求成功 — IP 限流未触发");
    }
}
