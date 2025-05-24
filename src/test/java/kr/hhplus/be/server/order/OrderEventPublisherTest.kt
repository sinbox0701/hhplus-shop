package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.infrastructure.event.SpringOrderEventPublisher
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
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderEventPublisherTest {

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    private lateinit var orderEventPublisher: OrderEventPublisher

    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<OrderEvent>

    @BeforeEach
    fun setup() {
        orderEventPublisher = SpringOrderEventPublisher(applicationEventPublisher)
    }

    @Test
    @DisplayName("주문 생성 이벤트 발행 테스트")
    fun publishOrderCreatedEventTest() {
        // given
        val orderItems = listOf(createOrderItem(1L, 1L, 2, 1000.0))
        
        val event = OrderEvent.Created(
            orderId = 1L,
            userId = 1L,
            userCouponId = null,
            totalPrice = 2000.0,
            orderItems = orderItems,
            createdAt = LocalDateTime.now()
        )

        // when
        orderEventPublisher.publish(event)

        // then
        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture())
        val capturedEvent = eventCaptor.value as OrderEvent.Created
        
        assertEquals(event.orderId, capturedEvent.orderId)
        assertEquals(event.userId, capturedEvent.userId)
        assertEquals(event.totalPrice, capturedEvent.totalPrice)
    }

    @Test
    @DisplayName("주문 완료 이벤트 발행 테스트")
    fun publishOrderCompletedEventTest() {
        // given
        val orderItems = listOf(createOrderItem(1L, 1L, 2, 1000.0))
        
        val event = OrderEvent.Completed(
            orderId = 1L,
            userId = 1L,
            userCouponId = null,
            totalPrice = 2000.0,
            orderItems = orderItems,
            completedAt = LocalDateTime.now()
        )

        // when
        orderEventPublisher.publish(event)

        // then
        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture())
        val capturedEvent = eventCaptor.value as OrderEvent.Completed
        
        assertEquals(event.orderId, capturedEvent.orderId)
        assertEquals(event.userId, capturedEvent.userId)
        assertEquals(event.totalPrice, capturedEvent.totalPrice)
    }

    @Test
    @DisplayName("주문 취소 이벤트 발행 테스트")
    fun publishOrderCancelledEventTest() {
        // given
        val orderItems = listOf(createOrderItem(1L, 1L, 2, 1000.0))
        
        val event = OrderEvent.Cancelled(
            orderId = 1L,
            userId = 1L,
            userCouponId = null,
            totalPrice = 2000.0,
            orderItems = orderItems,
            previousStatus = OrderStatus.PENDING,
            cancelledAt = LocalDateTime.now()
        )

        // when
        orderEventPublisher.publish(event)

        // then
        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture())
        val capturedEvent = eventCaptor.value as OrderEvent.Cancelled
        
        assertEquals(event.orderId, capturedEvent.orderId)
        assertEquals(event.userId, capturedEvent.userId)
        assertEquals(event.totalPrice, capturedEvent.totalPrice)
        assertEquals(event.previousStatus, capturedEvent.previousStatus)
    }
    
    private fun createOrderItem(productId: Long, productOptionId: Long, quantity: Int, price: Double): OrderItem {
        val orderItem = Mockito.mock(OrderItem::class.java)
        Mockito.`when`(orderItem.productId).thenReturn(productId)
        Mockito.`when`(orderItem.productOptionId).thenReturn(productOptionId)
        Mockito.`when`(orderItem.quantity).thenReturn(quantity)
        Mockito.`when`(orderItem.price).thenReturn(price)
        return orderItem
    }
} 