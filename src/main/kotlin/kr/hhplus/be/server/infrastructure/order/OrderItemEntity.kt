package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.model.OrderItem
import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val orderId: Long,
    
    @Column(nullable = false)
    val productId: Long,
    
    @Column(nullable = false)
    val productOptionId: Long,
    
    @Column(nullable = true)
    val userCouponId: Long?,
    
    @Column(nullable = false)
    val quantity: Int,
    
    @Column(nullable = false)
    val price: Double,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(nullable = false)
    val updatedAt: LocalDateTime
) {
    fun toOrderItem(): OrderItem {
        // OrderItem의 private 생성자 접근을 위한 리플렉션 사용
        val orderItemClass = OrderItem::class.java
        val constructor = orderItemClass.getDeclaredConstructor(
            Long::class.java, Long::class.java, Long::class.java,
            Long::class.java, Long::class.java, Int::class.java,
            Double::class.java, LocalDateTime::class.java, LocalDateTime::class.java
        )
        constructor.isAccessible = true
        
        return constructor.newInstance(
            id, orderId, productId, productOptionId,
            userCouponId, quantity, price, createdAt, updatedAt
        )
    }
    
    companion object {
        fun fromOrderItem(orderItem: OrderItem): OrderItemEntity {
            // OrderItem의 private 필드 접근을 위한 리플렉션 사용
            val orderItemClass = OrderItem::class.java
            
            val idField = orderItemClass.getDeclaredField("id")
            val orderIdField = orderItemClass.getDeclaredField("orderId")
            val productIdField = orderItemClass.getDeclaredField("productId")
            val productOptionIdField = orderItemClass.getDeclaredField("productOptionId")
            val userCouponIdField = orderItemClass.getDeclaredField("userCouponId")
            val quantityField = orderItemClass.getDeclaredField("quantity")
            val priceField = orderItemClass.getDeclaredField("price")
            val createdAtField = orderItemClass.getDeclaredField("createdAt")
            val updatedAtField = orderItemClass.getDeclaredField("updatedAt")
            
            idField.isAccessible = true
            orderIdField.isAccessible = true
            productIdField.isAccessible = true
            productOptionIdField.isAccessible = true
            userCouponIdField.isAccessible = true
            quantityField.isAccessible = true
            priceField.isAccessible = true
            createdAtField.isAccessible = true
            updatedAtField.isAccessible = true
            
            return OrderItemEntity(
                id = idField.get(orderItem) as? Long,
                orderId = orderIdField.get(orderItem) as Long,
                productId = productIdField.get(orderItem) as Long,
                productOptionId = productOptionIdField.get(orderItem) as Long,
                userCouponId = userCouponIdField.get(orderItem) as? Long,
                quantity = quantityField.get(orderItem) as Int,
                price = priceField.get(orderItem) as Double,
                createdAt = createdAtField.get(orderItem) as LocalDateTime,
                updatedAt = updatedAtField.get(orderItem) as LocalDateTime
            )
        }
    }
} 