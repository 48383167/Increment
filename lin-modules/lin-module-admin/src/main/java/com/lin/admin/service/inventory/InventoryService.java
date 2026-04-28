package com.lin.admin.service.inventory;

import com.lin.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InventoryService {

    private final Map<String, Stock> stockMap = new ConcurrentHashMap<>();

    public InventoryService() {
        stockMap.put("P001", new Stock("键盘", 100, new BigDecimal("299.00")));
        stockMap.put("P002", new Stock("鼠标", 200, new BigDecimal("149.00")));
    }

    public Stock checkAndLock(String productId, int quantity) {
        Stock stock = stockMap.get(productId);
        if (stock == null) {
            throw new BusinessException("商品不存在: " + productId);
        }
        if (stock.available < quantity) {
            throw new BusinessException("库存不足，剩余: " + stock.available);
        }
        stock.available -= quantity;
        log.info("库存锁定: {} x{} -> 剩余 {}", productId, quantity, stock.available);
        return stock;
    }

    public static class Stock {
        public final String name;
        public final BigDecimal price;
        public int available;

        Stock(String name, int available, BigDecimal price) {
            this.name = name;
            this.available = available;
            this.price = price;
        }
    }
}
