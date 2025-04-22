package kr.hhplus.be.server.domain.order.model

import kr.hhplus.be.server.domain.common.TimeProvider
import java.time.LocalDateTime

enum class OrderStatus {
    PENDING, // 주문 대기
    COMPLETED, // 주문 완료
    CANCELLED // 주문 취소
}

data class Order private constructor(
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    val userId: Long,
    val userCouponId: Long?,
    val totalPrice: Double,
    val status: OrderStatus,
    val orderDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
){
    companion object {
        fun create(userId: Long, userCouponId: Long?, status: OrderStatus = OrderStatus.PENDING, totalPrice: Double, timeProvider: TimeProvider): Order {
            val now = timeProvider.now()
            return Order(userId=userId, userCouponId=userCouponId, totalPrice=totalPrice, status=status, orderDate=now, createdAt=now, updatedAt=now)
        }
    }

    fun updateStatus(status: OrderStatus, timeProvider: TimeProvider): Order {
        val now = timeProvider.now()
        val newOrderDate = if (status == OrderStatus.COMPLETED) now else this.orderDate
        
        return Order(
            id = this.id,
            userId = this.userId,
            userCouponId = this.userCouponId,
            totalPrice = this.totalPrice,
            status = status,
            orderDate = newOrderDate,
            createdAt = this.createdAt,
            updatedAt = now
        )
    }

    fun updateTotalPrice(totalPrice: Double, timeProvider: TimeProvider): Order {
        return Order(
            id = this.id,
            userId = this.userId,
            userCouponId = this.userCouponId,
            totalPrice = totalPrice,
            status = this.status,
            orderDate = this.orderDate,
            createdAt = this.createdAt,
            updatedAt = timeProvider.now()
        )
    }
    
    fun isCancellable(): Boolean {
        return status == OrderStatus.PENDING
    }
}

