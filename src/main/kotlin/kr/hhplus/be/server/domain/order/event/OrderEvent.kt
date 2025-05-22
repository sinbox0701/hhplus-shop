package kr.hhplus.be.server.domain.order.event

import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import java.time.LocalDateTime

/**
 * 주문 도메인에서 발생하는 이벤트 정의
 */
sealed class OrderEvent {
    /**
     * 주문 생성 이벤트
     * - 상품 재고 차감, 랭킹 업데이트 등 부가 기능에 사용됨
     */
    data class Created(
        val orderId: Long,
        val userId: Long,
        val userCouponId: Long?,
        val orderItems: List<OrderItem>,
        val totalPrice: Double,
        val createdAt: LocalDateTime
    ) : OrderEvent()

    /**
     * 주문 결제 완료 이벤트
     * - 쿠폰 사용, 계좌 처리, 데이터 플랫폼 전송 등에 사용됨
     */
    data class Completed(
        val orderId: Long,
        val userId: Long,
        val userCouponId: Long?,
        val totalPrice: Double,
        val orderItems: List<OrderItem>,
        val completedAt: LocalDateTime
    ) : OrderEvent()

    /**
     * 주문 취소 이벤트
     */
    data class Cancelled(
        val orderId: Long,
        val userId: Long,
        val userCouponId: Long?,
        val orderItems: List<OrderItem>,
        val totalPrice: Double,
        val previousStatus: OrderStatus,
        val cancelledAt: LocalDateTime
    ) : OrderEvent()

    /**
     * 주문 상품 취소 이벤트
     */
    data class OrderItemCancelled(
        val orderId: Long,
        val userId: Long,
        val orderItemId: Long,
        val productId: Long,
        val productOptionId: Long,
        val quantity: Int,
        val price: Double,
        val cancelledAt: LocalDateTime
    ) : OrderEvent()

    /**
     * 주문 실패 이벤트
     */
    data class Failed(
        val userId: Long,
        val reason: String,
        val failedAt: LocalDateTime
    ) : OrderEvent()
} 