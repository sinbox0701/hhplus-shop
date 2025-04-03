package kr.hhplus.be.server.domain.order

import java.time.LocalDateTime

enum class OrderStatus {
    PENDING,
    COMPLETED,
    CANCELLED
}

data class Order private constructor(
    val id: Long,
    val accountId: Long,
    val accountCouponId: Long,
    var totalPrice: Double,
    var status: OrderStatus,
    var orderDate: LocalDateTime,
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
){
    companion object {
        val MIN_TOTAL_PRICE = 100.0
        
        fun create(id: Long, accountId: Long, accountCouponId: Long, totalPrice: Double, status: OrderStatus): Order {
            require(totalPrice >= MIN_TOTAL_PRICE) { "총 가격은 $MIN_TOTAL_PRICE 이상이어야 합니다." }
            return Order(id, accountId, accountCouponId, totalPrice, status, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now())
        }
    }

    fun updateStatus(status: OrderStatus) {
        this.status = status
        this.updatedAt = LocalDateTime.now()
    }

    fun updateTotalPrice(totalPrice: Double) {
        require(totalPrice >= MIN_TOTAL_PRICE) { "총 가격은 $MIN_TOTAL_PRICE 이상이어야 합니다." }
        this.totalPrice = totalPrice
        this.updatedAt = LocalDateTime.now()
    }

    fun updateOrderDate(orderDate: LocalDateTime) {
        this.orderDate = orderDate
        this.updatedAt = LocalDateTime.now()
    }
    
    fun isCancellable(): Boolean {
        return status != OrderStatus.COMPLETED
    }
}

