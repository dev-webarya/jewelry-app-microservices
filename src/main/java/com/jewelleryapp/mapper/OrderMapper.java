package com.jewelleryapp.mapper;

import com.jewelleryapp.dto.response.OrderResponseDto;
import com.jewelleryapp.entity.CustomerOrder;
import com.jewelleryapp.entity.OrderItem;
import com.jewelleryapp.entity.OrderStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "statusHistory", source = "statusHistory")
    @Mapping(target = "fulfillmentStoreId", source = "fulfillmentStore.id")
    @Mapping(target = "fulfillmentStoreName", source = "fulfillmentStore.name")
    @Mapping(target = "fulfillmentType", source = ".", qualifiedByName = "determineFulfillmentType")
    OrderResponseDto toDto(CustomerOrder order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "priceAtPurchase", source = "priceAtTimeOfPurchase")
    @Mapping(target = "subTotal", source = ".", qualifiedByName = "calculateSubTotal")
    OrderResponseDto.OrderItemDto toItemDto(OrderItem orderItem);

    OrderResponseDto.StatusHistoryDto toHistoryDto(OrderStatusHistory history);

    @Named("calculateSubTotal")
    default BigDecimal calculateSubTotal(OrderItem item) {
        if (item.getPriceAtTimeOfPurchase() == null || item.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPriceAtTimeOfPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    @Named("determineFulfillmentType")
    default String determineFulfillmentType(CustomerOrder order) {
        return (order.getFulfillmentStore() != null) ? "Pickup / Store Fulfilled" : "Warehouse Delivery";
    }
}