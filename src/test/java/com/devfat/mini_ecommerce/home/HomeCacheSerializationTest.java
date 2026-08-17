package com.devfat.mini_ecommerce.home;

import com.devfat.mini_ecommerce.home.dto.*;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
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

        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
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
        System.out.println("JavaTime List JSON: " + new String(bytes));

        Object deserialized = serializer.deserialize(bytes);
        assertNotNull(deserialized);
        List<?> deserializedList = (List<?>) deserialized;
        assertEquals(1, deserializedList.size());
        assertEquals(DailyArrivalDto.class, deserializedList.get(0).getClass());
    }

    @Test
    void testListOfWithEverythingAndProperty() {
        GenericJackson2JsonRedisSerializer serializer = createSerializer();

        List<FeaturedProductTabDto> tabs = List.of(
                new FeaturedProductTabDto("all", "Tất cả", 0)
        );

        byte[] bytes = serializer.serialize(tabs);
        System.out.println("List.of JSON: " + new String(bytes));

        Object deserialized = serializer.deserialize(bytes);
        System.out.println("List.of Deserialized: " + deserialized);
        assertNotNull(deserialized);
    }

    @Test
    void testConfig2_DefaultConstructor_HomePageDataDto() {
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
        System.out.println("Config 2 HomePageData JSON: " + new String(bytes));

        Object deserialized = serializer.deserialize(bytes);
        System.out.println("Config 2 HomePageData Deserialized: " + deserialized);
        assertEquals(HomePageDataDto.class, deserialized.getClass());
    }
}
