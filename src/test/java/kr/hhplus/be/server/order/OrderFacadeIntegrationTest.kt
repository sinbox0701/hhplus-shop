package kr.hhplus.be.server.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.application.order.OrderResult
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemCommand
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.domain.user.model.Account
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class OrderFacadeIntegrationTest {

    private lateinit var orderService: OrderService
    private lateinit var orderItemService: OrderItemService
    private lateinit var productService: ProductService
    private lateinit var productOptionService: ProductOptionService
    private lateinit var userService: UserService
    private lateinit var couponService: CouponService
    private lateinit var accountService: AccountService
    private lateinit var orderFacade: OrderFacade

    companion object {
        private const val USER_ID = 1L
        private const val PRODUCT_ID = 1L
        private const val PRODUCT_OPTION_ID = 1L
        private const val ORDER_ID = 1L
        private const val ORDER_ITEM_ID = 1L
        private const val COUPON_ID = 1L
        private const val USER_COUPON_ID = 1L
        private const val ACCOUNT_ID = 1L
        private const val PRODUCT_PRICE = 10000.0
        private const val OPTION_PRICE = 1000.0
        private const val QUANTITY = 2
        private const val DISCOUNT_RATE = 10.0
    }

    @BeforeEach
    fun setup() {
        orderService = mockk()
        orderItemService = mockk()
        productService = mockk()
        productOptionService = mockk()
        userService = mockk()
        couponService = mockk()
        accountService = mockk()
        orderFacade = OrderFacade(
            orderService,
            orderItemService,
            productService,
            productOptionService,
            userService,
            couponService,
            accountService
        )
    }

    private fun createMockUser(): User {
        val user = mockk<User>()
        every { user.id } returns USER_ID
        every { user.name } returns "테스트 유저"
        every { user.email } returns "test@example.com"
        return user
    }

    private fun createMockProduct(): Product {
        val product = mockk<Product>()
        every { product.id } returns PRODUCT_ID
        every { product.name } returns "테스트 상품"
        every { product.price } returns PRODUCT_PRICE
        every { product.description } returns "테스트 상품 설명"
        return product
    }

    private fun createMockProductOption(): ProductOption {
        val option = mockk<ProductOption>()
        every { option.id } returns PRODUCT_OPTION_ID
        every { option.productId } returns PRODUCT_ID
        every { option.name } returns "테스트 옵션"
        every { option.additionalPrice } returns OPTION_PRICE
        every { option.availableQuantity } returns 10
        return option
    }

    private fun createMockOrder(status: OrderStatus = OrderStatus.PENDING): Order {
        val order = mockk<Order>()
        every { order.id } returns ORDER_ID
        every { order.userId } returns USER_ID
        every { order.userCouponId } returns null
        every { order.totalPrice } returns (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY
        every { order.status } returns status
        every { order.isCancellable() } returns (status == OrderStatus.PENDING)
        return order
    }

    private fun createMockOrderItem(): OrderItem {
        val orderItem = mockk<OrderItem>()
        every { orderItem.id } returns ORDER_ITEM_ID
        every { orderItem.orderId } returns ORDER_ID
        every { orderItem.productId } returns PRODUCT_ID
        every { orderItem.productOptionId } returns PRODUCT_OPTION_ID
        every { orderItem.quantity } returns QUANTITY
        every { orderItem.price } returns (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY
        return orderItem
    }

    private fun createMockCoupon(): Coupon {
        val coupon = mockk<Coupon>()
        every { coupon.id } returns COUPON_ID
        every { coupon.couponType } returns CouponType.DISCOUNT_ORDER
        every { coupon.discountRate } returns DISCOUNT_RATE
        every { coupon.startDate } returns LocalDateTime.now().minusDays(1)
        every { coupon.endDate } returns LocalDateTime.now().plusDays(1)
        every { coupon.isValid() } returns true
        return coupon
    }

    private fun createMockUserCoupon(): UserCoupon {
        val userCoupon = mockk<UserCoupon>()
        every { userCoupon.id } returns USER_COUPON_ID
        every { userCoupon.userId } returns USER_ID
        every { userCoupon.couponId } returns COUPON_ID
        every { userCoupon.isIssued() } returns true
        every { userCoupon.isUsed() } returns false
        return userCoupon
    }

    private fun createMockAccount(amount: Double = 100000.0): Account {
        val account = mockk<Account>()
        every { account.id } returns ACCOUNT_ID
        every { account.userId } returns USER_ID
        every { account.amount } returns amount
        return account
    }

    @Test
    @DisplayName("주문 생성 성공")
    fun createOrderSuccess() {
        // given
        val user = createMockUser()
        val product = createMockProduct()
        val option = createMockProductOption()
        val order = createMockOrder()
        val orderItem = createMockOrderItem()

        val itemCriteria = OrderCriteria.OrderItemCreateCriteria(
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            quantity = QUANTITY
        )

        val criteria = OrderCriteria.OrderCreateCriteria(
            userId = USER_ID,
            orderItems = listOf(itemCriteria)
        )

        every { userService.findById(USER_ID) } returns user
        every { productService.get(PRODUCT_ID) } returns product
        every { productOptionService.get(PRODUCT_OPTION_ID) } returns option
        every { orderService.createOrder(any()) } returns order
        every { orderItemService.create(any()) } returns orderItem
        every { productOptionService.subtractQuantity(any()) } returns option
        every { orderService.updateOrderTotalPrice(any()) } returns order

        // when
        val result = orderFacade.createOrder(criteria)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)
        assertEquals(orderItem, result.items[0])

        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { productService.get(PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.get(PRODUCT_OPTION_ID) }
        verify(exactly = 1) { orderService.createOrder(any()) }
        verify(exactly = 1) { orderItemService.create(any()) }
        verify(exactly = 1) { productOptionService.subtractQuantity(any()) }
        verify(exactly = 1) { orderService.updateOrderTotalPrice(any()) }
    }

    @Test
    @DisplayName("쿠폰 적용 주문 생성 성공")
    fun createOrderWithCouponSuccess() {
        // given
        val user = createMockUser()
        val product = createMockProduct()
        val option = createMockProductOption()
        val coupon = createMockCoupon()
        val userCoupon = createMockUserCoupon()
        
        val order = mockk<Order>()
        every { order.id } returns ORDER_ID
        every { order.userId } returns USER_ID
        every { order.userCouponId } returns USER_COUPON_ID
        every { order.totalPrice } returns ((PRODUCT_PRICE + OPTION_PRICE) * QUANTITY) * (1 - DISCOUNT_RATE / 100)
        every { order.status } returns OrderStatus.PENDING
        
        val orderItem = createMockOrderItem()

        val itemCriteria = OrderCriteria.OrderItemCreateCriteria(
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            quantity = QUANTITY
        )

        val criteria = OrderCriteria.OrderCreateCriteria(
            userId = USER_ID,
            orderItems = listOf(itemCriteria),
            userCouponId = USER_COUPON_ID
        )

        every { userService.findById(USER_ID) } returns user
        every { couponService.findUserCouponById(USER_COUPON_ID) } returns userCoupon
        every { couponService.findById(COUPON_ID) } returns coupon
        every { productService.get(PRODUCT_ID) } returns product
        every { productOptionService.get(PRODUCT_OPTION_ID) } returns option
        every { orderService.createOrder(any()) } returns order
        every { orderItemService.create(any()) } returns orderItem
        every { productOptionService.subtractQuantity(any()) } returns option
        every { orderService.updateOrderTotalPrice(any()) } returns order

        // when
        val result = orderFacade.createOrder(criteria)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)
        assertEquals(orderItem, result.items[0])
        assertEquals(((PRODUCT_PRICE + OPTION_PRICE) * QUANTITY) * (1 - DISCOUNT_RATE / 100), result.order.totalPrice)

        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { couponService.findUserCouponById(USER_COUPON_ID) }
        verify(exactly = 1) { couponService.findById(COUPON_ID) }
        verify(exactly = 1) { productService.get(PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.get(PRODUCT_OPTION_ID) }
        verify(exactly = 1) { orderService.createOrder(any()) }
        verify(exactly = 1) { orderItemService.create(any()) }
        verify(exactly = 1) { productOptionService.subtractQuantity(any()) }
        verify(exactly = 1) { orderService.updateOrderTotalPrice(any()) }
    }

    @Test
    @DisplayName("재고 부족으로 주문 생성 실패")
    fun createOrderFailDueToInsufficientStock() {
        // given
        val user = createMockUser()
        val product = createMockProduct()
        
        val option = mockk<ProductOption>()
        every { option.id } returns PRODUCT_OPTION_ID
        every { option.productId } returns PRODUCT_ID
        every { option.name } returns "테스트 옵션"
        every { option.additionalPrice } returns OPTION_PRICE
        every { option.availableQuantity } returns 1  // 재고가 1개밖에 없음
        
        val itemCriteria = OrderCriteria.OrderItemCreateCriteria(
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            quantity = 2  // 2개 주문 시도
        )

        val criteria = OrderCriteria.OrderCreateCriteria(
            userId = USER_ID,
            orderItems = listOf(itemCriteria)
        )

        every { userService.findById(USER_ID) } returns user
        every { productService.get(PRODUCT_ID) } returns product
        every { productOptionService.get(PRODUCT_OPTION_ID) } returns option

        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderFacade.createOrder(criteria)
        }
        
        assertEquals("상품 옵션의 재고가 부족합니다: 테스트 옵션", exception.message)
        
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { productService.get(PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.get(PRODUCT_OPTION_ID) }
        verify(exactly = 0) { orderService.createOrder(any()) }
    }

    @Test
    @DisplayName("결제 처리 성공")
    fun processPaymentSuccess() {
        // given
        val user = createMockUser()
        val order = createMockOrder()
        val account = createMockAccount()
        val orderItem = createMockOrderItem()

        val criteria = OrderCriteria.OrderPaymentCriteria(
            orderId = ORDER_ID,
            userId = USER_ID
        )

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { accountService.findByUserId(USER_ID) } returns account
        every { accountService.withdraw(any()) } returns account
        every { orderService.completeOrder(ORDER_ID) } returns order
        every { orderItemService.getByOrderId(ORDER_ID) } returns listOf(orderItem)

        // when
        val result = orderFacade.processPayment(criteria)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)
        assertEquals(orderItem, result.items[0])

        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { accountService.findByUserId(USER_ID) }
        verify(exactly = 1) { accountService.withdraw(any()) }
        verify(exactly = 1) { orderService.completeOrder(ORDER_ID) }
        verify(exactly = 1) { orderItemService.getByOrderId(ORDER_ID) }
    }

    @Test
    @DisplayName("잔액 부족으로 결제 처리 실패")
    fun processPaymentFailDueToInsufficientBalance() {
        // given
        val user = createMockUser()
        val order = createMockOrder()
        
        // 잔액이 주문 금액보다 적게 설정
        val insufficientAmount = (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY - 1000.0
        val account = createMockAccount(insufficientAmount)

        val criteria = OrderCriteria.OrderPaymentCriteria(
            orderId = ORDER_ID,
            userId = USER_ID
        )

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { accountService.findByUserId(USER_ID) } returns account

        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderFacade.processPayment(criteria)
        }
        
        assertEquals("계좌 잔액이 부족합니다", exception.message)
        
        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { accountService.findByUserId(USER_ID) }
        verify(exactly = 0) { accountService.withdraw(any()) }
        verify(exactly = 0) { orderService.completeOrder(any()) }
    }

    @Test
    @DisplayName("주문 취소 성공")
    fun cancelOrderSuccess() {
        // given
        val user = createMockUser()
        val order = createMockOrder()
        val orderItem = createMockOrderItem()

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { orderItemService.getByOrderId(ORDER_ID) } returns listOf(orderItem)
        every { productOptionService.updateQuantity(any()) } returns createMockProductOption()
        every { orderService.cancelOrder(ORDER_ID) } returns order

        // when
        val result = orderFacade.cancelOrder(ORDER_ID, USER_ID)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)
        assertEquals(orderItem, result.items[0])

        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { orderItemService.getByOrderId(ORDER_ID) }
        verify(exactly = 1) { productOptionService.updateQuantity(any()) }
        verify(exactly = 1) { orderService.cancelOrder(ORDER_ID) }
    }

    @Test
    @DisplayName("취소 불가능한 주문 상태로 인한 취소 실패")
    fun cancelOrderFailDueToNonCancellableStatus() {
        // given
        val user = createMockUser()
        
        // 완료된 주문은 취소 불가능으로 설정
        val completedOrder = createMockOrder(OrderStatus.COMPLETED)
        
        every { orderService.getOrder(ORDER_ID) } returns completedOrder
        every { userService.findById(USER_ID) } returns user

        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderFacade.cancelOrder(ORDER_ID, USER_ID)
        }
        
        assertEquals("취소할 수 없는 주문 상태입니다: ${OrderStatus.COMPLETED}", exception.message)
        
        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 0) { orderItemService.getByOrderId(any()) }
        verify(exactly = 0) { productOptionService.updateQuantity(any()) }
        verify(exactly = 0) { orderService.cancelOrder(any()) }
    }

    @Test
    @DisplayName("부분 주문 취소 성공")
    fun cancelOrderItemSuccess() {
        // given
        val user = createMockUser()
        val order = createMockOrder()
        val orderItem = createMockOrderItem()
        val remainingItems = listOf(createMockOrderItem())

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { orderItemService.getById(ORDER_ITEM_ID) } returns orderItem
        every { productOptionService.updateQuantity(any()) } returns createMockProductOption()
        every { orderItemService.deleteById(ORDER_ITEM_ID) } returns Unit
        every { orderItemService.getByOrderId(ORDER_ID) } returns remainingItems
        every { orderItemService.calculateTotalPrice(any()) } returns (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY
        every { orderService.updateOrderTotalPrice(any()) } returns order

        // when
        val result = orderFacade.cancelOrderItem(ORDER_ID, ORDER_ITEM_ID, USER_ID)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)

        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { orderItemService.getById(ORDER_ITEM_ID) }
        verify(exactly = 1) { productOptionService.updateQuantity(any()) }
        verify(exactly = 1) { orderItemService.deleteById(ORDER_ITEM_ID) }
        verify(exactly = 1) { orderItemService.getByOrderId(ORDER_ID) }
        verify(exactly = 1) { orderItemService.calculateTotalPrice(any()) }
        verify(exactly = 1) { orderService.updateOrderTotalPrice(any()) }
    }

    @Test
    @DisplayName("주문과 상품 정보 조회 성공")
    fun getOrderWithItemsSuccess() {
        // given
        val user = createMockUser()
        val order = createMockOrder()
        val orderItems = listOf(createMockOrderItem())

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { orderItemService.getByOrderId(ORDER_ID) } returns orderItems

        // when
        val result = orderFacade.getOrderWithItems(ORDER_ID, USER_ID)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)
        assertEquals(orderItems[0], result.items[0])

        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { orderItemService.getByOrderId(ORDER_ID) }
    }

    @Test
    @DisplayName("사용자의 주문 목록 조회 성공")
    fun getAllOrdersByUserIdSuccess() {
        // given
        val user = createMockUser()
        val orders = listOf(createMockOrder(), createMockOrder())
        val orderItems = listOf(createMockOrderItem())

        every { userService.findById(USER_ID) } returns user
        every { orderService.getOrdersByUserId(USER_ID) } returns orders
        every { orderItemService.getByOrderId(ORDER_ID) } returns orderItems

        // when
        val results = orderFacade.getAllOrdersByUserId(USER_ID)

        // then
        assertNotNull(results)
        assertEquals(2, results.size)
        assertEquals(orders[0], results[0].order)
        assertEquals(1, results[0].items.size)

        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { orderService.getOrdersByUserId(USER_ID) }
        verify(exactly = 2) { orderItemService.getByOrderId(ORDER_ID) }
    }

    @Test
    @DisplayName("특정 상태의 주문 목록 조회 성공")
    fun getOrdersByUserIdAndStatusSuccess() {
        // given
        val user = createMockUser()
        val pendingOrders = listOf(createMockOrder(OrderStatus.PENDING))
        val orderItems = listOf(createMockOrderItem())

        every { userService.findById(USER_ID) } returns user
        every { orderService.getOrdersByUserIdAndStatus(USER_ID, OrderStatus.PENDING) } returns pendingOrders
        every { orderItemService.getByOrderId(ORDER_ID) } returns orderItems

        // when
        val results = orderFacade.getOrdersByUserIdAndStatus(USER_ID, OrderStatus.PENDING)

        // then
        assertNotNull(results)
        assertEquals(1, results.size)
        assertEquals(pendingOrders[0], results[0].order)
        assertEquals(OrderStatus.PENDING, results[0].order.status)
        assertEquals(1, results[0].items.size)

        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { orderService.getOrdersByUserIdAndStatus(USER_ID, OrderStatus.PENDING) }
        verify(exactly = 1) { orderItemService.getByOrderId(ORDER_ID) }
    }
}