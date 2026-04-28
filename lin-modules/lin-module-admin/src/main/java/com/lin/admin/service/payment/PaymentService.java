package com.lin.admin.service.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentService {

    public void processPayment(String orderId, BigDecimal amount) {
        log.info("支付处理: 订单={} 金额={}", orderId, amount);
    }
}
