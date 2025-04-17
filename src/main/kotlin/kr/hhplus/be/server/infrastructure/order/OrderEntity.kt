package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val userId: Long,
    
    @Column(nullable = true)
    val userCouponId: Long?,
    
    @Column(nullable = false)
    val totalPrice: Double,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus,
    
    @Column(nullable = false)
    val orderDate: LocalDateTime,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toOrder(): Order {
        // Order의 private 생성자 접근을 위한 리플렉션 사용
        val orderClass = Order::class.java
        val constructor = orderClass.getDeclaredConstructor(
            Long::class.java, Long::class.java, Long::class.java,
            Double::class.java, OrderStatus::class.java, LocalDateTime::class.java,
            LocalDateTime::class.java, LocalDateTime::class.java
        )
        constructor.isAccessible = true
        
        return constructor.newInstance(
            id, userId, userCouponId, totalPrice,
            status, orderDate, createdAt, updatedAt
        )
    }
    
    companion object {
        fun fromOrder(order: Order): OrderEntity {
            // Order의 private 필드 접근을 위한 리플렉션 사용
            val orderClass = Order::class.java
            
            val idField = orderClass.getDeclaredField("id")
            val userIdField = orderClass.getDeclaredField("userId")
            val userCouponIdField = orderClass.getDeclaredField("userCouponId")
            val totalPriceField = orderClass.getDeclaredField("totalPrice")
            val statusField = orderClass.getDeclaredField("status")
            val orderDateField = orderClass.getDeclaredField("orderDate")
            val createdAtField = orderClass.getDeclaredField("createdAt")
            val updatedAtField = orderClass.getDeclaredField("updatedAt")
            
            idField.isAccessible = true
            userIdField.isAccessible = true
            userCouponIdField.isAccessible = true
            totalPriceField.isAccessible = true
            statusField.isAccessible = true
            orderDateField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true
            
            return OrderEntity(
                id = idField.get(order) as? Long,
                userId = userIdField.get(order) as Long,
                userCouponId = userCouponIdField.get(order) as? Long,
                totalPrice = totalPriceField.get(order) as Double,
                status = statusField.get(order) as OrderStatus,
                orderDate = orderDateField.get(order) as LocalDateTime,
                createdAt = createdAtField.get(order) as LocalDateTime,
                updatedAt = updatedAtField.get(order) as LocalDateTime
            )
        }
    }
} 