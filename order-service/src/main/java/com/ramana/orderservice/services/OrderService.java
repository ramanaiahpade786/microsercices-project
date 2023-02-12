package com.ramana.orderservice.services;

import com.ramana.orderservice.dtos.InventoryResponse;
import com.ramana.orderservice.dtos.OrderLineItemDto;
import com.ramana.orderservice.dtos.OrderRequest;
import com.ramana.orderservice.models.Order;
import com.ramana.orderservice.models.OrderLineItem;
import com.ramana.orderservice.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItems(orderRequest.getOrderLineItemDto()
                .stream()
                .map(this::mapToDto)
                .toList());

        List<String> skuCodes = order.getOrderLineItems().stream()
                .map(OrderLineItem::getSkuCode)
                .toList();

        //call Inventory service and place order is product is available
        InventoryResponse[] inventoryResponseArray = webClient.get()
                .uri("http://localhost:8089/api/inventory", uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                .retrieve().bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductsInStock = false;
        if (inventoryResponseArray != null && inventoryResponseArray.length != 0) {
            allProductsInStock = Arrays.stream(inventoryResponseArray).allMatch(InventoryResponse::isInStock);
        }

        if (allProductsInStock)
            orderRepository.save(order);
        else
            throw new RuntimeException("Product is not in stock, please try again later");
    }

    private OrderLineItem mapToDto(OrderLineItemDto orderLineItemDto) {
        return OrderLineItem.builder()
                .price(orderLineItemDto.getPrice())
                .quantity(orderLineItemDto.getQuantity())
                .skuCode(orderLineItemDto.getSkuCode())
                .build();
    }
}
