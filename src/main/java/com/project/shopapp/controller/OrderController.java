package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.order.OrderListResponse;
import com.project.shopapp.responses.order.OrderResponse;
import com.project.shopapp.services.order.IOrderService;
import com.project.shopapp.services.user.IUserService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/orders")
@AllArgsConstructor
public class OrderController {
    private final IOrderService orderService;
    private final IUserService userService;
    private final LocalizationUtils localizationUtils;
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteOrder(
            @Valid @PathVariable("orderId") Long orderId
    ){
        orderService.deleteOrder(orderId);
        // update status field => false.
        return ResponseEntity.ok().body(localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY, String.valueOf(orderId)));
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<OrderListResponse> getOrdersByKeyword(@RequestParam(defaultValue = "") String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int limit
    ){// create Pageable từ thông tin page và limit
        // sort: newest on top.
        PageRequest pageRequest = PageRequest.of(page, limit,
//                Sort.by("createdAt").descending());
                Sort.by("id").ascending());

        Page<OrderResponse> orderPage = orderService.getOrdersByKeyword(keyword, pageRequest);
        // get total of pages
        int totalPages = orderPage.getTotalPages();
        List<OrderResponse> orders = orderPage.getContent();

        return ResponseEntity.ok(OrderListResponse
                .builder()
                .orders(orders)
                .totalPages(totalPages)
                .build());
    }

}
