package com.lin.admin.manager.order;

import com.lin.admin.model.order.OrderRequest;
import com.lin.admin.model.order.OrderVO;
import com.lin.admin.service.inventory.InventoryService;
import com.lin.admin.service.order.OrderService;
import com.lin.admin.service.payment.PaymentService;
import com.lin.admin.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderManager {

    private final UserService userService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    /**
     * 下单编排：校验用户 → 锁定库存 → 创建订单 → 处理支付。
     * 每一步都是原子 Service，Manager 只负责编排顺序和事务边界。
     */
    public OrderVO createOrder(OrderRequest request) {
        // 1. 校验用户
        userService.checkUser(request.getUserId());

        // 2. 检查并锁定库存
        InventoryService.Stock stock = inventoryService.checkAndLock(
                request.getProductId(), request.getQuantity());

        // 3. 创建订单
        OrderVO order = orderService.createOrder(
                request.getUserId(), stock.name, request.getQuantity(), stock.price);

        // 4. 处理支付
        paymentService.processPayment(order.getOrderId(), order.getTotalAmount());

        log.info("下单编排完成: {}", order.getOrderId());
        return order;
    }
}
