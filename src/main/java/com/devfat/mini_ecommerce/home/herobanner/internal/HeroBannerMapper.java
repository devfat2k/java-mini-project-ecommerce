package com.devfat.mini_ecommerce.home.herobanner.internal;


import com.devfat.mini_ecommerce.home.herobanner.dto.CreateHeroBannerRequestDto;
import com.devfat.mini_ecommerce.home.herobanner.dto.HeroBannerResponseDto;
import com.devfat.mini_ecommerce.home.herobanner.dto.UpdateHeroBannerRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface HeroBannerMapper {
    HeroBannerResponseDto toResponseDto(HeroBannerEntity entity);

    @Mapping(target = "id", ignore = true)
    HeroBannerEntity toEntity(CreateHeroBannerRequestDto dto);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(UpdateHeroBannerRequestDto dto, @MappingTarget HeroBannerEntity entity);
}
