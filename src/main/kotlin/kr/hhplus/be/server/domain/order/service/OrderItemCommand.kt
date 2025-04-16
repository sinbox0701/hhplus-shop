package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.coupon.model.UserCoupon

class OrderItemCommand {
    data class CreateOrderItemCommand(val order: Order, val product: Product, val productOption: ProductOption, val quantity: Int, val userCoupon: UserCoupon?, val discountRate: Double?)
    data class UpdateOrderItemCommand(val id: Long, val quantity: Int, val productPrice: Double)
    data class UpdateOrderItemPriceCommand(val id: Long, val price: Double)
}