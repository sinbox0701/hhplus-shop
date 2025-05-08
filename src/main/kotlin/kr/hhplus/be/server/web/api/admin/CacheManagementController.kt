package kr.hhplus.be.server.web.api.admin

import com.github.benmanes.caffeine.cache.stats.CacheStats
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.data.redis.cache.RedisCache
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ConcurrentMap

/**
 * 캐시 관리를 위한 관리자용 API 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/cache")
class CacheManagementController(
    private val cacheManager: CacheManager,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private val CACHE_NAMES = listOf(
            "products", "bestSellers", "users", "userAccounts", 
            "coupons", "orderProducts", "categories"
        )
    }
    
    /**
     * 모든 캐시 상태 정보 조회
     */
    @GetMapping("/stats")
    fun getCacheStats(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        // 각 캐시 이름으로 통계 수집
        CACHE_NAMES.forEach { cacheName ->
            val cache = cacheManager.getCache(cacheName)
            if (cache != null) {
                val cacheStats = mutableMapOf<String, Any>(
                    "name" to cacheName,
                    "available" to true
                )
                
                // Caffeine 캐시 통계 (L1)
                if (cache is CaffeineCache) {
                    val nativeCache = cache.nativeCache
                    val caffeineStats = nativeCache.stats()
                    
                    cacheStats["type"] = "Caffeine"
                    cacheStats["hitCount"] = caffeineStats.hitCount()
                    cacheStats["missCount"] = caffeineStats.missCount()
                    cacheStats["hitRate"] = caffeineStats.hitRate()
                    cacheStats["estimatedSize"] = nativeCache.estimatedSize()
                    cacheStats["evictionCount"] = caffeineStats.evictionCount()
                }
                
                // Redis 캐시 정보 (L2)
                if (cache is RedisCache) {
                    val keyPattern = "cache::${cacheName}::*"
                    val keys = redisTemplate.keys(keyPattern)
                    
                    cacheStats["type"] = "Redis"
                    cacheStats["keyCount"] = keys.size
                    
                    // 캐시 TTL 정보
                    val ttlInfo = cache.cacheConfiguration.ttl
                    cacheStats["ttl"] = "${ttlInfo.seconds}s"
                }
                
                stats[cacheName] = cacheStats
            }
        }
        
        return stats
    }
    
    /**
     * 상세 캐시 통계 조회
     */
    @GetMapping("/stats/detailed")
    fun getDetailedCacheStats(): Map<String, Any> {
        val globalStats = mutableMapOf<String, Any>()
        
        var totalHitCount = 0L
        var totalMissCount = 0L
        var totalRequestCount = 0L
        var totalEvictionCount = 0L
        
        // 각 Caffeine 캐시에서 통계 수집 및 집계
        CACHE_NAMES.forEach { cacheName ->
            val cache = cacheManager.getCache(cacheName)
            if (cache is CaffeineCache) {
                val stats = cache.nativeCache.stats()
                
                totalHitCount += stats.hitCount()
                totalMissCount += stats.missCount()
                totalEvictionCount += stats.evictionCount()
            }
        }
        
        totalRequestCount = totalHitCount + totalMissCount
        
        // 글로벌 히트율 계산
        val globalHitRate = if (totalRequestCount > 0) {
            totalHitCount.toDouble() / totalRequestCount.toDouble()
        } else {
            0.0
        }
        
        globalStats["totalRequests"] = totalRequestCount
        globalStats["totalHits"] = totalHitCount
        globalStats["totalMisses"] = totalMissCount
        globalStats["globalHitRate"] = globalHitRate
        globalStats["totalEvictions"] = totalEvictionCount
        
        return globalStats
    }
    
    /**
     * 특정 캐시 초기화
     */
    @PostMapping("/clear/{cacheName}")
    fun clearCache(@PathVariable cacheName: String): Map<String, String> {
        val cache = cacheManager.getCache(cacheName)
        cache?.clear()
        return mapOf("message" to "캐시 $cacheName 초기화 완료")
    }
    
    /**
     * 모든 캐시 초기화
     */
    @PostMapping("/clear-all")
    fun clearAllCaches(): Map<String, String> {
        CACHE_NAMES.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
        return mapOf("message" to "모든 캐시 초기화 완료")
    }
    
    /**
     * 캐시 워밍업 - 인기 항목 미리 로드
     */
    @PostMapping("/warmup/{cacheName}")
    fun warmupCache(@PathVariable cacheName: String): Map<String, String> {
        // 실제 구현에서는 캐시 워밍업 로직 구현
        // 예: 상품 캐시 워밍업을 위해 인기 상품 목록 조회 서비스 호출
        
        return mapOf("message" to "캐시 $cacheName 워밍업 요청 완료")
    }
} 