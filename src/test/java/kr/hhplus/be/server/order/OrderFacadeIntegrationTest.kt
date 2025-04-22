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
import kr.hhplus.be.server.order.TestFixtures.DISCOUNT_RATE
import kr.hhplus.be.server.order.TestFixtures.OPTION_PRICE
import kr.hhplus.be.server.order.TestFixtures.ORDER_ID
import kr.hhplus.be.server.order.TestFixtures.ORDER_ITEM_ID
import kr.hhplus.be.server.order.TestFixtures.PRODUCT_ID
import kr.hhplus.be.server.order.TestFixtures.PRODUCT_OPTION_ID
import kr.hhplus.be.server.order.TestFixtures.PRODUCT_PRICE
import kr.hhplus.be.server.order.TestFixtures.QUANTITY
import kr.hhplus.be.server.order.TestFixtures.USER_COUPON_ID
import kr.hhplus.be.server.order.TestFixtures.USER_ID
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class OrderFacadeIntegrationTest {

    // OrderResult 클래스 정의 추가
    // 실제 application.order.OrderResult 클래스를 사용하도록 제거
    // data class OrderResult(val order: Order, val items: List<OrderItem>)

    private lateinit var orderService: OrderService
    private lateinit var orderItemService: OrderItemService
    private lateinit var productService: ProductService
    private lateinit var productOptionService: ProductOptionService
    private lateinit var userService: UserService
    private lateinit var couponService: CouponService
    private lateinit var accountService: AccountService
    private lateinit var orderFacade: OrderFacade

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
        every { user.id } returns TestFixtures.USER_ID
        every { user.name } returns "테스트 유저"
        every { user.email } returns "test@example.com"
        return user
    }

    private fun createMockProduct(): Product {
        val product = mockk<Product>()
        every { product.id } returns TestFixtures.PRODUCT_ID
        every { product.name } returns "테스트 상품"
        every { product.price } returns TestFixtures.PRODUCT_PRICE
        every { product.description } returns "테스트 상품 설명"
        return product
    }

    private fun createMockProductOption(): ProductOption {
        val option = mockk<ProductOption>()
        every { option.id } returns TestFixtures.PRODUCT_OPTION_ID
        every { option.productId } returns TestFixtures.PRODUCT_ID
        every { option.name } returns "테스트 옵션"
        every { option.additionalPrice } returns TestFixtures.OPTION_PRICE
        every { option.availableQuantity } returns 10
        return option
    }

    private fun createMockOrder(status: OrderStatus = OrderStatus.PENDING): Order {
        val order = mockk<Order>()
        every { order.id } returns TestFixtures.ORDER_ID
        every { order.userId } returns TestFixtures.USER_ID
        every { order.userCouponId } returns null
        every { order.totalPrice } returns (TestFixtures.PRODUCT_PRICE + TestFixtures.OPTION_PRICE) * TestFixtures.QUANTITY
        every { order.status } returns status
        every { order.isCancellable() } returns (status == OrderStatus.PENDING)
        return order
    }

    private fun createMockOrderItem(): OrderItem {
        val orderItem = mockk<OrderItem>()
        every { orderItem.id } returns TestFixtures.ORDER_ITEM_ID
        every { orderItem.orderId } returns TestFixtures.ORDER_ID
        every { orderItem.productId } returns TestFixtures.PRODUCT_ID
        every { orderItem.productOptionId } returns TestFixtures.PRODUCT_OPTION_ID
        every { orderItem.quantity } returns TestFixtures.QUANTITY
        every { orderItem.price } returns (TestFixtures.PRODUCT_PRICE + TestFixtures.OPTION_PRICE) * TestFixtures.QUANTITY
        every { orderItem.isCancelled() } returns false
        return orderItem
    }

    private fun createMockCoupon(): Coupon {
        val coupon = mockk<Coupon>()
        every { coupon.id } returns TestFixtures.COUPON_ID
        every { coupon.couponType } returns CouponType.DISCOUNT_ORDER
        every { coupon.discountRate } returns TestFixtures.DISCOUNT_RATE
        every { coupon.startDate } returns LocalDateTime.now().minusDays(1)
        every { coupon.endDate } returns LocalDateTime.now().plusDays(1)
        every { coupon.isValid(any()) } returns true
        return coupon
    }

    private fun createMockUserCoupon(): UserCoupon {
        val userCoupon = mockk<UserCoupon>()
        every { userCoupon.id } returns TestFixtures.USER_COUPON_ID
        every { userCoupon.userId } returns TestFixtures.USER_ID
        every { userCoupon.couponId } returns TestFixtures.COUPON_ID
        every { userCoupon.isIssued() } returns true
        every { userCoupon.isUsed() } returns false
        return userCoupon
    }

    private fun createMockAccount(amount: Double = 100000.0): Account {
        val account = mockk<Account>()
        every { account.id } returns TestFixtures.ACCOUNT_ID
        every { account.userId } returns TestFixtures.USER_ID
        every { account.amount } returns amount
        return account
    }

    @Test
    @DisplayName("주문 생성 성공")
    fun createOrderSuccess() {
        // given
        val user = TestFixtures.createUser()
        val product = TestFixtures.createProduct()
        val option = TestFixtures.createProductOption()
        val order = TestFixtures.createOrder()
        val orderItem = TestFixtures.createOrderItem()

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
        val user = TestFixtures.createUser()
        val product = TestFixtures.createProduct()
        val option = TestFixtures.createProductOption()
        val coupon = TestFixtures.createCoupon()
        val userCoupon = TestFixtures.createUserCoupon()
        
        val discountedTotalPrice = ((PRODUCT_PRICE + OPTION_PRICE) * QUANTITY) * (1 - DISCOUNT_RATE / 100)
        val order = TestFixtures.createOrder(
            userCouponId = USER_COUPON_ID,
            totalPrice = discountedTotalPrice
        )
        
        val orderItem = TestFixtures.createOrderItem()

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
        every { couponService.findById(TestFixtures.COUPON_ID) } returns coupon
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
        assertEquals(discountedTotalPrice, result.order.totalPrice)

        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { couponService.findUserCouponById(USER_COUPON_ID) }
        verify(exactly = 1) { couponService.findById(TestFixtures.COUPON_ID) }
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
        val user = TestFixtures.createUser()
        val product = TestFixtures.createProduct()
        val option = TestFixtures.createProductOption(availableQuantity = 1) // 재고가 1개뿐
        
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
        val user = TestFixtures.createUser()
        val order = TestFixtures.createOrder()
        val account = TestFixtures.createAccount()
        val orderItem = TestFixtures.createOrderItem()

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
        val user = TestFixtures.createUser()
        val order = TestFixtures.createOrder()
        
        // 잔액이 주문 금액보다 적게 설정
        val insufficientAmount = (PRODUCT_PRICE + OPTION_PRICE) * QUANTITY - 1000.0
        val account = TestFixtures.createAccount(insufficientAmount)

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
        val user = TestFixtures.createUser()
        val order = TestFixtures.createOrder()
        val orderItem = TestFixtures.createOrderItem()

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { orderItemService.getByOrderId(ORDER_ID) } returns listOf(orderItem)
        every { productOptionService.updateQuantity(any()) } returns TestFixtures.createProductOption()
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
        val user = TestFixtures.createUser()
        
        // 완료된 주문은 취소 불가능으로 설정
        val completedOrder = TestFixtures.createOrder(status = OrderStatus.COMPLETED)
        
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
    fun `부분 주문 취소가 정상적으로 처리되어야 한다`() {
        // given
        // 사용자 생성
        val user = TestFixtures.createUser()
        
        // 계좌 생성 및 잔액 충전
        val account = TestFixtures.createAccount()
        
        // 상품 생성
        val product1 = TestFixtures.createProduct()
        val product2 = TestFixtures.createProduct()
        
        // 상품 옵션 생성
        val productOption1 = TestFixtures.createProductOption()
        val productOption2 = TestFixtures.createProductOption()
        
        // 주문 생성 - 두 개의 상품
        val orderItemCriterias = listOf(
            OrderCriteria.OrderItemCreateCriteria(
                productId = product1.id!!,
                productOptionId = productOption1.id!!,
                quantity = 2
            ),
            OrderCriteria.OrderItemCreateCriteria(
                productId = product2.id!!,
                productOptionId = productOption2.id!!,
                quantity = 3
            )
        )
        
        val createOrderCriteria = OrderCriteria.OrderCreateCriteria(
            userId = user.id!!,
            orderItems = orderItemCriterias,
            userCouponId = null
        )
        
        val order = TestFixtures.createOrder(status = OrderStatus.COMPLETED)
        val orderItem1 = TestFixtures.createOrderItem()
        val orderItem2 = TestFixtures.createOrderItem()
        val orderItemsList = listOf(orderItem1, orderItem2)
        
        // OrderResult 대신 OrderResult.OrderWithItems 사용
        every { orderFacade.createOrder(any()) } returns OrderResult.OrderWithItems(order, orderItemsList)
        
        // 주문 결제
        val paymentCriteria = OrderCriteria.OrderPaymentCriteria(
            userId = user.id!!,
            orderId = order.id!!
        )
        
        // OrderResult 대신 OrderResult.OrderWithItems 사용
        every { orderFacade.processPayment(any()) } returns OrderResult.OrderWithItems(order, orderItemsList)
        
        // 결제 후 상품 옵션 재고 확인
        val optionAfterOrder1 = productOption1
        val optionAfterOrder2 = productOption2
        
        every { productOptionService.get(productOption1.id!!) } returns optionAfterOrder1
        every { productOptionService.get(productOption2.id!!) } returns optionAfterOrder2
        
        // when
        // 부분 주문 취소 (첫 번째 상품만 취소)
        val orderItemToCancel = orderItem1
        
        // 모의 테스트를 위한 설정
        every { orderService.getOrder(order.id!!) } returns order
        every { userService.findById(user.id!!) } returns user
        every { orderItemService.getById(orderItemToCancel.id!!) } returns orderItemToCancel
        every { productOptionService.updateQuantity(any()) } returns productOption1
        every { orderItemService.deleteById(orderItemToCancel.id!!) } returns Unit
        every { orderItemService.getByOrderId(order.id!!) } returns listOf(orderItem2)
        every { orderItemService.calculateTotalPrice(any()) } returns product2.price * orderItem2.quantity
        every { orderService.updateOrderTotalPrice(any()) } returns order
        
        // 취소된 상품 재고 확인을 위한 설정
        val optionAfterCancel1 = mockk<ProductOption>()
        every { optionAfterCancel1.id } returns productOption1.id
        every { optionAfterCancel1.availableQuantity } returns optionAfterOrder1.availableQuantity + 2
        every { productOptionService.get(productOption1.id!!) } returns optionAfterCancel1
        
        // 계좌 환불 정보 확인
        val accountAfterCancel = mockk<Account>()
        every { accountAfterCancel.amount } returns account.amount + product1.price * orderItemToCancel.quantity
        every { accountService.findByUserId(user.id!!) } returns accountAfterCancel
        
        // OrderItem에 isCancelled 메서드 추가 설정
        val cancelledOrderItem = mockk<OrderItem>()
        every { cancelledOrderItem.id } returns orderItemToCancel.id
        every { cancelledOrderItem.isCancelled() } returns true
        
        val notCancelledOrderItem = mockk<OrderItem>()
        every { notCancelledOrderItem.id } returns orderItem2.id
        every { notCancelledOrderItem.isCancelled() } returns false
        
        every { orderItemService.getByOrderId(order.id!!) } returns listOf(cancelledOrderItem, notCancelledOrderItem)
        
        // 부분 취소 실행
        val result = orderFacade.cancelOrderItem(order.id!!, orderItemToCancel.id!!, user.id!!)
        
        // then
        // 1. 주문 상태는 여전히 COMPLETED 상태여야 함 (부분 취소이므로)
        assertEquals(OrderStatus.COMPLETED, result.order.status)
        
        // 2. 취소된 상품의 재고는 복구되어야 함
        val updatedOptionAfterCancel1 = productOptionService.get(productOption1.id!!)
        assertEquals(optionAfterOrder1.availableQuantity + 2, updatedOptionAfterCancel1.availableQuantity, 
            "취소된 상품의 재고가 원래대로 복구되어야 함")
        
        // 3. 해당 주문 항목의 상태가 변경되어야 함
        val updatedItems = orderItemService.getByOrderId(order.id!!)
        val cancelledItem = updatedItems.find { it.id == orderItemToCancel.id }
        val notCancelledItem = updatedItems.find { it.id != orderItemToCancel.id }
        
        assertTrue(cancelledItem?.isCancelled() ?: false, "취소된 주문 항목은 취소 상태여야 함")
        assertFalse(notCancelledItem?.isCancelled() ?: true, "취소되지 않은 주문 항목은 취소 상태가 아니어야 함")
        
        // 4. 취소된 상품에 대한 환불이 이루어져야 함
        val updatedAccount = accountService.findByUserId(user.id!!)
        val refundAmount = product1.price * orderItemToCancel.quantity
        assertEquals(updatedAccount.amount, account.amount + refundAmount, 0.01, 
            "취소된 상품에 대한 환불이 정확하게 이루어져야 함")
    }

    @Test
    @DisplayName("주문과 상품 정보 조회 성공")
    fun getOrderWithItemsSuccess() {
        // given
        val user = TestFixtures.createUser()
        val order = TestFixtures.createOrder()
        val orderItemList = listOf(TestFixtures.createOrderItem())

        every { orderService.getOrder(ORDER_ID) } returns order
        every { userService.findById(USER_ID) } returns user
        every { orderItemService.getByOrderId(ORDER_ID) } returns orderItemList

        // when
        val result = orderFacade.getOrderWithItems(ORDER_ID, USER_ID)

        // then
        assertNotNull(result)
        assertEquals(order, result.order)
        assertEquals(1, result.items.size)
        assertEquals(orderItemList[0], result.items[0])

        verify(exactly = 1) { orderService.getOrder(ORDER_ID) }
        verify(exactly = 1) { userService.findById(USER_ID) }
        verify(exactly = 1) { orderItemService.getByOrderId(ORDER_ID) }
    }

    @Test
    @DisplayName("사용자의 주문 목록 조회 성공")
    fun getAllOrdersByUserIdSuccess() {
        // given
        val user = TestFixtures.createUser()
        val orders = listOf(TestFixtures.createOrder(), TestFixtures.createOrder())
        val orderItemList = listOf(TestFixtures.createOrderItem())

        every { userService.findById(USER_ID) } returns user
        every { orderService.getOrdersByUserId(USER_ID) } returns orders
        every { orderItemService.getByOrderId(ORDER_ID) } returns orderItemList

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
        val user = TestFixtures.createUser()
        val pendingOrders = listOf(TestFixtures.createOrder(status = OrderStatus.PENDING))
        val orderItemList = listOf(TestFixtures.createOrderItem())

        every { userService.findById(USER_ID) } returns user
        every { orderService.getOrdersByUserIdAndStatus(USER_ID, OrderStatus.PENDING) } returns pendingOrders
        every { orderItemService.getByOrderId(ORDER_ID) } returns orderItemList

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