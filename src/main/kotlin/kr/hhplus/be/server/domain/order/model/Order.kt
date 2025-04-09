package kr.hhplus.be.server.domain.order.model

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table

enum class OrderStatus {
    PENDING, // 주문 대기
    COMPLETED, // 주문 완료
    CANCELLED // 주문 취소
}

@Entity
@Table(name = "orders")
data class Order private constructor(
    @Id
    val id: Long,
    
    @Column(nullable = false)
    val accountId: Long,
    
    @Column(nullable = true)
    val accountCouponId: Long?,
    
    @Column(nullable = false)
    var totalPrice: Double,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    
    @Column(nullable = false)
    var orderDate: LocalDateTime,
    
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    
    @Column(nullable = false)
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
        return status == OrderStatus.CANCELLED
    }
}

