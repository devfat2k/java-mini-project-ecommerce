package com.devfat.mini_ecommerce.shared.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule());
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Nhúng thông tin class dưới dạng Property ("@class") để deserialize đúng Record / DTO / List cụ thể
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Cấu hình mặc định có JSON Serializer
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();

        // 1. Cấu hình Cache chung
        perCacheConfig.put("products", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        perCacheConfig.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        perCacheConfig.put("analytics", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // 2. Cấu hình Cache riêng cho Module HOME (Tái sử dụng defaultConfig đã có JSON Serializer)
        perCacheConfig.put("home:heroSlides", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put("home:featuredProductTabs", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put("home:categories", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put("home:dailyArrivals", defaultConfig.entryTtl(Duration.ofHours(6)));
        perCacheConfig.put("home:featuredProducts", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        perCacheConfig.put("home:comboSets", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        perCacheConfig.put("home:featuredReviews", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        perCacheConfig.put("home:stats", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }
}
