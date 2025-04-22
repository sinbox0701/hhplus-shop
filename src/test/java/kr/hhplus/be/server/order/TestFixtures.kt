package kr.hhplus.be.server.order

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
import java.time.LocalDateTime

object TestFixtures {
    // 상수 정의
    const val USER_ID = 1L
    const val PRODUCT_ID = 1L
    const val PRODUCT_OPTION_ID = 1L
    const val ORDER_ID = 1L
    const val ORDER_ITEM_ID = 1L
    const val COUPON_ID = 1L
    const val USER_COUPON_ID = 1L
    const val ACCOUNT_ID = 1L
    const val PRODUCT_PRICE = 10000.0
    const val OPTION_PRICE = 1000.0
    const val QUANTITY = 2
    const val DISCOUNT_RATE = 10.0

    // 사용자 Fixture
    fun createUser(): User {
        val user = mockk<User>()
        every { user.id } returns USER_ID
        every { user.name } returns "테스트 유저"
        every { user.email } returns "test@example.com"
        return user
    }

    // 상품 Fixture
    fun createProduct(): Product {
        val product = mockk<Product>()
        every { product.id } returns PRODUCT_ID
        every { product.name } returns "테스트 상품"
        every { product.price } returns PRODUCT_PRICE
        every { product.description } returns "테스트 상품 설명"
        return product
    }

    // 상품 옵션 Fixture
    fun createProductOption(availableQuantity: Int = 10): ProductOption {
        val option = mockk<ProductOption>()
        every { option.id } returns PRODUCT_OPTION_ID
        every { option.productId } returns PRODUCT_ID
        every { option.name } returns "테스트 옵션"
        every { option.additionalPrice } returns OPTION_PRICE
        every { option.availableQuantity } returns availableQuantity
        return option
    }

    // 주문 Fixture
    fun createOrder(
        status: OrderStatus = OrderStatus.PENDING,
        userCouponId: Long? = null,
        totalPrice: Double = (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY
    ): Order {
        val order = mockk<Order>()
        every { order.id } returns ORDER_ID
        every { order.userId } returns USER_ID
        every { order.userCouponId } returns userCouponId
        every { order.totalPrice } returns totalPrice
        every { order.status } returns status
        every { order.isCancellable() } returns (status == OrderStatus.PENDING)
        return order
    }

    // 주문 항목 Fixture
    fun createOrderItem(): OrderItem {
        val orderItem = mockk<OrderItem>()
        every { orderItem.id } returns ORDER_ITEM_ID
        every { orderItem.orderId } returns ORDER_ID
        every { orderItem.productId } returns PRODUCT_ID
        every { orderItem.productOptionId } returns PRODUCT_OPTION_ID
        every { orderItem.quantity } returns QUANTITY
        every { orderItem.price } returns (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY
        return orderItem
    }

    // 쿠폰 Fixture
    fun createCoupon(isValid: Boolean = true): Coupon {
        val coupon = mockk<Coupon>()
        every { coupon.id } returns COUPON_ID
        every { coupon.couponType } returns CouponType.DISCOUNT_ORDER
        every { coupon.discountRate } returns DISCOUNT_RATE
        
        val now = LocalDateTime.now()
        every { coupon.startDate } returns now.minusDays(1)
        every { coupon.endDate } returns if (isValid) now.plusDays(1) else now.minusDays(1)
        every { coupon.isValid() } returns isValid
        
        return coupon
    }

    // 사용자 쿠폰 Fixture
    fun createUserCoupon(isIssued: Boolean = true, isUsed: Boolean = false): UserCoupon {
        val userCoupon = mockk<UserCoupon>()
        every { userCoupon.id } returns USER_COUPON_ID
        every { userCoupon.userId } returns USER_ID
        every { userCoupon.couponId } returns COUPON_ID
        every { userCoupon.isIssued() } returns isIssued
        every { userCoupon.isUsed() } returns isUsed
        return userCoupon
    }

    // 계좌 Fixture
    fun createAccount(amount: Double = 100000.0): Account {
        val account = mockk<Account>()
        every { account.id } returns ACCOUNT_ID
        every { account.userId } returns USER_ID
        every { account.amount } returns amount
        return account
    }
} 