package kr.hhplus.be.server.controller.order.dto.response

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderStatus
import java.time.LocalDateTime

data class OrderResponse(
    val id: Long,
    val accountId: Long,
    val accountCouponId: Long,
    val totalPrice: Double,
    val status: OrderStatus,
    val orderDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val orderItems: List<OrderItemResponse>? = null
) {
    companion object {
        fun of(order: Order): OrderResponse {
            return OrderResponse(
                id = order.id,
                accountId = order.accountId,
                accountCouponId = order.accountCouponId,
                totalPrice = order.totalPrice,
                status = order.status,
                orderDate = order.orderDate,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt
            )
        }
        
        fun of(order: Order, orderItems: List<OrderItem>): OrderResponse {
            return OrderResponse(
                id = order.id,
                accountId = order.accountId,
                accountCouponId = order.accountCouponId,
                totalPrice = order.totalPrice,
                status = order.status,
                orderDate = order.orderDate,
                createdAt = order.createdAt,
                updatedAt = order.updatedAt,
                orderItems = orderItems.map { OrderItemResponse.of(it) }
            )
        }
    }
}

data class OrderItemResponse(
    val id: Long,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun of(orderItem: OrderItem): OrderItemResponse {
            return OrderItemResponse(
                id = orderItem.id,
                orderId = orderItem.orderId,
                productId = orderItem.productId,
                quantity = orderItem.quantity,
                price = orderItem.price,
                createdAt = orderItem.createdAt,
                updatedAt = orderItem.updatedAt
            )
        }
    }
} 