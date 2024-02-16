package com.project.shopapp.controller;

import com.project.shopapp.dtos.OrderDetailDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/order_details")
public class OrderDetailController {
    // add new order details
    @PostMapping
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO newOrderDetail
    ){
        return ResponseEntity.ok("create order detail successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        return ResponseEntity.ok("get order detail with id " + id);
    }

    // get list of ordersDetail from order id
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @Valid @PathVariable("orderId") Long orderId
    ){
        return ResponseEntity.ok("getOrderDetails with orderId  = " + orderId);
    }

    // update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO newOrderDetailData
            ){
        return ResponseEntity.ok("updateOrderDetail with id = " + id
                + ", newOrderDetailData " + newOrderDetailData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(
            @Valid @PathVariable("id") Long id
    ){
        return ResponseEntity.noContent().build();
    }
}
