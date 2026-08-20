package com.devfat.mini_ecommerce.home;

import com.devfat.mini_ecommerce.home.dto.*;
import com.devfat.mini_ecommerce.product.dto.CategoryRevenueResponseDto;
import com.devfat.mini_ecommerce.product.dto.MonthlyRevenueResponseDto;
import com.devfat.mini_ecommerce.product.dto.TopProductResponseDto;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HomeCacheSerializationTest {

    private GenericJackson2JsonRedisSerializer createSerializer() {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule());
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        redisObjectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        redisObjectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("com.devfat.mini_ecommerce")
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("java.math")
                .allowIfSubType("org.springframework.data.domain")
                .build();

        redisObjectMapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    @Test
    void testDailyArrivalDtoWithJavaTime() {
        GenericJackson2JsonRedisSerializer serializer = createSerializer();

        DailyArrivalDto dto = new DailyArrivalDto(
                1L,
                10L,
                LocalDateTime.now(),
                "CHUYẾN ĐÊM HÔM NAY",
                "Tôm Hùm Bông",
                "Mô tả",
                "1kg",
                "Phú Yên",
                new BigDecimal("99.99"),
                new BigDecimal("120.00"),
                "https://image.png",
                "Tôm Hùm"
        );

        List<DailyArrivalDto> list = new ArrayList<>(List.of(dto));

        byte[] bytes = serializer.serialize(list);
        Object deserialized = serializer.deserialize(bytes);
        assertNotNull(deserialized);
        List<?> deserializedList = (List<?>) deserialized;
        assertEquals(1, deserializedList.size());
        assertEquals(DailyArrivalDto.class, deserializedList.get(0).getClass());
    }

    @Test
    void testAnalyticsDtosSerialization() {
        GenericJackson2JsonRedisSerializer serializer = createSerializer();

        List<TopProductResponseDto> topProducts = List.of(
                new TopProductResponseDto("Cua Hoàng Đế", new BigDecimal("1500000"), 25)
        );
        byte[] topBytes = serializer.serialize(new ArrayList<>(topProducts));
        Object topDes = serializer.deserialize(topBytes);
        assertNotNull(topDes);

        List<MonthlyRevenueResponseDto> monthly = List.of(
                new MonthlyRevenueResponseDto(LocalDateTime.now(), new BigDecimal("50000000"))
        );
        byte[] monthlyBytes = serializer.serialize(new ArrayList<>(monthly));
        Object monthlyDes = serializer.deserialize(monthlyBytes);
        assertNotNull(monthlyDes);

        List<CategoryRevenueResponseDto> category = List.of(
                new CategoryRevenueResponseDto("Tôm Cua Ghẹ", new BigDecimal("35000000"))
        );
        byte[] catBytes = serializer.serialize(new ArrayList<>(category));
        Object catDes = serializer.deserialize(catBytes);
        assertNotNull(catDes);
    }

    @Test
    void testHomePageDataDtoSerialization() {
        GenericJackson2JsonRedisSerializer serializer = createSerializer();

        HomePageDataDto homeData = new HomePageDataDto(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(List.of(new FeaturedProductTabDto("all", "Tất cả", 0))),
                new ArrayList<>(),
                new ArrayList<>(),
                new HomeStatsDto(1250L, new BigDecimal("5.0"), 0L)
        );

        byte[] bytes = serializer.serialize(homeData);
        Object deserialized = serializer.deserialize(bytes);
        assertNotNull(deserialized);
        assertEquals(HomePageDataDto.class, deserialized.getClass());
    }
}
