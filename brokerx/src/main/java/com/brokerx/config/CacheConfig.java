package com.brokerx.config;

import java.time.Duration;
import java.util.Map;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class CacheConfig {

  private static final Duration DEFAULT_TTL = Duration.ofSeconds(60);

  @Bean
  CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration baseConfig =
        RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()))
            .entryTtl(DEFAULT_TTL)
            .disableCachingNullValues();

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(baseConfig)
        .withInitialCacheConfigurations(
            Map.of(
                "orderExecutions", baseConfig.entryTtl(DEFAULT_TTL),
                "orderNotifications", baseConfig.entryTtl(DEFAULT_TTL)))
        .build();
  }
}
