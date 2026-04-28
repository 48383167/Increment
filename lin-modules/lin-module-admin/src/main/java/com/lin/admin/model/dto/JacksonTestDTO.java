package com.lin.admin.model.dto;

import com.lin.jackson.validate.JsonPattern;
import com.lin.jackson.validate.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@Schema(description = "Jackson 功能测试入参")
public class JacksonTestDTO {

    @Schema(description = "Long 类型数字（超出 JS 安全范围会序列化为字符串）", example = "9007199254740992")
    private Long bigLong;

    @Schema(description = "BigInteger 类型数字", example = "12345678901234567890")
    private BigInteger bigInteger;

    @Schema(description = "BigDecimal 类型数字", example = "12345678901234567890.12345")
    private BigDecimal bigDecimal;

    @Schema(description = "在 JS 安全范围内的 Long", example = "100")
    private Long normalLong;

    @Schema(description = "Date 类型日期（支持多种格式反序列化）", example = "2025-01-01 12:00:00")
    private Date date;

    @Schema(description = "JSON 字符串（会被 @JsonPattern 校验格式）", example = "{\"key\":\"value\"}")
    @JsonPattern(type = JsonType.OBJECT, message = "必须是合法的 JSON 对象字符串")
    private String jsonStr;

    @Schema(description = "JSON 字符串（校验为数组格式）", example = "[1,2,3]")
    @JsonPattern(type = JsonType.ARRAY, message = "必须是合法的 JSON 数组字符串")
    private String jsonArrayStr;

    @Schema(description = "JSON 字符串（校验为任意合法 JSON）", example = "{\"a\":1}")
    @JsonPattern(type = JsonType.ANY, message = "必须是合法的 JSON 字符串")
    private String jsonAnyStr;

    @Schema(description = "必填字段，用于测试 @NotNull", example = "hello")
    @NotNull(message = "requiredField 不能为空")
    private String requiredField;
}
