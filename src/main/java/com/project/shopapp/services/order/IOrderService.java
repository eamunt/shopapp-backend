package com.project.shopapp.services.order;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.order.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderService {
    OrderResponse createOrder(OrderDTO orderDTO) throws Exception;
    OrderResponse getOrder(Long orderId) throws Exception;
    OrderResponse updateOrder(Long orderId, OrderDTO order) throws Exception;
    void deleteOrder(Long orderId, boolean active) throws Exception;
    List<OrderResponse> findByUserId(User userId);
    Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable);
}
