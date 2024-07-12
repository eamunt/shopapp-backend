package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.responses.order.OrderDetailResponse;
import com.project.shopapp.services.order.OrderDetailService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseObject> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO orderDetailDTO
    )throws Exception{
        OrderDetailResponse newOrderDetail = orderDetailService.createOrderDetail(orderDetailDTO);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Order detail created")
                        .status(HttpStatus.CREATED)
                        .data(newOrderDetail)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getOrderDetail(
            @Valid @PathVariable("id") Long id
    ) throws Exception {
        OrderDetailResponse orderDetailResponse = orderDetailService.getOrderDetail(id);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get order Detail successfull")
                        .status(HttpStatus.OK)
                        .data(orderDetailResponse)
                .build());
    }

    // get list of ordersDetail from order id
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ResponseObject> findByOrderId(
            @Valid @PathVariable("orderId") Long orderId
    ) throws Exception {
        List<OrderDetailResponse> orderDetailResponse = orderDetailService.findByOrderId(orderId);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get order detail by Order Id successfull")
                        .status(HttpStatus.OK)
                        .data(orderDetailResponse)
                .build());
    }

    // update
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO orderDetailDTO
            ) throws Exception {

        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Update order detail successfully")
                        .status(HttpStatus.OK)
                        .data(orderDetailService.updateOrderDetail(id, orderDetailDTO))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> deleteOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        orderDetailService.deleteOrderDetail(id);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_DETAILS_SUCCESSFULLY, String.valueOf(id)))
                        .status(HttpStatus.OK)
                        .data(null)
                .build());
    }
}
