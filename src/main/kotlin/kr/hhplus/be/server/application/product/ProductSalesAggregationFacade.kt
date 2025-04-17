package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.product.model.ProductDailySales
import kr.hhplus.be.server.domain.product.service.ProductSalesService
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
    private val productSalesService: ProductSalesService
) {
    
    /**
     * 매일 자정에 전날의 판매 데이터를 집계
     */
    @Transactional
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    fun aggregateDailySales() {
        // 어제 날짜 계산
        val yesterday = LocalDate.now().minusDays(1)
        
        // 집계 기간 설정 (어제 00:00:00 ~ 23:59:59)
        val startDateTime = yesterday.atStartOfDay()
        val endDateTime = yesterday.plusDays(1).atStartOfDay().minusNanos(1)
        
        // 어제 판매된 상품별 판매량 집계
        val productIds = orderItemService.getTopSellingProductIds(startDateTime, endDateTime, Int.MAX_VALUE)
        
        // ProductId 별로 주문량 계산 (임시 방법)
        val productSalesMap = mutableMapOf<Long, Int>()
        
        // 실제로는 이 로직이 더 효율적으로 구현되어야 함
        // 예: ORDER BY와 GROUP BY를 사용한 쿼리로 한 번에 가져오기
        productIds.forEach { productId ->
            val count = productSalesMap.getOrDefault(productId, 0) + 1
            productSalesMap[productId] = count
        }
        
        // 기존 집계 데이터 찾기 (중복 방지)
        val existingSales = productSalesService.getSalesByDate(yesterday)
            .associateBy { it.productId }
            
        // 새로운 집계 데이터 생성
        val now = LocalDateTime.now()
        val salesEntities = productSalesMap.map { (productId, quantity) ->
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
    }
} 