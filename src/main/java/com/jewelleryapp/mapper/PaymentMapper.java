package com.jewelleryapp.mapper;

import com.jewelleryapp.dto.response.PaymentResponseDto;
import com.jewelleryapp.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "orderId", source = "order.id")
    PaymentResponseDto toDto(Payment payment);
}