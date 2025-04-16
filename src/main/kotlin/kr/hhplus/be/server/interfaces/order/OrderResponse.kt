package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.domain.order.model.OrderStatus
import java.time.LocalDateTime

class OrderResponse{
    data class Response(
        val id: Long,
        val userId: Long,
        val userCouponId: Long?,
        val totalPrice: Double,
        val status: OrderStatus,
        val orderDate: LocalDateTime,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val items: List<OrderItemResponse>
    ) 
    data class OrderItemResponse(
        val id: Long,
        val orderId: Long,
        val productId: Long,
        val productOptionId: Long,
        val userCouponId: Long?,
        val quantity: Int,
        val price: Double,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
}