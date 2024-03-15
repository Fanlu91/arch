package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.demo.api.Order;
import com.flhai.myrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@MyProvider
public class OrderServiceImpl implements OrderService {
@Override
    public Order findOrderById(Integer id) {
        Order order = new Order(id.longValue(), 10.0f);
        return order;
    }
}
