package com.devfat.mini_ecommerce.payment.internal;

import com.devfat.mini_ecommerce.payment.dto.CreatePaymentResponseDto;










import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "paymentUrl", ignore = true)
    CreatePaymentResponseDto toCreatePaymentResponseDto(PaymentEntity paymentEntity);
}
