package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.interfaces.order.event.OrderDataPlatformEventListener
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.Logger
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@ExtendWith(MockitoExtension::class)
class OrderDataPlatformEventListenerTest {

    @InjectMocks
    private lateinit var eventListener: OrderDataPlatformEventListener

    @Test
    @DisplayName("주문 생성 이벤트 처리 테스트")
    fun handleOrderCreatedTest() {
        // 로그 모킹을 위한 설정
        val mockLogger = org.mockito.Mockito.mock(Logger::class.java)
        ReflectionTestUtils.setField(eventListener, "log", mockLogger)
        
        // 이벤트 처리 완료 확인용 래치
        val latch = CountDownLatch(1)
        
        // 테스트용 이벤트 생성
        val createdEvent = createOrderCreatedEvent()
        
        // 이벤트 처리
        eventListener.handleOrderCreated(createdEvent)
        
        // 비동기 처리 대기
        latch.await(1, TimeUnit.SECONDS)
        
        // 로그 확인 (이 부분은 모킹된 로거를 통해 확인)
        org.mockito.Mockito.verify(mockLogger).info(
            org.mockito.ArgumentMatchers.contains("주문 생성 정보 데이터 플랫폼 전송 시작"),
            org.mockito.ArgumentMatchers.eq(createdEvent.orderId)
        )
    }
    
    @Test
    @DisplayName("주문 완료 이벤트 처리 테스트")
    fun handleOrderCompletedTest() {
        // 로그 모킹을 위한 설정
        val mockLogger = org.mockito.Mockito.mock(Logger::class.java)
        ReflectionTestUtils.setField(eventListener, "log", mockLogger)
        
        // 이벤트 처리 완료 확인용 래치
        val latch = CountDownLatch(1)
        
        // 테스트용 이벤트 생성
        val completedEvent = createOrderCompletedEvent()
        
        // 이벤트 처리
        eventListener.handleOrderCompleted(completedEvent)
        
        // 비동기 처리 대기
        latch.await(1, TimeUnit.SECONDS)
        
        // 로그 확인
        org.mockito.Mockito.verify(mockLogger).info(
            org.mockito.ArgumentMatchers.contains("주문 완료 정보 데이터 플랫폼 전송 시작"),
            org.mockito.ArgumentMatchers.eq(completedEvent.orderId)
        )
    }
    
    @Test
    @DisplayName("주문 취소 이벤트 처리 테스트")
    fun handleOrderCancelledTest() {
        // 로그 모킹을 위한 설정
        val mockLogger = org.mockito.Mockito.mock(Logger::class.java)
        ReflectionTestUtils.setField(eventListener, "log", mockLogger)
        
        // 이벤트 처리 완료 확인용 래치
        val latch = CountDownLatch(1)
        
        // 테스트용 이벤트 생성
        val cancelledEvent = createOrderCancelledEvent()
        
        // 이벤트 처리
        eventListener.handleOrderCancelled(cancelledEvent)
        
        // 비동기 처리 대기
        latch.await(1, TimeUnit.SECONDS)
        
        // 로그 확인
        org.mockito.Mockito.verify(mockLogger).info(
            org.mockito.ArgumentMatchers.contains("주문 취소 정보 데이터 플랫폼 전송 시작"),
            org.mockito.ArgumentMatchers.eq(cancelledEvent.orderId)
        )
    }
    
    // 테스트용 이벤트 생성 헬퍼 메서드
    private fun createOrderCreatedEvent(): OrderEvent.Created {
        val orderItems = listOf(
            createOrderItem(1L, 1L, 2, 1000.0)
        )
        
        return OrderEvent.Created(
            orderId = 1L,
            userId = 1L,
            userCouponId = null,
            totalPrice = 2000.0,
            orderItems = orderItems,
            createdAt = LocalDateTime.now()
        )
    }
    
    private fun createOrderCompletedEvent(): OrderEvent.Completed {
        val orderItems = listOf(
            createOrderItem(1L, 1L, 2, 1000.0)
        )
        
        return OrderEvent.Completed(
            orderId = 1L,
            userId = 1L,
            userCouponId = null,
            totalPrice = 2000.0,
            orderItems = orderItems,
            completedAt = LocalDateTime.now()
        )
    }
    
    private fun createOrderCancelledEvent(): OrderEvent.Cancelled {
        val orderItems = listOf(
            createOrderItem(1L, 1L, 2, 1000.0)
        )
        
        return OrderEvent.Cancelled(
            orderId = 1L,
            userId = 1L,
            userCouponId = null,
            totalPrice = 2000.0,
            orderItems = orderItems,
            previousStatus = OrderStatus.PENDING,
            cancelledAt = LocalDateTime.now()
        )
    }
    
    private fun createOrderItem(productId: Long, productOptionId: Long, quantity: Int, price: Double): OrderItem {
        val orderItem = org.mockito.Mockito.mock(OrderItem::class.java)
        org.mockito.Mockito.`when`(orderItem.productId).thenReturn(productId)
        org.mockito.Mockito.`when`(orderItem.productOptionId).thenReturn(productOptionId)
        org.mockito.Mockito.`when`(orderItem.quantity).thenReturn(quantity)
        org.mockito.Mockito.`when`(orderItem.price).thenReturn(price)
        return orderItem
    }
} 