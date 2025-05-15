package kr.hhplus.be.server.application.ranking

import kr.hhplus.be.server.domain.ranking.service.ProductRankingService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 랭킹 시스템 자동화 스케줄러
 * - 매일 자정에 실행되어 랭킹 정보 초기화 및 관리
 */
@Component
class RankingScheduler(
    private val productRankingService: ProductRankingService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 매일 자정에 일간 랭킹 초기화 및 관리
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    fun initializeDailyRankings() {
        log.info("일간 랭킹 초기화 시작")
        try {
            productRankingService.initializeRankings()
            log.info("일간 랭킹 초기화 완료")
        } catch (e: Exception) {
            log.error("일간 랭킹 초기화 실패", e)
        }
    }
    
    /**
     * 매 15분마다 인기 상품 캐시 갱신
     */
    @Scheduled(fixedRate = 900000) // 15분(15 * 60 * 1000ms)
    fun refreshPopularProductsCache() {
        log.info("인기 상품 캐시 갱신 시작")
        try {
            // 인기 상품 캐시 미리 갱신 로직 (필요시 구현)
            log.info("인기 상품 캐시 갱신 완료")
        } catch (e: Exception) {
            log.error("인기 상품 캐시 갱신 실패", e)
        }
    }
} 