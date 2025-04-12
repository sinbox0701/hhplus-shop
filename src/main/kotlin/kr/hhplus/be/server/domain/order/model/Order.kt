package kr.hhplus.be.server.domain.order.model

import java.time.LocalDateTime
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
enum class OrderStatus {
    PENDING, // 주문 대기
    COMPLETED, // 주문 완료
    CANCELLED // 주문 취소
}

@Entity
@Table(name = "orders")
data class Order private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // 데이터베이스가 자동 생성하므로 null 허용
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_coupon_id")
    val userCoupon: UserCoupon?,
    
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
        fun create(user: User, userCoupon: UserCoupon?, status: OrderStatus = OrderStatus.PENDING, totalPrice: Double): Order {
            return Order(user=user, userCoupon=userCoupon, totalPrice=totalPrice, status=status, orderDate=LocalDateTime.now(), createdAt=LocalDateTime.now(), updatedAt=LocalDateTime.now())
        }
    }

    fun updateStatus(status: OrderStatus) {
        this.status = status
        if (status == OrderStatus.COMPLETED) this.orderDate = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    fun updateTotalPrice(totalPrice: Double) {
        this.totalPrice = totalPrice
        this.updatedAt = LocalDateTime.now()
    }
    
    fun isCancellable(): Boolean {
        return status == OrderStatus.CANCELLED
    }
}

