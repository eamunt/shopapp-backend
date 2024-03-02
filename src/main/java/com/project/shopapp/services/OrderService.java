package com.project.shopapp.services;

import com.project.shopapp.dtos.OrderDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderStatus;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.responses.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
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
                ? LocalDate.now() : orderDTO.getShippingDate();
        if(shippingDate.isBefore(LocalDate.now())){
            throw new DataNotFoundException("Date must be at least today");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true);

        // save to DB
        orderRepository.save(order);

        // Configure the mapping between Order and OrderResponse
        modelMapper.typeMap(Order.class, OrderResponse.class);

        return modelMapper.map(order, OrderResponse.class);

    }

    @Override
    public OrderResponse getOrder(Long orderId) throws Exception {
        modelMapper.typeMap(Order.class, OrderResponse.class);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order with id: " + orderId + " not found"));
        return modelMapper.map(order, OrderResponse.class);
    }

    @Override
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
}
