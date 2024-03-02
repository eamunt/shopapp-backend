package com.project.shopapp.controller;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.OrderResponse;
import com.project.shopapp.services.IOrderService;
import com.project.shopapp.services.IUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@AllArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final IUserService userService;
    @PostMapping("")
    public ResponseEntity<?> createOrder(
            @RequestBody @Valid OrderDTO orderDTO,
            BindingResult result) {
        try {
            if(result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(fieldError -> fieldError.getDefaultMessage())
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            OrderResponse newOrder = orderService.createOrder(orderDTO);
            return ResponseEntity.ok().body(newOrder);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{user_id}")
    // GET: http://localhost:8088/api/v1/orders/user/4
    public ResponseEntity<?> getUserOrders(@Valid @PathVariable("user_id") Long userId) {
        try {
            User existingUser = userService.findUserById(userId);
            List<OrderResponse> orderList = orderService.findByUserId(existingUser);
            return ResponseEntity.ok().body(orderList);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{orderId}")
    // GET: http://localhost:8088/api/v1/orders/4
    public ResponseEntity<?> getOrder(@Valid @PathVariable("orderId") Long orderId) {
        try {
            OrderResponse existingOrder = orderService.getOrder(orderId);
            return ResponseEntity.ok().body(existingOrder);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Put
    @PutMapping("/{idOrder}")
    // adming update order: money, address...
    public ResponseEntity<?> updateOrder(
            @Valid @PathVariable("idOrder") Long id,
            @Valid @RequestBody OrderDTO orderDTO
    ){
        try {
            OrderResponse updatedOrder = orderService.updateOrder(id, orderDTO);
            return ResponseEntity.ok().body(updatedOrder);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Delete
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(
            @Valid @PathVariable("orderId") Long orderId
    ){
        orderService.deleteOrder(orderId);
        // update status field => false.
        return ResponseEntity.ok().body("Delete successfully");
    }

}
