package com.devfat.mini_ecommerce.home.dailyarrival.internal;


import com.devfat.mini_ecommerce.home.dailyarrival.dto.CreateDailyArrivalRequestDto;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.DailyArrivalResponseDto;
import com.devfat.mini_ecommerce.home.dailyarrival.dto.UpdateDailyArrivalRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface DailyArrivalMapper {
    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "arrivalDate", target = "date")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "product.originalPrice", target = "originalPrice")
    @Mapping(source = "product.imageUrl", target = "imageUrl")
    @Mapping(source = "product.name", target = "imageAlt")
    DailyArrivalResponseDto toResponseDto(DailyArrivalEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "date", target = "arrivalDate")
    @Mapping(target = "arrivedAt", ignore = true)
    DailyArrivalEntity toEntity(CreateDailyArrivalRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "arrivalDate", ignore = true)
    @Mapping(target = "arrivedAt", ignore = true)
    void updateEntityFromDto(UpdateDailyArrivalRequestDto dto, @MappingTarget DailyArrivalEntity entity);
}
