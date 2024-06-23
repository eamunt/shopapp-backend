package com.project.shopapp.services.order;

import com.project.shopapp.dtos.OrderDetailDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Order;
import com.project.shopapp.models.OrderDetail;
import com.project.shopapp.models.Product;
import com.project.shopapp.repositories.OrderDetailRepository;
import com.project.shopapp.repositories.OrderRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.order.OrderDetailResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderDetailService implements IOrderDetailService{
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ModelMapper modelMapper;
    @Override
    @Transactional
    public OrderDetailResponse createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception {
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found: " + orderDetailDTO.getOrderId()));
        Product existingProduct = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found: "+orderDetailDTO.getProductId()));


        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.typeMap(OrderDetailDTO.class, OrderDetail.class)
                .addMappings(mapper -> {
                    mapper.skip(OrderDetail::setId);
                });

        OrderDetail orderDetail = new OrderDetail();
        modelMapper.map(orderDetailDTO, orderDetail);
        orderDetail.setOrderId(existingOrder);
        orderDetail.setProductId(existingProduct);

        orderDetailRepository.save(orderDetail);

        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        modelMapper.typeMap(OrderDetail.class, OrderDetailResponse.class)
                .addMappings(mapper -> {
                    mapper.skip(OrderDetailResponse::setOrderId);
                    mapper.skip(OrderDetailResponse::setProductId);
                });
        modelMapper.map(orderDetail, orderDetailResponse);
        orderDetailResponse.setOrderId(orderDetail.getOrderId().getId());
        orderDetailResponse.setProductId(orderDetail.getProductId().getId());

        return orderDetailResponse;
    }

    @Override
    public OrderDetailResponse getOrderDetail(Long id) throws Exception {
        modelMapper.typeMap(OrderDetail.class, OrderDetailResponse.class);
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Not found OrderDetail for id " + id));
        return modelMapper.map(orderDetail, OrderDetailResponse.class);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderDetail(
            Long id,
            OrderDetailDTO orderDetailDTO) throws Exception
    {
        OrderDetail existingOrderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("OrderDetail not found " + id));
        Order existingOrder = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException("Order not found " + orderDetailDTO.getOrderId()));
        Product existingProduct = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException("Product not found " + orderDetailDTO.getProductId()));

        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.typeMap(OrderDetailDTO.class, OrderDetail.class)
                        .addMappings(mapper -> {
                            mapper.skip(OrderDetail::setOrderId);
                            mapper.skip(OrderDetail::setProductId);
                        });
        modelMapper.map(orderDetailDTO, existingOrderDetail);
        existingOrderDetail.setOrderId(existingOrder);
        existingOrderDetail.setProductId(existingProduct);

        orderDetailRepository.save(existingOrderDetail);

        OrderDetailResponse newOrderDetailResponse = new OrderDetailResponse();
        modelMapper.typeMap(OrderDetail.class, OrderDetailResponse.class)
                .addMappings(mapper -> {
                    mapper.skip(OrderDetailResponse::setOrderId);
                    mapper.skip(OrderDetailResponse::setProductId);
                });
        modelMapper.map(existingOrderDetail, newOrderDetailResponse);
        newOrderDetailResponse.setOrderId(existingOrderDetail.getOrderId().getId());
        newOrderDetailResponse.setProductId(existingOrderDetail.getProductId().getId());

        return newOrderDetailResponse;
    }

    @Override
    @Transactional
    public void deleteOrderDetail(Long id) {
        try {
            getOrderDetail(id);
            orderDetailRepository.deleteById(id);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<OrderDetailResponse> findByOrderId(Long orderId) throws Exception {
        modelMapper.typeMap(OrderDetail.class, OrderDetailResponse.class);
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new DataNotFoundException("Order not found" + orderId));
        List<OrderDetail> tmp = orderDetailRepository.findByOrderId(existingOrder);
        return tmp.stream()
                .map(orderDetail -> modelMapper.map(orderDetail, OrderDetailResponse.class))
                .toList();
    }
}
