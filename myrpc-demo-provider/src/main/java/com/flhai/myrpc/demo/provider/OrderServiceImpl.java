package com.flhai.myrpc.demo.provider;

import com.flhai.myrpc.core.annotation.MyProvider;
import com.flhai.myrpc.core.api.RpcException;
import com.flhai.myrpc.demo.api.Order;
import com.flhai.myrpc.demo.api.OrderService;
import org.springframework.stereotype.Component;

@Component
@MyProvider
public class OrderServiceImpl implements OrderService {
    @Override
    public Order findOrderById(Integer id) {
        if (id == 404) {
            throw new RpcException("order not found");
        }
        Order order = new Order(id.longValue(), 10.0f);
        return order;
    }
}
