package com.jewelleryapp.mapper;

import com.jewelleryapp.dto.response.ShipmentResponseDto;
import com.jewelleryapp.entity.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "orderId", source = "order.id")
    ShipmentResponseDto toDto(Shipment shipment);
}