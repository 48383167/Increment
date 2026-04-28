package com.lin.admin.model.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "订单响应")
public class OrderVO {

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
