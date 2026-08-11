package com.devfat.mini_ecommerce.user.address;

import com.devfat.mini_ecommerce.user.address.dto.AddressResponseDto;
import com.devfat.mini_ecommerce.user.address.dto.ChangeDefaultAddressDto;
import com.devfat.mini_ecommerce.user.address.dto.CreateAddressRequestDto;
import com.devfat.mini_ecommerce.user.address.dto.UpdateAddressRequestDto;

import java.util.List;

public interface AddressService {
    AddressResponseDto createAddress(Long userId, CreateAddressRequestDto createAddressRequestDto);
    AddressResponseDto updateAddress(Long userId, Long addressId, UpdateAddressRequestDto updateAddressRequestDto);
    void deleteAddress(Long userId, Long addressId);
    void changeDefaultAddress(Long userId, Long addressId, ChangeDefaultAddressDto changeDefaultAddressDto);
    List<AddressResponseDto> getAddressList(Long userId);
}
