package kr.hhplus.be.server.infrastructure.cache

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

/**
 * Cache Stampede 현상을 방지하기 위한 서비스
 * 
 * Cache Stampede는 다수의 요청이 동시에 캐시 미스를 경험하고 
 * 모두 데이터 소스에 접근할 때 발생하는 성능 병목 현상입니다.
 */
@Service
class CacheStampedePreventionService(
    private val cacheManager: CacheManager,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private const val LOCK_TIMEOUT = 5L  // 초
        private const val LOCK_PREFIX = "cache:lock:"
        private const val PROBABILISTIC_REFRESH_THRESHOLD = 0.1  // 만료 시간의 10% 남았을 때
        private const val REFRESH_PROBABILITY = 0.1  // 10% 확률로 갱신
    }
    
    /**
     * Mutex(Lock) 패턴을 사용하여 Cache Stampede 방지
     * 캐시 미스 시 첫 번째 스레드만 값을 로드하고 나머지는 대기합니다.
     * 
     * @param cacheName 캐시 이름
     * @param key 캐시 키
     * @param dataLoader 데이터 로딩 함수(캐시 미스 시 호출)
     * @param ttl 캐시 TTL(초)
     * @return 캐시된 값 또는 새로 로드된 값
     */
    fun <T> executeWithLock(
        cacheName: String,
        key: String,
        dataLoader: Supplier<T>,
        ttl: Long = 300  // 기본 5분
    ): T {
        val cache = cacheManager.getCache(cacheName)
        
        // 1. 캐시에서 값 확인
        val cachedValue = cache?.get(key)
        if (cachedValue != null) {
            @Suppress("UNCHECKED_CAST")
            return cachedValue.get() as T
        }
        
        // 2. 분산 락 키 생성
        val lockKey = "$LOCK_PREFIX$cacheName:$key"
        
        // 3. 락 획득 시도
        val lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", LOCK_TIMEOUT, TimeUnit.SECONDS)
            ?: false
        
        try {
            if (lockAcquired) {
                // 락 획득 성공: 데이터 로드 및 캐시 저장
                val value = dataLoader.get()
                cache?.put(key, value)
                return value
            } else {
                // 락 획득 실패: 짧게 대기 후 재확인
                Thread.sleep(100)
                
                // 다시 캐시 확인 (다른 스레드가 값을 로드했을 수 있음)
                val valueAfterWait = cache?.get(key)
                if (valueAfterWait != null) {
                    @Suppress("UNCHECKED_CAST")
                    return valueAfterWait.get() as T
                }
                
                // 여전히 없으면 직접 로드 (최악의 경우)
                return dataLoader.get()
            }
        } finally {
            // 락 해제
            if (lockAcquired) {
                redisTemplate.delete(lockKey)
            }
        }
    }
    
    /**
     * 확률적 조기 만료를 통한 Cache Stampede 방지
     * TTL이 일정 비율 이하로 남았을 때 일정 확률로 백그라운드에서 캐시를 갱신합니다.
     * 
     * @param cacheName 캐시 이름
     * @param key 캐시 키
     * @param dataLoader 데이터 로딩 함수(캐시 미스 또는 갱신 시 호출)
     * @param ttl 캐시 TTL(초)
     * @return 캐시된 값 또는 새로 로드된 값
     */
    fun <T> executeWithProbabilisticRefresh(
        cacheName: String,
        key: String,
        dataLoader: Supplier<T>,
        ttl: Long = 300  // 기본 5분
    ): T {
        val cache = cacheManager.getCache(cacheName)
        
        // 1. 캐시에서 값 확인
        val cachedValue = cache?.get(key)
        if (cachedValue != null) {
            @Suppress("UNCHECKED_CAST")
            val value = cachedValue.get() as T
            
            // 만료 시간 확인 (Redis 전용)
            val remainingTtl = redisTemplate.getExpire("$cacheName::$key", TimeUnit.SECONDS)
            
            // 만료 임계치에 도달하고 확률 조건이 맞으면 비동기 갱신
            val thresholdTtl = (ttl * PROBABILISTIC_REFRESH_THRESHOLD).toLong()
            if (remainingTtl in 1..thresholdTtl && Math.random() < REFRESH_PROBABILITY) {
                CompletableFuture.runAsync {
                    try {
                        val freshValue = dataLoader.get()
                        cache.put(key, freshValue)
                    } catch (e: Exception) {
                        // 백그라운드 갱신 실패 로깅
                    }
                }
            }
            
            return value
        }
        
        // 2. 캐시 미스: 새로 데이터 로드
        val newValue = dataLoader.get()
        cache?.put(key, newValue)
        return newValue
    }
} 