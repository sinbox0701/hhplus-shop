package kr.hhplus.be.server.domain.order.service

class OrderItemCommand {
    data class CreateOrderItemCommand(val orderId: Long, val productId: Long, val productOptionId: Long, val quantity: Int, val userCouponId: Long?, val discountRate: Double?)
    data class UpdateOrderItemCommand(val id: Long, val quantity: Int, val productPrice: Double)
    data class UpdateOrderItemPriceCommand(val id: Long, val discountRate: Double?)
}