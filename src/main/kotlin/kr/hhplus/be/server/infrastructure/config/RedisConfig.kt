package kr.hhplus.be.server.infrastructure.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis 설정 클래스
 * RedisTemplate 설정을 통해 Redis 연결 및 직렬화 방식을 구성합니다.
 */
@Configuration
class RedisConfig {
    
    /**
     * Redis 작업을 위한 RedisTemplate 빈을 생성합니다.
     * 
     * @param redisConnectionFactory Redis 연결 팩토리
     * @return 설정된 RedisTemplate
     */
    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory
        
        // 키는 문자열로 직렬화
        template.keySerializer = StringRedisSerializer()
        
        // 값은 JSON으로 직렬화 (LocalDateTime 등 Java 8 시간 타입 지원)
        val objectMapper = ObjectMapper().apply {
            registerModule(JavaTimeModule())
        }
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        
        template.valueSerializer = jsonSerializer
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = jsonSerializer
        
        template.afterPropertiesSet()
        
        return template
    }
} 