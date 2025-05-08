package kr.hhplus.be.server.infrastructure.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Redisson 클라이언트 설정 클래스
 * 분산 락 구현을 위한 Redis 연결 설정
 */
@Configuration
class RedissonConfig {

    @Value("\${spring.data.redis.host}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port}")
    private val port: Int = 6379

    @Value("\${redisson.threads:16}")
    private val threads: Int = 16

    @Value("\${redisson.netty-threads:32}")
    private val nettyThreads: Int = 32

    @Value("\${redisson.transport-mode:NIO}")
    private val transportMode: String = "NIO"

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        
        // 단일 노드 설정
        config.useSingleServer()
            .setAddress("redis://$host:$port")
            .setRetryAttempts(5)
            .setRetryInterval(1500)
            .setTimeout(3000)
            .setConnectTimeout(3000)
        
        // 스레드 설정
        config.threads = threads
        config.nettyThreads = nettyThreads
        config.transportMode = when (transportMode) {
            "EPOLL" -> org.redisson.config.TransportMode.EPOLL
            "KQUEUE" -> org.redisson.config.TransportMode.KQUEUE
            else -> org.redisson.config.TransportMode.NIO
        }
        
        return Redisson.create(config)
    }
} 