package com.lin.admin.repository.order;

import com.lin.admin.model.order.OrderVO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class OrderRepository {

    private final Map<String, OrderVO> store = new ConcurrentHashMap<>();

    public void save(OrderVO order) {
        store.put(order.getOrderId(), order);
    }

    public OrderVO findById(String orderId) {
        return store.get(orderId);
    }

    public List<OrderVO> findAll() {
        return List.copyOf(store.values());
    }
}
