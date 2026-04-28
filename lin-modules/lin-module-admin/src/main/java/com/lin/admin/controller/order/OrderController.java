package com.lin.admin.controller.order;

import com.lin.admin.manager.order.OrderManager;
import com.lin.admin.model.order.OrderRequest;
import com.lin.admin.model.order.OrderVO;
import com.lin.admin.repository.order.OrderRepository;
import com.lin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "订单管理", description = "演示 Controller → Manager → Service → Repository 四层架构")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderManager orderManager;
    private final OrderRepository orderRepository;

    @Operation(summary = "创建订单", description = "演示 Manager 编排多个 Service")
    @PostMapping("/create")
    public Result<OrderVO> create(@Valid @RequestBody OrderRequest request) {
        return Result.success(orderManager.createOrder(request));
    }

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public Result<List<OrderVO>> list() {
        return Result.success(orderRepository.findAll());
    }
}
