package kr.hhplus.be.server.domain.order.model

import java.time.LocalDateTime

enum class OrderStatus {
    PENDING, // 주문 대기
    COMPLETED, // 주문 완료
    CANCELLED // 주문 취소
}

data class Order private constructor(
    val id: Long,
    val accountId: Long,
    val accountCouponId: Long?,
    var totalPrice: Double,
    var status: OrderStatus,
    var orderDate: LocalDateTime,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
){
    companion object {
        fun create(id: Long, accountId: Long, accountCouponId: Long?, status: OrderStatus = OrderStatus.PENDING, totalPrice: Double): Order {
            return Order(id, accountId, accountCouponId, totalPrice, status, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun updateStatus(status: OrderStatus) {
        this.status = status
        if (status == OrderStatus.COMPLETED) this.orderDate = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    fun updateTotalPrice(totalPrice: Double, discountRate: Double? = null) {
        this.totalPrice = discountRate?.let { totalPrice * (1 - it / 100) } ?: totalPrice
        this.updatedAt = LocalDateTime.now()
    }
    
    fun isCancellable(): Boolean {
        return status == OrderStatus.PENDING
    }
}

