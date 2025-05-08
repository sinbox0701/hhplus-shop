package kr.hhplus.be.server.infrastructure.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.cache.support.CompositeCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 캐시 설정 클래스
 * Redis와 Caffeine을 사용하여 다층 캐싱 구조를 구성합니다.
 */
@Configuration
@EnableCaching
class CacheConfig {
    /**
     * 메인 캐시 매니저
     * 로컬 캐시(Caffeine)와 분산 캐시(Redis)를 계층적으로 구성합니다.
     */
    @Primary
    @Bean
    fun cacheManager(
        redisCacheManager: RedisCacheManager,
        caffeineCacheManager: CaffeineCacheManager
    ): CacheManager {
        return CompositeCacheManager().apply {
            setCacheManagers(listOf(caffeineCacheManager, redisCacheManager))
            setFallbackToNoOpCache(false)
        }
    }

    /**
     * Redis 캐시 매니저 설정
     * 분산 환경에서의 L2 캐시로 사용됩니다.
     */
    @Bean
    fun redisCacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
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
            "bestSellers" to defaultCacheConfig.entryTtl(Duration.ofMinutes(15)),
            "users" to defaultCacheConfig.entryTtl(Duration.ofMinutes(30)),
            "userAccounts" to defaultCacheConfig.entryTtl(Duration.ofMinutes(30)),
            "coupons" to defaultCacheConfig.entryTtl(Duration.ofMinutes(20)),
            "orderProducts" to defaultCacheConfig.entryTtl(Duration.ofMinutes(10))
        )
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
    
    /**
     * Caffeine 캐시 매니저 설정
     * 앱 인스턴스 내 로컬 캐시(L1)로 사용됩니다.
     */
    @Bean
    fun caffeineCacheManager(): CaffeineCacheManager {
        val cacheManager = CaffeineCacheManager()
        
        val defaultCaffeine = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
        
        cacheManager.setCaffeine(defaultCaffeine)
        cacheManager.setCacheNames(
            setOf(
                "products", "bestSellers", "categories",
                "users", "userAccounts", "coupons", "orderProducts"
            )
        )
        
        return cacheManager
    }
} 