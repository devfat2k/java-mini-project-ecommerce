package com.devfat.mini_ecommerce.shared.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public LettuceBasedProxyManager<byte[]> proxyManager(RedisConnectionFactory connectionFactory) {
        if (connectionFactory instanceof LettuceConnectionFactory lettuceConnectionFactory) {
            RedisClient redisClient = (RedisClient) lettuceConnectionFactory.getNativeClient();
            StatefulRedisConnection<byte[], byte[]> connection = redisClient.connect(ByteArrayCodec.INSTANCE);

            return LettuceBasedProxyManager.builderFor(connection)
                    .withClientSideConfig(
                            ClientSideConfig.getDefault()
                                    .withExpirationAfterWriteStrategy(
                                            ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                                    Duration.ofMinutes(30)
                                            )
                                    )
                    )
                    .build();
        }
        throw new IllegalStateException("RedisConnectionFactory must be an instance of LettuceConnectionFactory");
    }
}
