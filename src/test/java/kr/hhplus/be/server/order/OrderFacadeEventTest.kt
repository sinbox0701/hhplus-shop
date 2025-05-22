package kr.hhplus.be.server.order

import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductService
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.user.service.AccountService
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderFacadeEventTest {

    @Mock
    private lateinit var orderService: OrderService
    
    @Mock
    private lateinit var orderItemService: OrderItemService
    
    @Mock
    private lateinit var productService: ProductService
    
    @Mock
    private lateinit var productOptionService: ProductOptionService
    
    @Mock
    private lateinit var userService: UserService
    
    @Mock
    private lateinit var couponService: CouponService
    
    @Mock
    private lateinit var accountService: AccountService
    
    @Mock
    private lateinit var transactionHelper: TransactionHelper
    
    @Mock
    private lateinit var orderEventPublisher: OrderEventPublisher
    
    private lateinit var orderFacade: OrderFacade
    
    private val testUserId = 1L
    private val testOrderId = 1L
    private val testProductId = 1L
    private val testProductOptionId = 1L
    private val testQuantity = 2
    private val testPrice = 1000.0
    
    @BeforeEach
    fun setup() {
        orderFacade = OrderFacade(
            orderService,
            orderItemService,
            productService,
            productOptionService,
            userService,
            couponService,
            transactionHelper
        )
        
        // transactionHelper 모의 처리
        Mockito.`when`(transactionHelper.executeInTransaction<Any>(Mockito.any())).thenAnswer { invocation ->
            val callable = invocation.getArgument<() -> Any>(0)
            callable.invoke()
        }
    }
    
    @Test
    @DisplayName("주문 생성 시 이벤트 기반 메소드가 호출되어야 한다")
    fun createOrderShouldUseEventBasedMethod() {
        // given
        val user = createMockUser()
        val product = createMockProduct()
        val productOption = createMockProductOption()
        val order = createMockOrder()
        val orderItem = createMockOrderItem()
        
        // 주문 생성 요청 생성
        val itemCriteria = OrderCriteria.OrderItemCreateCriteria(
            productId = testProductId,
            productOptionId = testProductOptionId,
            quantity = testQuantity
        )
        
        val criteria = OrderCriteria.OrderCreateCriteria(
            userId = testUserId,
            orderItems = listOf(itemCriteria)
        )
        
        // Mockito when 설정
        Mockito.`when`(userService.findById(testUserId)).thenReturn(user)
        Mockito.`when`(productService.get(testProductId)).thenReturn(product)
        Mockito.`when`(productOptionService.get(testProductOptionId)).thenReturn(productOption)
        Mockito.`when`(orderService.createOrderAndPublishEvent(Mockito.any(), Mockito.any())).thenReturn(order)
        Mockito.`when`(orderItemService.create(Mockito.any())).thenReturn(orderItem)
        
        // when
        orderFacade.createOrder(criteria)
        
        // then
        // createOrderAndPublishEvent 메소드가 호출되었는지 확인 (이벤트 기반 메소드)
        val orderCommandCaptor = ArgumentCaptor.forClass(OrderCommand.CreateOrderCommand::class.java)
        val orderItemsCaptor = ArgumentCaptor.forClass(List::class.java) as ArgumentCaptor<List<OrderItem>>
        
        Mockito.verify(orderService).createOrderAndPublishEvent(
            orderCommandCaptor.capture(),
            orderItemsCaptor.capture()
        )
        
        // 호출 인자 검증
        val capturedCommand = orderCommandCaptor.value
        assertEquals(testUserId, capturedCommand.userId)
    }
    
    @Test
    @DisplayName("주문 결제 시 이벤트 발행이 포함된 메소드가 호출되어야 한다")
    fun processPaymentShouldUseEventBasedMethod() {
        // given
        val user = createMockUser()
        val order = createMockOrder(OrderStatus.PENDING)
        val orderItems = listOf(createMockOrderItem())
        
        val criteria = OrderCriteria.OrderPaymentCriteria(
            orderId = testOrderId,
            userId = testUserId
        )
        
        // Mockito when 설정
        Mockito.`when`(orderService.getOrder(testOrderId)).thenReturn(order)
        Mockito.`when`(userService.findById(testUserId)).thenReturn(user)
        Mockito.`when`(accountService.findByUserId(testUserId)).thenReturn(null)
        Mockito.`when`(orderService.completeOrder(testOrderId)).thenReturn(order)
        Mockito.`when`(orderItemService.getByOrderId(testOrderId)).thenReturn(orderItems)
        
        // when
        orderFacade.processPayment(criteria)
        
        // then
        // completeOrder 메소드가 호출되었는지 확인 (이벤트 발행 포함)
        Mockito.verify(orderService).completeOrder(testOrderId)
    }
    
    @Test
    @DisplayName("주문 취소 시 이벤트 발행이 포함된 메소드가 호출되어야 한다")
    fun cancelOrderShouldUseEventBasedMethod() {
        // given
        val user = createMockUser()
        val order = createMockOrder(OrderStatus.PENDING)
        val orderItems = listOf(createMockOrderItem())
        
        // Mockito when 설정
        Mockito.`when`(orderService.getOrder(testOrderId)).thenReturn(order)
        Mockito.`when`(userService.findById(testUserId)).thenReturn(user)
        Mockito.`when`(orderService.cancelOrder(testOrderId)).thenReturn(order)
        Mockito.`when`(orderItemService.getByOrderId(testOrderId)).thenReturn(orderItems)
        
        // when
        orderFacade.cancelOrder(testOrderId, testUserId)
        
        // then
        // cancelOrder 메소드가 호출되었는지 확인 (이벤트 발행 포함)
        Mockito.verify(orderService).cancelOrder(testOrderId)
    }
    
    // 테스트 보조 메소드
    private fun createMockUser(): User {
        val user = Mockito.mock(User::class.java)
        Mockito.`when`(user.id).thenReturn(testUserId)
        return user
    }
    
    private fun createMockProduct(): Product {
        val product = Mockito.mock(Product::class.java)
        Mockito.`when`(product.id).thenReturn(testProductId)
        Mockito.`when`(product.price).thenReturn(testPrice)
        return product
    }
    
    private fun createMockProductOption(): ProductOption {
        val option = Mockito.mock(ProductOption::class.java)
        Mockito.`when`(option.id).thenReturn(testProductOptionId)
        Mockito.`when`(option.availableQuantity).thenReturn(10)
        Mockito.`when`(option.additionalPrice).thenReturn(0.0)
        return option
    }
    
    private fun createMockOrder(status: OrderStatus = OrderStatus.PENDING): Order {
        val order = Mockito.mock(Order::class.java)
        Mockito.`when`(order.id).thenReturn(testOrderId)
        Mockito.`when`(order.userId).thenReturn(testUserId)
        Mockito.`when`(order.totalPrice).thenReturn(testPrice * testQuantity)
        Mockito.`when`(order.status).thenReturn(status)
        Mockito.`when`(order.isCancellable()).thenReturn(status == OrderStatus.PENDING)
        Mockito.`when`(order.createdAt).thenReturn(LocalDateTime.now())
        return order
    }
    
    private fun createMockOrderItem(): OrderItem {
        val orderItem = Mockito.mock(OrderItem::class.java)
        Mockito.`when`(orderItem.orderId).thenReturn(testOrderId)
        Mockito.`when`(orderItem.productId).thenReturn(testProductId)
        Mockito.`when`(orderItem.productOptionId).thenReturn(testProductOptionId)
        Mockito.`when`(orderItem.quantity).thenReturn(testQuantity)
        Mockito.`when`(orderItem.price).thenReturn(testPrice * testQuantity)
        return orderItem
    }
} 