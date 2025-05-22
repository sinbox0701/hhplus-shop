package kr.hhplus.be.server.interfaces.order.event

import kr.hhplus.be.server.domain.order.event.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.LocalDateTime

/**
 * 주문 이벤트를 구독하여 데이터 플랫폼으로 정보를 전송하는 리스너
 * - 트랜잭션 완료 후 비동기로 처리
 * - dataPlatform.enabled 설정으로 활성화/비활성화 가능
 */
@Component
@ConditionalOnProperty(name = ["dataPlatform.enabled"], havingValue = "true", matchIfMissing = false)
class OrderDataPlatformEventListener {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 주문 생성 이벤트를 받아 데이터 플랫폼으로 전송
     * - 트랜잭션 완료 후 비동기 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Created::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Async
    fun handleOrderCreated(event: OrderEvent.Created) {
        try {
            log.info("주문 생성 정보 데이터 플랫폼 전송 시작: orderId={}", event.orderId)
            
            // 데이터 플랫폼 전송 처리 (Mock)
            sendToDataPlatform(createOrderDataDto(event))
            
            log.info("주문 생성 정보 데이터 플랫폼 전송 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("주문 생성 정보 데이터 플랫폼 전송 실패: orderId={}, 사유={}", 
                event.orderId, e.message, e)
            // 데이터 플랫폼 전송은 중요하지만 핵심 비즈니스 흐름에 영향을 주지 않도록 예외 처리
        }
    }
    
    /**
     * 주문 완료 이벤트를 받아 데이터 플랫폼으로 전송
     * - 트랜잭션 완료 후 비동기 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Completed::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Async
    fun handleOrderCompleted(event: OrderEvent.Completed) {
        try {
            log.info("주문 완료 정보 데이터 플랫폼 전송 시작: orderId={}", event.orderId)
            
            // 데이터 플랫폼 전송 처리 (Mock)
            sendToDataPlatform(createCompletedOrderDataDto(event))
            
            log.info("주문 완료 정보 데이터 플랫폼 전송 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("주문 완료 정보 데이터 플랫폼 전송 실패: orderId={}, 사유={}", 
                event.orderId, e.message, e)
        }
    }
    
    /**
     * 주문 취소 이벤트를 받아 데이터 플랫폼으로 전송
     * - 트랜잭션 완료 후 비동기 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Cancelled::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Async
    fun handleOrderCancelled(event: OrderEvent.Cancelled) {
        try {
            log.info("주문 취소 정보 데이터 플랫폼 전송 시작: orderId={}", event.orderId)
            
            // 데이터 플랫폼 전송 처리 (Mock)
            sendToDataPlatform(createCancelledOrderDataDto(event))
            
            log.info("주문 취소 정보 데이터 플랫폼 전송 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("주문 취소 정보 데이터 플랫폼 전송 실패: orderId={}, 사유={}", 
                event.orderId, e.message, e)
        }
    }
    
    /**
     * 데이터 플랫폼 전송 (현재는 Mock 구현)
     * 실제 구현 시 REST API 호출 등으로 대체
     */
    private fun sendToDataPlatform(data: OrderDataDto) {
        // 실제 구현 시 API 호출 코드 작성
        // - REST API 호출 등으로 대체
        log.info("데이터 플랫폼으로 주문 데이터 전송 (Mock): {}", data)
    }
    
    private fun createOrderDataDto(event: OrderEvent.Created): OrderDataDto {
        return OrderDataDto(
            orderId = event.orderId,
            userId = event.userId,
            status = "CREATED",
            totalAmount = event.totalPrice,
            items = event.orderItems.map { item ->
                OrderItemDto(
                    productId = item.productId,
                    productOptionId = item.productOptionId,
                    quantity = item.quantity,
                    price = item.price
                )
            },
            timestamp = LocalDateTime.now()
        )
    }
    
    private fun createCompletedOrderDataDto(event: OrderEvent.Completed): OrderDataDto {
        return OrderDataDto(
            orderId = event.orderId,
            userId = event.userId,
            status = "COMPLETED",
            totalAmount = event.totalPrice,
            items = event.orderItems.map { item ->
                OrderItemDto(
                    productId = item.productId,
                    productOptionId = item.productOptionId,
                    quantity = item.quantity,
                    price = item.price
                )
            },
            timestamp = event.completedAt
        )
    }
    
    private fun createCancelledOrderDataDto(event: OrderEvent.Cancelled): OrderDataDto {
        return OrderDataDto(
            orderId = event.orderId,
            userId = event.userId,
            status = "CANCELLED",
            totalAmount = event.totalPrice,
            items = event.orderItems.map { item ->
                OrderItemDto(
                    productId = item.productId,
                    productOptionId = item.productOptionId,
                    quantity = item.quantity,
                    price = item.price
                )
            },
            timestamp = event.cancelledAt
        )
    }
}

/**
 * 데이터 플랫폼으로 전송할 DTO
 */
data class OrderDataDto(
    val orderId: Long,
    val userId: Long,
    val status: String,
    val totalAmount: Double,
    val items: List<OrderItemDto>,
    val timestamp: LocalDateTime
)

data class OrderItemDto(
    val productId: Long,
    val productOptionId: Long,
    val quantity: Int,
    val price: Double
) 