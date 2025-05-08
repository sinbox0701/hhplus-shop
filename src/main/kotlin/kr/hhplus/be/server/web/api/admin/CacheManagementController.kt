package kr.hhplus.be.server.web.api.admin

import org.springframework.cache.CacheManager
import org.springframework.data.redis.cache.RedisCache
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 캐시 관리를 위한 관리자용 API 컨트롤러
 */
@RestController
@RequestMapping("/api/admin/cache")
class CacheManagementController(
    private val cacheManager: CacheManager
) {
    /**
     * 모든 캐시 상태 정보 조회
     */
    @GetMapping("/stats")
    fun getCacheStats(): Map<String, Any> {
        val stats = mutableMapOf<String, Any>()
        
        // 각 캐시 이름으로 통계 수집
        val cacheNames = listOf("products", "bestSellers", "users", "userAccounts", "coupons", "orderProducts")
        cacheNames.forEach { cacheName ->
            val cache = cacheManager.getCache(cacheName)
            if (cache != null) {
                stats[cacheName] = mapOf(
                    "name" to cacheName,
                    "available" to (cache != null)
                )
            }
        }
        
        return stats
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
        val cacheNames = listOf("products", "bestSellers", "users", "userAccounts", "coupons", "orderProducts")
        cacheNames.forEach { cacheName ->
            cacheManager.getCache(cacheName)?.clear()
        }
        return mapOf("message" to "모든 캐시 초기화 완료")
    }
} 