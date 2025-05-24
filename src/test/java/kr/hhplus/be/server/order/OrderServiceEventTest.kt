package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.order.service.OrderService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderServiceEventTest {

    @Mock
    private lateinit var orderRepository: OrderRepository
    
    @Mock
    private lateinit var orderEventPublisher: OrderEventPublisher
    
    @Mock
    private lateinit var orderItemService: OrderItemService
    
    @Mock
    private lateinit var timeProvider: TimeProvider
    
    private lateinit var orderService: OrderService
    
    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<OrderEvent>
    
    private val testOrderId = 1L
    private val testUserId = 1L
    private val testTotalPrice = 1000.0
    
    @BeforeEach
    fun setup() {
        orderService = OrderService(
            orderRepository,
            orderEventPublisher,
            orderItemService,
            timeProvider
        )
        
        // 현재 시간 설정
        Mockito.`when`(timeProvider.now()).thenReturn(LocalDateTime.now())
    }
    
    @Test
    @DisplayName("주문 생성 시 이벤트가 발행되어야 한다")
    fun createOrderAndPublishEventTest() {
        // given
        val command = OrderCommand.CreateOrderCommand(
            userId = testUserId,
            userCouponId = null,
            totalPrice = testTotalPrice
        )
        
        val order = createMockOrder()
        val orderItems = createMockOrderItems()
        
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenReturn(order)
        
        // when
        val result = orderService.createOrderAndPublishEvent(command, orderItems)
        
        // then
        assertEquals(order, result)
        
        // 이벤트 발행 확인
        verify(orderEventPublisher).publish(eventCaptor.capture())
        
        val capturedEvent = eventCaptor.value
        assertEquals(OrderEvent.Created::class.java, capturedEvent.javaClass)
        
        val createdEvent = capturedEvent as OrderEvent.Created
        assertEquals(testOrderId, createdEvent.orderId)
        assertEquals(testUserId, createdEvent.userId)
        assertEquals(testTotalPrice, createdEvent.totalPrice)
        assertEquals(orderItems, createdEvent.orderItems)
    }
    
    @Test
    @DisplayName("주문 완료 시 이벤트가 발행되어야 한다")
    fun completeOrderTest() {
        // given
        val order = createMockOrder()
        val orderItems = createMockOrderItems()
        
        Mockito.`when`(orderRepository.findById(testOrderId)).thenReturn(order)
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenReturn(order)
        Mockito.`when`(orderItemService.getByOrderId(testOrderId)).thenReturn(orderItems)
        Mockito.`when`(order.status).thenReturn(OrderStatus.PENDING)
        
        // when
        val result = orderService.completeOrder(testOrderId)
        
        // then
        assertEquals(order, result)
        
        // 이벤트 발행 확인
        verify(orderEventPublisher).publish(eventCaptor.capture())
        
        val capturedEvent = eventCaptor.value
        assertEquals(OrderEvent.Completed::class.java, capturedEvent.javaClass)
        
        val completedEvent = capturedEvent as OrderEvent.Completed
        assertEquals(testOrderId, completedEvent.orderId)
        assertEquals(testUserId, completedEvent.userId)
        assertEquals(testTotalPrice, completedEvent.totalPrice)
        assertEquals(orderItems, completedEvent.orderItems)
    }
    
    @Test
    @DisplayName("주문 취소 시 이벤트가 발행되어야 한다")
    fun cancelOrderTest() {
        // given
        val order = createMockOrder()
        val orderItems = createMockOrderItems()
        
        Mockito.`when`(orderRepository.findById(testOrderId)).thenReturn(order)
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java))).thenReturn(order)
        Mockito.`when`(orderItemService.getByOrderId(testOrderId)).thenReturn(orderItems)
        Mockito.`when`(order.status).thenReturn(OrderStatus.PENDING)
        Mockito.`when`(order.isCancellable()).thenReturn(true)
        
        // when
        val result = orderService.cancelOrder(testOrderId)
        
        // then
        assertEquals(order, result)
        
        // 이벤트 발행 확인
        verify(orderEventPublisher).publish(eventCaptor.capture())
        
        val capturedEvent = eventCaptor.value
        assertEquals(OrderEvent.Cancelled::class.java, capturedEvent.javaClass)
        
        val cancelledEvent = capturedEvent as OrderEvent.Cancelled
        assertEquals(testOrderId, cancelledEvent.orderId)
        assertEquals(testUserId, cancelledEvent.userId)
        assertEquals(testTotalPrice, cancelledEvent.totalPrice)
        assertEquals(orderItems, cancelledEvent.orderItems)
        assertEquals(OrderStatus.PENDING, cancelledEvent.previousStatus)
    }
    
    @Test
    @DisplayName("주문 생성 실패 시 실패 이벤트가 발행되어야 한다")
    fun createOrderFailedEventTest() {
        // given
        val command = OrderCommand.CreateOrderCommand(
            userId = testUserId,
            userCouponId = null,
            totalPrice = testTotalPrice
        )
        
        // 예외 발생 시뮬레이션
        val exception = RuntimeException("주문 생성 실패")
        Mockito.`when`(orderRepository.save(Mockito.any(Order::class.java)))
            .thenThrow(exception)
        
        // when
        try {
            orderService.createOrder(command)
        } catch (e: Exception) {
            // 예외 무시 - 이벤트 발행 확인에 집중
        }
        
        // then
        verify(orderEventPublisher).publish(eventCaptor.capture())
        
        val capturedEvent = eventCaptor.value
        assertEquals(OrderEvent.Failed::class.java, capturedEvent.javaClass)
        
        val failedEvent = capturedEvent as OrderEvent.Failed
        assertEquals(testUserId, failedEvent.userId)
        assertEquals("주문 생성 실패", failedEvent.reason)
    }
    
    // 테스트 보조 메소드
    private fun createMockOrder(): Order {
        val order = Mockito.mock(Order::class.java)
        Mockito.`when`(order.id).thenReturn(testOrderId)
        Mockito.`when`(order.userId).thenReturn(testUserId)
        Mockito.`when`(order.totalPrice).thenReturn(testTotalPrice)
        Mockito.`when`(order.userCouponId).thenReturn(null)
        Mockito.`when`(order.createdAt).thenReturn(LocalDateTime.now())
        return order
    }
    
    private fun createMockOrderItems(): List<OrderItem> {
        val orderItem = Mockito.mock(OrderItem::class.java)
        Mockito.`when`(orderItem.orderId).thenReturn(testOrderId)
        Mockito.`when`(orderItem.productId).thenReturn(1L)
        Mockito.`when`(orderItem.productOptionId).thenReturn(1L)
        Mockito.`when`(orderItem.quantity).thenReturn(2)
        Mockito.`when`(orderItem.price).thenReturn(500.0)
        return listOf(orderItem)
    }
} 