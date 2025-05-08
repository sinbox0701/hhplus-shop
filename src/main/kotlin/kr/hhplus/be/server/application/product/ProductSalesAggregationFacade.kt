package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.product.model.ProductDailySales
import kr.hhplus.be.server.domain.product.service.ProductSalesService
import kr.hhplus.be.server.domain.product.service.ProductService
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 상품 판매량 집계를 위한 애플리케이션 서비스
 * - 여러 도메인 서비스를 조합하여 판매 데이터 집계 기능 제공
 */
@Service
class ProductSalesAggregationFacade(
    private val orderItemService: OrderItemService,
    private val productSalesService: ProductSalesService,
    private val productService: ProductService,
    private val cacheManager: CacheManager
) : ApplicationContextAware {
    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var applicationContext: ApplicationContext
    
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }
    
    /**
     * 매일 자정에 전날의 판매 데이터를 집계 후 캐시 갱신
     */
    @CacheEvict(value = ["bestSellers"], allEntries = true)
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    fun aggregateDailySales() {
        // 어제 날짜 계산
        val yesterday = LocalDate.now().minusDays(1)
        
        // 집계 기간 설정 (어제 00:00:00 ~ 23:59:59)
        val startDateTime = yesterday.atStartOfDay()
        val endDateTime = yesterday.plusDays(1).atStartOfDay().minusNanos(1)
        
        // 어제 판매된 상품별 판매량 집계 - 인덱스를 활용한 효율적인 조회를 위해 직접 쿼리 결과 사용
        val productQuantityMap = orderItemService.getProductQuantityMap(startDateTime, endDateTime)
        
        // 기존 집계 데이터 찾기 (중복 방지)
        val existingSales = productSalesService.getSalesByDate(yesterday)
            .associateBy { it.productId }
            
        // 새로운 집계 데이터 생성
        val now = LocalDateTime.now()
        val salesEntities = productQuantityMap.map { (productId, quantity) ->
            // 기존 데이터가 있으면 업데이트, 없으면 신규 생성
            existingSales[productId]?.let { existing ->
                ProductDailySales(
                    id = existing.id,
                    productId = existing.productId,
                    salesDate = existing.salesDate,
                    quantitySold = quantity,
                    createdAt = existing.createdAt,
                    updatedAt = now
                )
            } ?: ProductDailySales(
                productId = productId,
                salesDate = yesterday,
                quantitySold = quantity,
                createdAt = now,
                updatedAt = now
            )
        }
        
        // 집계 테이블에 저장
        productSalesService.saveAllSales(salesEntities)
        
        // 캐시 갱신을 위한 인기 상품 미리 로드 (Refresh-Ahead 패턴)
        refreshBestSellersCache()
    }
    
    /**
     * 인기 상품 캐시 미리 갱신 (Refresh-Ahead 패턴)
     */
    @Scheduled(fixedRate = 900000) // 15분마다 실행 (15min * 60sec * 1000ms)
    fun refreshBestSellersCache() {
        // 인기 상품 캐시 미리 생성 (주요 사용 케이스)
        try {
            log.info("인기 상품 캐시 갱신 시작")
            
            // 기본 인기 상품 캐싱 (3일, 5개)
            val productFacade = applicationContext.getBean(ProductFacade::class.java)
            productFacade.getTopSellingProducts(3, 5)
            
            // 추가적인 인기 상품 케이스 캐싱
            productFacade.getTopSellingProducts(7, 10)
            
            log.info("인기 상품 캐시 갱신 완료")
        } catch (e: Exception) {
            log.error("인기 상품 캐시 갱신 중 오류 발생", e)
        }
    }
} 