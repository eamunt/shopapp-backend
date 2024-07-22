package com.project.shopapp.controller;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.models.User;
import com.project.shopapp.responses.ResponseObject;
import com.project.shopapp.responses.order.OrderListResponse;
import com.project.shopapp.responses.order.OrderResponse;
import com.project.shopapp.responses.product.ProductListResponse;
import com.project.shopapp.services.order.IOrderService;
import com.project.shopapp.services.user.IUserService;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseObject> createOrder(
            @RequestBody @Valid OrderDTO orderDTO,
            BindingResult result) throws Exception{
        if(result.hasErrors()) {
            List<String> errorMessages = result.getFieldErrors()
                    .stream()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                            .message(String.join(";", errorMessages))
                            .status(HttpStatus.BAD_REQUEST)
                    .build());
        }
        OrderResponse newOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Insert order successfully")
                        .status(HttpStatus.CREATED)
                        .data(newOrder)
                .build());
    }

    @GetMapping("/user/{user_id}")
    // GET: http://localhost:8088/api/v1/orders/user/4
    public ResponseEntity<ResponseObject> getUserOrders(@Valid @PathVariable("user_id") Long userId) throws Exception {
        User existingUser = userService.findUserById(userId);
        List<OrderResponse> orderList = orderService.findByUserId(existingUser);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get list of orders successfully")
                        .status(HttpStatus.OK)
                        .data(orderList)
                .build());
    }

    @GetMapping("/{orderId}")
    // GET: http://localhost:8088/api/v1/orders/4
    public ResponseEntity<ResponseObject> getOrder(@Valid @PathVariable("orderId") Long orderId) throws Exception{
        OrderResponse existingOrder = orderService.getOrder(orderId);
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message("Get order successfully")
                        .status(HttpStatus.OK)
                        .data(existingOrder)
                .build());
    }

    // Put
    @PutMapping("/{idOrder}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    // adming update order: money, address...
    public ResponseEntity<?> updateOrder(
            @Valid @PathVariable("idOrder") Long id,
            @Valid @RequestBody OrderDTO orderDTO
    ) throws Exception{
        OrderResponse updatedOrder = orderService.updateOrder(id, orderDTO);
        return ResponseEntity.ok().body(new ResponseObject("Update order successfully", HttpStatus.OK, updatedOrder));
    }

    // Delete
    @PutMapping ("delete/{orderId}/{active}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> deleteOrder(
            @Valid @PathVariable("orderId") Long orderId,
            @Valid @PathVariable int active
    ) throws Exception{
        orderService.deleteOrder(orderId, active>0);
        // update status field => false.
        String message  = active > 0 ? "Successfully blocked the order" : localizationUtils.getLocalizedMessage(MessageKeys.DELETE_ORDER_SUCCESSFULLY, String.valueOf(orderId));
        return ResponseEntity.ok().body(ResponseObject.builder()
                        .message(message)
                        .status(HttpStatus.OK)
                        .data(null)
                .build());
    }

    @GetMapping("/get-orders-by-keyword")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseObject> getOrdersByKeyword(@RequestParam(defaultValue = "") String keyword,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int limit
    ){// create Pageable từ thông tin page và limit
        // sort: newest on top.
        PageRequest pageRequest = PageRequest.of(page, limit,
//                Sort.by("createdAt").descending());
                Sort.by("id").ascending());
        OrderListResponse orderListResponse;

        Page<OrderResponse> orderPage = orderService.getOrdersByKeyword(keyword, pageRequest);
        // get total of pages
        int totalPages = orderPage.getTotalPages();
        List<OrderResponse> orders = orderPage.getContent();

        orderListResponse = OrderListResponse
                .builder()
                .orders(orders)
                .totalPages(totalPages)
                .build();

        return ResponseEntity.ok(ResponseObject.builder()
                        .message("Get orders successfully")
                        .status(HttpStatus.OK)
                        .data(orderListResponse)
                .build());
    }

}
