package kr.hhplus.be.server.presentation.ranking

import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.ProductResult
import kr.hhplus.be.server.domain.ranking.service.ProductRankingService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

/**
 * 상품 랭킹 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/rankings")
class RankingController(
    private val productRankingService: ProductRankingService,
    private val productFacade: ProductFacade
) {
    /**
     * 일간 인기 상품 조회
     * 
     * @param date 조회 날짜 (기본값: 오늘)
     * @param limit 조회 개수 (기본값: 10)
     * @return 인기 상품 목록
     */
    @GetMapping("/daily")
    fun getDailyTopProducts(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(defaultValue = "10") limit: Int
    ): List<ProductResult.ProductWithOptions> {
        val targetDate = date ?: LocalDate.now()
        val topProductIds = productRankingService.getTopDailyProducts(targetDate, limit)
        
        return if (topProductIds.isNotEmpty()) {
            productFacade.getProductsWithOptionsByIds(topProductIds)
        } else {
            // 데이터가 없으면 일반 상품 목록 반환
            productFacade.getAllProductsWithOptions().take(limit)
        }
    }
    
    /**
     * 주간 인기 상품 조회
     * 
     * @param date 기준 날짜 (기본값: 오늘)
     * @param limit 조회 개수 (기본값: 10)
     * @return 인기 상품 목록
     */
    @GetMapping("/weekly")
    fun getWeeklyTopProducts(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(defaultValue = "10") limit: Int
    ): List<ProductResult.ProductWithOptions> {
        val targetDate = date ?: LocalDate.now()
        val topProductIds = productRankingService.getTopWeeklyProducts(targetDate, limit)
        
        return if (topProductIds.isNotEmpty()) {
            productFacade.getProductsWithOptionsByIds(topProductIds)
        } else {
            // 데이터가 없으면 일반 상품 목록 반환
            productFacade.getAllProductsWithOptions().take(limit)
        }
    }
    
    /**
     * 특정 상품의 랭킹 조회
     * 
     * @param productId 상품 ID
     * @return 일간/주간 랭킹 정보
     */
    @GetMapping("/product/{productId}")
    fun getProductRanking(
        @PathVariable productId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): Map<String, Any?> {
        val targetDate = date ?: LocalDate.now()
        
        return mapOf(
            "productId" to productId,
            "date" to targetDate.toString(),
            "dailyRank" to productRankingService.getProductDailyRank(productId, targetDate),
            "weeklyRank" to productRankingService.getProductWeeklyRank(productId, targetDate)
        )
    }
} 