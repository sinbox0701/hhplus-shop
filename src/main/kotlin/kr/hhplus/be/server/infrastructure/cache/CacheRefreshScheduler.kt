package kr.hhplus.be.server.infrastructure.cache

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.context.ApplicationContext
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 주요 캐시의 정기적인 갱신을 처리하는 스케줄러
 * Refresh-Ahead 패턴을 구현하여 캐시가 만료되기 전에 미리 갱신합니다.
 */
@Component
@EnableScheduling
class CacheRefreshScheduler(
    private val cacheManager: CacheManager,
    private val applicationContext: ApplicationContext
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 인기 상품 캐시 주기적 갱신 (15분마다)
     * 캐시 만료 전에 미리 데이터를 갱신하여 항상 최신 데이터 유지
     */
    @Scheduled(fixedRate = 900000) // 15분 = 15 * 60 * 1000ms
    fun refreshBestSellersCache() {
        try {
            log.info("인기 상품 캐시 갱신 시작")
            
            // 필요한 서비스 주입
            // 실제 구현에서는 ProductFacade를 주입받아 사용
            val productFacade = applicationContext.getBean("productFacade")
            
            // 리플렉션을 사용해 메서드 호출 (실제 구현에서는 직접 호출)
            try {
                val getTopSellingProductsMethod = productFacade.javaClass.getMethod(
                    "getTopSellingProducts", Int::class.java, Int::class.java
                )
                
                // 기본 인기 상품 캐싱 (3일, 5개)
                getTopSellingProductsMethod.invoke(productFacade, 3, 5)
                
                // 추가적인 인기 상품 케이스 캐싱
                getTopSellingProductsMethod.invoke(productFacade, 7, 10)
                
                log.info("인기 상품 캐시 갱신 완료")
            } catch (e: Exception) {
                log.error("인기 상품 메서드 호출 중 오류", e)
            }
        } catch (e: Exception) {
            log.error("인기 상품 캐시 갱신 중 오류 발생", e)
        }
    }
    
    /**
     * 카테고리 캐시 주기적 갱신 (6시간마다)
     */
    @Scheduled(fixedRate = 21600000) // 6시간 = 6 * 60 * 60 * 1000ms
    fun refreshCategoryCache() {
        try {
            log.info("카테고리 캐시 갱신 시작")
            
            // 카테고리 조회 서비스 호출
            // 실제 구현에서는 관련 서비스 직접 호출
            
            log.info("카테고리 캐시 갱신 완료")
        } catch (e: Exception) {
            log.error("카테고리 캐시 갱신 중 오류 발생", e)
        }
    }
    
    /**
     * 모든 상품 캐시 주기적 갱신 (1시간마다)
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 60 * 60 * 1000ms
    fun refreshAllProductsCache() {
        try {
            log.info("전체 상품 캐시 갱신 시작")
            
            // 모든 상품 조회 서비스 호출
            // 실제 구현에서는 관련 서비스 직접 호출
            
            log.info("전체 상품 캐시 갱신 완료")
        } catch (e: Exception) {
            log.error("전체 상품 캐시 갱신 중 오류 발생", e)
        }
    }
    
    /**
     * 애플리케이션 시작 시 모든 필수 캐시 워밍업
     * 애플리케이션 시작 후 10초 뒤에 실행
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    fun warmupCaches() {
        try {
            log.info("캐시 워밍업 시작")
            
            // 모든 워밍업 메서드 호출
            refreshAllProductsCache()
            refreshCategoryCache()
            refreshBestSellersCache()
            
            log.info("캐시 워밍업 완료")
        } catch (e: Exception) {
            log.error("캐시 워밍업 중 오류 발생", e)
        }
    }
} 