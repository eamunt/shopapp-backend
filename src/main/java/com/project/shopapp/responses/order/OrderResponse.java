package com.project.shopapp.responses.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.models.BaseEntity;
import com.project.shopapp.models.OrderDetail;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class OrderResponse {
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("fullname")
    private String fullName;

    private String email;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private String address;

    private String note;

    @JsonProperty("order_date")
    private Date orderDate;

    private String status;

    @JsonProperty("total_money")
    private Float totalMoney;

    @JsonProperty("shipping_method")
    private String shippingMethod;

    @JsonProperty("shipping_address")
    private String shippingAddress;

    @JsonProperty("shipping_date")
    private Date shippingDate;

    @JsonProperty("tracking_number")
    private String trackingNumber;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("active")
    private Boolean active; // thuộc về admin

    @JsonProperty("created_at")
    private Date createdAt;

    @JsonProperty("updated_at")
    private Date updatedAt;

    @JsonProperty("order_details")
    private List<OrderDetail> orderDetails;

    @JsonProperty("cart_tems")
    private List<CartItemDTO> cartTems;


}
