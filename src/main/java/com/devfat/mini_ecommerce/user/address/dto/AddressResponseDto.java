package com.devfat.mini_ecommerce.user.address.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponseDto {
    private String id;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private String defaultAddress;
    private String tag;
}
