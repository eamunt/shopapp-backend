package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrder(Long orderId);
    OrderResponse updateOrder(Long orderId, OrderDTO order);
    void deleteOrder(Long orderId);
    List<OrderResponse> getAllOrders(Long userId);
}
