package com.lin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@Schema(description = "日期格式化测试响应")
public class DateTestVO {

    @Schema(description = "LocalDateTime → 序列化为 'yyyy-MM-dd HH:mm:ss' 格式", example = "2025-01-01 12:00:00")
    private LocalDateTime localDateTime;

    @Schema(description = "Date → 序列化为时间戳（Jackson 默认），但自定义反序列化支持多格式", example = "1735689600000")
    private Date date;

    @Schema(description = "描述信息")
    private String note;
}
