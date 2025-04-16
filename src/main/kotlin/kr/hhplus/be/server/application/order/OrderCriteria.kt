package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemCommand
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.UserCommand
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.user.service.AccountCommand
class OrderCriteria{
    /**
    * 주문 상품 생성 요청 데이터 클래스
     */
    data class OrderItemCreateCriteria(
        val productId: Long,
        val productOptionId: Long,
        val quantity: Int,
        val userCouponId: Long? = null,
        val discountRate: Double? = null
    ){
        fun toOrderItemCommand(order: Order, product: Product, productOption: ProductOption, userCoupon: UserCoupon?): OrderItemCommand.CreateOrderItemCommand {
            return OrderItemCommand.CreateOrderItemCommand(order, product, productOption, quantity, userCoupon, discountRate)
        }
    }

    data class OrderCreateCriteria(
        val userId: Long,
        val orderItems: List<OrderItemCreateCriteria>,
        val userCouponId: Long? = null
    ){
        fun toOrderCommand(user: User, userCoupon: UserCoupon?): OrderCommand.CreateOrderCommand {
            return OrderCommand.CreateOrderCommand(
                user,
                userCoupon,
                totalPrice = 0.0
            )
        }

        fun toOrderTotalPriceUpdateCommand(orderId: Long, totalPrice: Double): OrderCommand.UpdateOrderTotalPriceCommand {
            return OrderCommand.UpdateOrderTotalPriceCommand(orderId, totalPrice)
        }
    }

    data class OrderPaymentCriteria(
        val orderId: Long,
        val userId: Long
    ){
        fun toOrderPaymentCommand(totalPrice: Double): OrderCommand.UpdateOrderTotalPriceCommand {
            return OrderCommand.UpdateOrderTotalPriceCommand(orderId, totalPrice)
        }

        fun toUpdateAccountCommand(totalPrice: Double): AccountCommand.UpdateAccountCommand {
            return AccountCommand.UpdateAccountCommand(userId, totalPrice)
        }
    }
}