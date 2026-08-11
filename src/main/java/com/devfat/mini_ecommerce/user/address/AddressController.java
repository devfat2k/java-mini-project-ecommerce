package com.devfat.mini_ecommerce.user.address;

import com.devfat.mini_ecommerce.shared.base.ApiResponse;
import com.devfat.mini_ecommerce.shared.security.UserPrincipal;
import com.devfat.mini_ecommerce.user.address.dto.AddressResponseDto;
import com.devfat.mini_ecommerce.user.address.dto.ChangeDefaultAddressDto;
import com.devfat.mini_ecommerce.user.address.dto.CreateAddressRequestDto;
import com.devfat.mini_ecommerce.user.address.dto.UpdateAddressRequestDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Tag(name = "User Address", description = "Quản lý sổ địa chỉ giao hàng")
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AddressResponseDto>>> getAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal.getUserId();
        List<AddressResponseDto> addressResponse = addressService.getAddressList(userId);
        return ResponseEntity.ok().body(
                ApiResponse.success(
                        addressResponse,
                        "Get Address Successfully!"
                )
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponseDto>> createAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateAddressRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(
                        addressService.createAddress(userId, requestDto),
                        "Create Address Successfully"
                )
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AddressResponseDto>> updateAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long addressId,
            @Valid @RequestBody UpdateAddressRequestDto requestDto
    ) {
        Long userId = userPrincipal.getUserId();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        addressService.updateAddress(userId, addressId, requestDto),
                        "Update Address Successfully"
                )
        );
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long addressId
    ) {
        Long userId = userPrincipal.getUserId();
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        null,
                        "Delete Address Successfully"
                )
        );
    }

    @PostMapping("/change-address-default/{id}")
    public ResponseEntity<ApiResponse<Void>> changeDefaultAddress(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable("id") Long addressId,
            @Valid @RequestBody ChangeDefaultAddressDto requestDto
            ) {
        Long userId = userPrincipal.getUserId();
        addressService.changeDefaultAddress(userId, addressId, requestDto);
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.success(
                        null,
                        "Change Default Address Successfully"
                )
        );
    }
}


