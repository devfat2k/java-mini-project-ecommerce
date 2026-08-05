package com.devfat.mini_ecommerce.mapper;

import com.devfat.mini_ecommerce.dto.response.CreatePaymentResponseDto;
import com.devfat.mini_ecommerce.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "paymentUrl", ignore = true)
    CreatePaymentResponseDto toCreatePaymentResponseDto(PaymentEntity paymentEntity);
}
