package com.lin.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
@Schema(description = "大数字序列化测试响应")
public class BigNumberTestVO {

    @Schema(description = "超出 JS 安全最大值的长整数 → 应序列化为字符串", example = "9007199254740992")
    private Long overflowLong;

    @Schema(description = "超出 JS 安全最小值的负长整数 → 应序列化为字符串", example = "-9007199254740992")
    private Long underflowLong;

    @Schema(description = "JS 安全范围内的长整数 → 保持数字类型", example = "100")
    private Long normalLong;

    @Schema(description = "BigInteger → 始终序列化为字符串", example = "12345678901234567890")
    private BigInteger bigInteger;

    @Schema(description = "BigDecimal → 始终序列化为字符串", example = "12345678901234567890.12345")
    private BigDecimal bigDecimal;

    @Schema(description = "JS Number.MAX_SAFE_INTEGER 参考值", example = "9007199254740991")
    private Long maxSafeInteger;

    @Schema(description = "JS Number.MIN_SAFE_INTEGER 参考值", example = "-9007199254740991")
    private Long minSafeInteger;
}
