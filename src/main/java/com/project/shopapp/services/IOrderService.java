package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.OrderResponse;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrder(Long orderId) throws Exception;
    OrderResponse updateOrder(Long orderId, OrderDTO order) throws Exception;
    void deleteOrder(Long orderId);
    List<OrderResponse> findByUserId(User userId);
}
