package com.lin.admin.controller;

import com.lin.admin.model.dto.JacksonTestDTO;
import com.lin.common.result.Result;
import com.lin.crypto.annotation.EncryptResponse;
import com.lin.ratelimiter.annotation.RateLimiter;
import com.lin.ratelimiter.enums.LimitType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "灵活测试接口", description = "可自由传入参数，验证框架在不同输入下的表现")
@RestController
@RequestMapping("/test/flexible")
public class TestFlexibleController {

    // ──────────────────── 统一返回 ────────────────────

    @Operation(summary = "统一返回格式", description = "传入任意数据，验证 Result<T> 包装后的返回格式")
    @PostMapping("/success")
    public Result<Map<String, Object>> success(@RequestBody Map<String, Object> data) {
        return Result.success(data);
    }

    // ──────────────────── 参数校验 ────────────────────

    @Operation(summary = "参数校验", description = "测试 @NotNull / @NotBlank 等 Jakarta Validation 校验")
    @EncryptResponse
    @PostMapping("/validate")
    public Result<JacksonTestDTO> validate(@Validated @RequestBody JacksonTestDTO dto) {
        return Result.success(dto);
    }

    // ──────────────────── Jackson 序列化测试 ────────────────────

    @Operation(summary = "大数字序列化", description = "传入 Long/BigInteger/BigDecimal，查看序列化结果是否按安全范围处理")
    @PostMapping("/big-number")
    public Result<Map<String, Object>> bigNumber(
            @Parameter(description = "长整数", example = "9007199254740992")
            @RequestParam(required = false) Long bigLong,
            @Parameter(description = "大整数", example = "12345678901234567890")
            @RequestParam(required = false) BigInteger bigInteger,
            @Parameter(description = "精确小数", example = "12345678901234567890.12345")
            @RequestParam(required = false) BigDecimal bigDecimal,
            @Parameter(description = "普通长整数", example = "100")
            @RequestParam(required = false) Long normalLong) {

        Map<String, Object> data = new HashMap<>();
        data.put("bigLong", bigLong);
        data.put("bigInteger", bigInteger);
        data.put("bigDecimal", bigDecimal);
        data.put("normalLong", normalLong);
        data.put("jsMaxSafeInteger", 9007199254740991L);
        data.put("description", "超出 JS 安全范围的 Long/BigInteger/BigDecimal 应序列化为字符串");
        return Result.success(data);
    }

    // ──────────────────── 日期反序列化测试 ────────────────────

    @Operation(summary = "日期反序列化", description = "传入不同格式的日期字符串，测试 CustomDateDeserializer 的多格式解析能力")
    @PostMapping("/date-deserialize")
    public Result<Map<String, Object>> dateDeserialize(
            @Parameter(description = "日期字符串（支持多种格式）", example = "2025-01-01 12:00:00")
            @RequestParam String dateStr) {

        JacksonTestDTO dto = new JacksonTestDTO();
        dto.setDate(new Date()); // 实际反序列化由 Jackson 在 @RequestBody 场景触发
        // 这里使用 @RequestParam 接收字符串，直接回显以验证格式

        Map<String, Object> data = new HashMap<>();
        data.put("input", dateStr);
        data.put("tip", "使用 POST /test/flexible/validate 并传 date 字段来测试完整的 Jackson 反序列化链路");
        return Result.success(data);
    }

    @Operation(summary = "完整 Jackson 反序列化", description = "POST JSON body 中的 date 字段经过 CustomDateDeserializer 解析")
    @PostMapping("/jackson-full-test")
    public Result<JacksonTestDTO> jacksonFullTest(@Validated @RequestBody JacksonTestDTO dto) {
        // JacksonTestDTO 中 date 字段会经过 CustomDateDeserializer 处理
        // bigLong/bigInteger/bigDecimal 会在返回时经过 BigNumberSerializer 处理
        // jsonStr/jsonArrayStr/jsonAnyStr 会经过 @JsonPattern 校验
        return Result.success(dto);
    }

    // ──────────────────── 限流测试 ────────────────────

    @Operation(summary = "限流测试（自定义 key）", description = "通过 key 参数设置限流维度，10秒内最多3次")
    @RateLimiter(key = "#key", time = 10, count = 3)
    @GetMapping("/rate-limit")
    public Result<String> rateLimit(
            @Parameter(description = "限流 key，相同 key 共享限流计数", example = "user-001")
            @RequestParam String key) {
        return Result.success("key=" + key + " — 限流未触发");
    }

    @Operation(summary = "IP 限流测试（动态）", description = "按请求方 IP 进行限流，10秒内每 IP 最多2次")
    @RateLimiter(time = 10, count = 2, limitType = LimitType.IP)
    @GetMapping("/rate-limit-ip")
    public Result<String> rateLimitIp() {
        return Result.success("当前 IP 限流未触发");
    }
}
