package kr.hhplus.be.server.infrastructure.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

/**
 * 캐시 설정 클래스
 * Redis를 사용하여 애플리케이션 캐싱을 구성합니다.
 */
@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )
        
        // 캐시별 TTL 설정
        val cacheConfigurations = mapOf(
            "products" to defaultCacheConfig.entryTtl(Duration.ofMinutes(60)),
            "categories" to defaultCacheConfig.entryTtl(Duration.ofHours(12)),
            "bestSellers" to defaultCacheConfig.entryTtl(Duration.ofMinutes(15))
        )
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
} 