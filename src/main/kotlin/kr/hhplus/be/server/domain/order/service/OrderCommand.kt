package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.order.model.OrderStatus

class OrderCommand {
    data class CreateOrderCommand(val account: Account, val accountCouponId: Long?, val totalPrice: Double)
    data class UpdateOrderStatusCommand(val id: Long, val status: OrderStatus)
    data class UpdateOrderTotalPriceCommand(val id: Long, val totalPrice: Double)
}