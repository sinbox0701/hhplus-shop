
package kr.hhplus.be.server.interfaces.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import java.time.LocalDateTime

class OrderResponse {

    data class Response(
        val id: Long,
        val accountId: Long,
        val accountCouponId: Long?,
        val totalPrice: Double,
        val status: OrderStatus,
        val orderDate: LocalDateTime,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
        val items: List<OrderItemResponse>
    ) {
        companion object {
            fun of(order: Order, orderItems: List<OrderItem>): Response {
                return Response(
                    id = order.id,
                    accountId = order.accountId,
                    accountCouponId = order.accountCouponId,
                    totalPrice = order.totalPrice,
                    status = order.status,
                    orderDate = order.orderDate,
                    createdAt = order.createdAt,
                    updatedAt = order.updatedAt,
                    items = orderItems.map { OrderItemResponse.of(it) }
                )
            }
        }
    }

    data class OrderItemResponse(
        val id: Long,
        val orderId: Long,
        val productId: Long,
        val productOptionId: Long,
        val accountCouponId: Long?,
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
                    productOptionId = orderItem.productOptionId,
                    accountCouponId = orderItem.accountCouponId,
                    quantity = orderItem.quantity,
                    price = orderItem.price,
                    createdAt = orderItem.createdAt,
                    updatedAt = orderItem.updatedAt
                )
            }
        }
    } 
}