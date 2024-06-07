package com.project.shopapp.services.product.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.responses.OrderDetailResponse;
import com.project.shopapp.services.order.OrderDetailService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@AllArgsConstructor
public class OrderDetailController {
    private final OrderDetailService orderDetailService;
    private final LocalizationUtils localizationUtils;
    // add new order details
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
    ){
        try {
            OrderDetailResponse newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
            return ResponseEntity.ok().body(newOrderDetail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id
    ) throws Exception {
        OrderDetailResponse orderDetailResponse = orderDetailService.getOrderDetail(id);
        return ResponseEntity.ok(orderDetailResponse);
    }

    // get list of ordersDetail from order id
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> findByOrderId(
            @Valid @PathVariable("orderId") Long orderId
    ) throws Exception {
        List<OrderDetailResponse> orderDetailResponse = orderDetailService.findByOrderId(orderId);
        return ResponseEntity.ok(orderDetailResponse);
    }

    // update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO
            ) throws Exception {

        return ResponseEntity.ok(orderDetailService.updateOrderDetail(id, orderDetailDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<String> deleteOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        try {
            orderDetailService.deleteOrderDetail(id);
            return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAILS_SUCCESSFULLY, String.valueOf(id)));
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
