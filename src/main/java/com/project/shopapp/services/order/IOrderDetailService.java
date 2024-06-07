package com.project.shopapp.services.order;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.responses.OrderDetailResponse;

import java.util.List;

public interface IOrderDetailService {
    OrderDetailResponse createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception;
    OrderDetailResponse getOrderDetail(Long id) throws Exception;
    OrderDetailResponse updateOrderDetail(Long id,OrderDetailDTO orderDetailDTO) throws Exception;
    void deleteOrderDetail(Long id);
    List<OrderDetailResponse> findByOrderId(Long orderId) throws Exception;
}
