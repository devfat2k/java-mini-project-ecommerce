package com.devfat.mini_ecommerce.shared.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
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

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.registerModule(new JavaTimeModule());
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Chống lỗi khi DTO thêm/bớt trường mới trong quá trình phát triển (tránh phải xóa cache bằng tay)
        redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        redisObjectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        redisObjectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

        // Bảo mật Deserialization: Chỉ cho phép serialize các class trong package dự án và JDK an toàn
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

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        // Cấu hình Redis Cache mặc định
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> perCacheConfig = new HashMap<>();

        // 1. Cấu hình Cache Module Product & Category
        perCacheConfig.put("products", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        perCacheConfig.put("product:category_browse", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        perCacheConfig.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        perCacheConfig.put("analytics", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        perCacheConfig.put("user-permissions", defaultConfig.entryTtl(Duration.ofMinutes(60)));

        // 2. Cấu hình Cache Module Home
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

    /**
     * Tự động phục hồi khi lỗi Cache: Nếu Redis có cache cũ không tương thích hoặc lỗi kết nối,
     * hệ thống sẽ ghi log cảnh báo và TỰ ĐỘNG query trực tiếp từ Database thay vì trả về lỗi 500.
     * Người phát triển KHÔNG BAO GIỜ cần phải xóa cache bằng tay nữa!
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("⚠️ Redis GET error for key '{}' in cache '{}'. Falling back to Database gracefully. Details: {}",
                        key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("⚠️ Redis PUT error for key '{}' in cache '{}'. Details: {}",
                        key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("⚠️ Redis EVICT error for key '{}' in cache '{}'. Details: {}",
                        key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("⚠️ Redis CLEAR error in cache '{}'. Details: {}",
                        cache.getName(), exception.getMessage());
            }
        };
    }
}
