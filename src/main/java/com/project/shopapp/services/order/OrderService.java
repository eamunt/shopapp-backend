package com.project.shopapp.services.order;

import com.project.shopapp.dtos.CartItemDTO;
import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.order.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderDTO orderDTO) throws Exception {
        User user = userRepository
                .findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id " + orderDTO.getUserId()));
        // convert orderDTO -> order
        // sử dụng library ModelMapper
        // Tạo một luồng bằng ánh xạ riêng để kiểm soát việc ánh xạ.
        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));
        Order order = new Order();
        modelMapper.map(orderDTO, order);
        order.setUserId(user);
        order.setOrderDate(new Date());
        order.setStatus(OrderStatus.PENDING);
        // check shipping_date >= today
        LocalDate shippingDate = orderDTO.getShippingDate() == null
                ? null : orderDTO.getShippingDate();

        if (shippingDate != null && shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);
        order.setTotalMoney(orderDTO.getTotalMoney());
        // save to DB
        orderRepository.save(order);

        // create OrderDetail from CartItemDTO
        List<OrderDetail> orderDetails = new ArrayList<>();
        for(CartItemDTO cartItemDTO : orderDTO.getCartItems()){
            OrderDetail orderDetail = new OrderDetail();

            // get data from cartItemDTO
            Long productId = cartItemDTO.getProductId();
            int quantity = cartItemDTO.getQuantity();
            Float totalMoney = cartItemDTO.getTotalMoney();


            // find info product
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new DataNotFoundException("Product not found with id " + productId));
            orderDetail.setProductId(product);
            orderDetail.setNumberOfProducts(quantity);
            orderDetail.setPrice(product.getPrice());
            orderDetail.setTotalMoney(totalMoney);
            orderDetail.setOrderId(order);
            orderDetails.add(orderDetail);

        }
        // save to OrderDetail
        orderDetailRepository.saveAll(orderDetails);

        // Configure the mapping between Order and OrderResponse
        modelMapper.typeMap(Order.class, OrderResponse.class);
        OrderResponse orderResponse = new OrderResponse();
        modelMapper.map(order, orderResponse);
        orderResponse.setCartTems(orderDTO.getCartItems());

        return orderResponse;

    }

    @Override
    public OrderResponse getOrder(Long orderId) throws Exception {
        modelMapper.typeMap(Order.class, OrderResponse.class);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order with id: " + orderId + " not found"));
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Long orderId, OrderDTO orderDTO) throws Exception{
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order with id: " + orderId + " not found"));
        User existingUser = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Order with id: " + orderId + " not found"));

        modelMapper.typeMap(OrderDTO.class, Order.class)
                .addMappings(mapper -> mapper.skip(Order::setId));

        modelMapper.map(orderDTO, existingOrder);
        existingOrder.setUserId(existingUser);
        orderRepository.save(existingOrder);
        modelMapper.typeMap(Order.class, OrderResponse.class);
        return modelMapper.map(existingOrder, OrderResponse.class);
        
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        // soft-delete not hard-delete
        Order existingOrder = orderRepository.findById(orderId)
                .orElse(null);
        if(existingOrder != null){
            existingOrder.setActive(false);
            orderRepository.save(existingOrder);
        }
    }

    @Override
    public List<OrderResponse> findByUserId(User userId) {
        modelMapper.typeMap(Order.class, OrderResponse.class);
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponse.class))
                .toList();
    }

    @Override
    public Page<OrderResponse> getOrdersByKeyword(String keyword, Pageable pageable) {
        Page<Order> ordersPage;
        try {
            ordersPage = orderRepository.findByKeyword(keyword, pageable);

            TypeMap<Order, OrderResponse> typeMap = modelMapper.typeMap(Order.class, OrderResponse.class);
            typeMap.addMappings(mapper -> {
                // Add a custom converter for the createdDate field
                mapper.using(localDateTimeToDateConverter).map(Order::getCreatedAt, OrderResponse::setCreatedAt);
                mapper.using(localDateTimeToDateConverter).map(Order::getUpdatedAt, OrderResponse::setUpdatedAt);

            });

            return ordersPage.map(order -> modelMapper.map(order, OrderResponse.class));
            // hoặc
            // return productsPage.map(ProductResponse::fromProduct);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    Converter<LocalDateTime, Date> localDateTimeToDateConverter = context -> {
        LocalDateTime source = context.getSource();
        return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
    };
}
