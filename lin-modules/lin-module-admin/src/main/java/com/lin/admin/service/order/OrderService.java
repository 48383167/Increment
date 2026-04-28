package com.lin.admin.service.order;

import com.lin.admin.model.order.OrderVO;
import com.lin.admin.repository.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderVO createOrder(String userId, String productName, int quantity, BigDecimal unitPrice) {
        OrderVO order = OrderVO.builder()
                .orderId(UUID.randomUUID().toString().substring(0, 8))
                .userId(userId)
                .productName(productName)
                .quantity(quantity)
                .totalAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .status("已创建")
                .createTime(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        log.info("订单创建: {}", order.getOrderId());
        return order;
    }
}
