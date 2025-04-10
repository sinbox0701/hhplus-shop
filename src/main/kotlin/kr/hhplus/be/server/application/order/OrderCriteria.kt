package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemCommand
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.user.service.AccountCommand
class OrderCriteria{
    /**
    * 주문 상품 생성 요청 데이터 클래스
     */
    data class OrderItemCreateCriteria(
        val productId: Long,
        val productOptionId: Long,
        val quantity: Int,
        val accountCouponId: Long? = null,
        val discountRate: Double? = null
    ){
        fun toOrderItemCommand(order: Order, product: Product, productOption: ProductOption): OrderItemCommand.CreateOrderItemCommand {
            return OrderItemCommand.CreateOrderItemCommand(order, product, productOption, quantity, accountCouponId, discountRate)
        }
    }

    data class OrderCreateCriteria(
        val accountId: Long,
        val items: List<OrderItemCreateCriteria>,
        val accountCouponId: Long? = null,
        val orderItems: List<OrderItemCreateCriteria>
    ){
        fun toOrderCommand(account: Account): OrderCommand.CreateOrderCommand {
            return OrderCommand.CreateOrderCommand(
                account,
                accountCouponId,
                totalPrice = 0.0
            )
        }

        fun toOrderTotalPriceUpdateCommand(orderId: Long, totalPrice: Double): OrderCommand.UpdateOrderTotalPriceCommand {
            return OrderCommand.UpdateOrderTotalPriceCommand(orderId, totalPrice)
        }
    }

    data class OrderPaymentCriteria(
        val orderId: Long,
        val accountId: Long
    ){
        fun toOrderPaymentCommand(totalPrice: Double): AccountCommand.UpdateAccountCommand {
            return AccountCommand.UpdateAccountCommand(accountId, totalPrice)
        }
    }
}