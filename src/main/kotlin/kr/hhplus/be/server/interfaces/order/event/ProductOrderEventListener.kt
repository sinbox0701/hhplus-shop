package kr.hhplus.be.server.interfaces.order.event

import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.ranking.service.ProductRankingService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 주문 이벤트를 구독하여 상품 관련 처리를 수행하는 리스너
 */
@Component
class ProductOrderEventListener(
    private val productOptionService: ProductOptionService,
    private val productRankingService: ProductRankingService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 주문 생성 이벤트 처리 - 상품 재고 감소
     * - 트랜잭션 완료 후 이벤트 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Created::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional
    fun handleOrderCreated(event: OrderEvent.Created) {
        try {
            log.info("상품 재고 차감 시작: orderId={}", event.orderId)
            
            // 각 주문 상품별로 재고 감소 처리
            event.orderItems.forEach { orderItem ->
                productOptionService.subtractQuantity(
                    ProductOptionCommand.UpdateQuantityCommand(
                        id = orderItem.productOptionId,
                        quantity = orderItem.quantity
                    )
                )
            }
            
            log.info("상품 재고 차감 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("상품 재고 차감 실패: orderId={}, 사유={}", event.orderId, e.message, e)
            
            // 재고 차감 실패 이벤트 발행 - 주문 취소를 위한 보상 트랜잭션 유도
            applicationEventPublisher.publishEvent(
                ProductStockEvent.DecreaseFailed(
                    orderId = event.orderId,
                    userId = event.userId,
                    reason = e.message ?: "Unknown error"
                )
            )
        }
    }
    
    /**
     * 주문 취소 이벤트 처리 - 상품 재고 복구
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Cancelled::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional
    fun handleOrderCancelled(event: OrderEvent.Cancelled) {
        try {
            log.info("상품 재고 복구 시작: orderId={}", event.orderId)
            
            // 각 주문 상품별로 재고 복구 처리
            event.orderItems.forEach { orderItem ->
                productOptionService.updateQuantity(
                    ProductOptionCommand.UpdateQuantityCommand(
                        id = orderItem.productOptionId,
                        quantity = orderItem.quantity
                    )
                )
            }
            
            log.info("상품 재고 복구 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("상품 재고 복구 실패: orderId={}, 사유={}", event.orderId, e.message, e)
        }
    }
    
    /**
     * 주문 완료 이벤트 처리 - 상품 랭킹 점수 업데이트
     * - 비동기 처리 (주문 처리와 별개로 진행)
     */
    @EventListener
    @Async
    fun handleOrderCompleted(event: OrderEvent.Completed) {
        try {
            log.info("상품 랭킹 업데이트 시작: orderId={}", event.orderId)
            
            // 각 주문 상품에 대해 랭킹 점수 업데이트
            event.orderItems.forEach { orderItem ->
                productRankingService.incrementProductScore(
                    productId = orderItem.productId,
                    increment = orderItem.quantity.toInt()
                )
            }
            
            log.info("상품 랭킹 업데이트 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("상품 랭킹 업데이트 실패: orderId={}, 사유={}", event.orderId, e.message, e)
            // 랭킹 업데이트는 중요도가 낮으므로 실패해도 별도 처리 없음
        }
    }
}

/**
 * 상품 재고 관련 이벤트
 */
sealed class ProductStockEvent {
    /**
     * 재고 감소 실패 이벤트
     */
    data class DecreaseFailed(
        val orderId: Long,
        val userId: Long,
        val reason: String
    ): ProductStockEvent()
} 