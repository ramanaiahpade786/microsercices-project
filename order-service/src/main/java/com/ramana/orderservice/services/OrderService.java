package com.ramana.orderservice.services;

import com.ramana.orderservice.dtos.OrderLineItemDto;
import com.ramana.orderservice.dtos.OrderRequest;
import com.ramana.orderservice.models.Order;
import com.ramana.orderservice.models.OrderLineItem;
import com.ramana.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItems(orderRequest.getOrderLineItemDto()
                        .stream()
                        .map(this::mapToDto)
                        .toList());

        orderRepository.save(order);

        log.info("Order {} placed successfully ", order.getOrderNumber());
    }

    private OrderLineItem mapToDto(OrderLineItemDto orderLineItemDto) {
        return OrderLineItem.builder()
                .price(orderLineItemDto.getPrice())
                .quantity(orderLineItemDto.getQuantity())
                .skuCode(orderLineItemDto.getSkuCode())
                .build();
    }
}
