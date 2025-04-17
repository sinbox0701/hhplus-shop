package kr.hhplus.be.server.order

import io.mockk.*
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class OrderServiceUnitTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setup() {
        orderRepository = mockk()
        orderService = OrderService(orderRepository)
    }

    @Test
    @DisplayName("주문을 성공적으로 생성한다")
    fun createOrderSuccess() {
        // given
        val userId = 1L
        val userCouponId = 3L
        val totalPrice = 10000.0

        val command = OrderCommand.CreateOrderCommand(
            userId = userId,
            userCouponId = userCouponId,
            totalPrice = totalPrice
        )

        // Order 모킹 대신 실제 Order 객체가 생성된 후 저장될 예상 결과 준비
        val savedOrder = Order.create(
            userId = userId,
            userCouponId = userCouponId,
            totalPrice = totalPrice
        )
        
        // repository에서 Order 저장 시 savedOrder를 반환하도록 설정
        every { orderRepository.save(any()) } returns savedOrder
        
        // when
        val result = orderService.createOrder(command)
        
        // then
        verify { orderRepository.save(any()) }
        assertEquals(userId, result.userId)
        assertEquals(userCouponId, result.userCouponId)
        assertEquals(OrderStatus.PENDING, result.status)
        assertEquals(totalPrice, result.totalPrice)
    }
    
    @Test
    @DisplayName("ID로 주문을 가져온다")
    fun getOrderByIdSuccess() {
        // given
        val orderId = 100L
        val userId = 1L
        val userCouponId = 3L
        val totalPrice = 10000.0

        // Order 모킹 대신 테스트용 Order 객체 생성
        val order = Order.create(
            userId = userId,
            userCouponId = userCouponId,
            totalPrice = totalPrice
        )
        // id 필드는 private이므로 리플렉션을 사용하거나, 이 테스트에서는 id 검증을 제외
        
        every { orderRepository.findById(orderId) } returns order
        
        // when
        val result = orderService.getOrder(orderId)
        
        // then
        verify { orderRepository.findById(orderId) }
        assertEquals(userId, result.userId)
        assertEquals(totalPrice, result.totalPrice)
    }
    
    @Test
    @DisplayName("ID로 주문을 가져올 때 없으면 예외가 발생한다")
    fun getOrderByIdNotFound() {
        // given
        val orderId = 100L
        
        every { orderRepository.findById(orderId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderService.getOrder(orderId)
        }
        
        verify { orderRepository.findById(orderId) }
        assertTrue(exception.message!!.contains("주문을 찾을 수 없습니다"))
    }
    
    @Test
    @DisplayName("계정 ID로 주문 목록을 가져온다")
    fun getOrdersByAccountIdSuccess() {
        // given
        val userId = 4L
        
        // Order 모킹 대신 테스트용 Order 객체 리스트 생성
        val orders = listOf(
            Order.create(userId = userId, userCouponId = null, totalPrice = 10000.0),
            Order.create(userId = userId, userCouponId = null, totalPrice = 20000.0)
        )
        
        every { orderRepository.findByUserId(userId) } returns orders
        
        // when
        val result = orderService.getOrdersByUserId(userId)
        
        // then
        verify { orderRepository.findByUserId(userId) }
        assertEquals(2, result.size)
        result.forEach { assertEquals(userId, it.userId) }
    }
    
    @Test
    @DisplayName("주문 상태별로 주문 목록을 가져온다")
    fun getOrdersByStatusSuccess() {
        // given
        val status = OrderStatus.PENDING
        
        // Order 모킹 대신 테스트용 Order 객체 리스트 생성
        val orders = listOf(
            Order.create(userId = 1L, userCouponId = null, totalPrice = 10000.0),
            Order.create(userId = 2L, userCouponId = null, totalPrice = 20000.0)
        )
        
        every { orderRepository.findByStatus(status) } returns orders
        
        // when
        val result = orderService.getOrdersByStatus(status)
        
        // then
        verify { orderRepository.findByStatus(status) }
        assertEquals(2, result.size)
        result.forEach { assertEquals(OrderStatus.PENDING, it.status) } // create 메소드의 기본값은 PENDING
    }
    
    @Test
    @DisplayName("계정 ID와 상태로 주문 목록을 가져온다")
    fun getOrdersByAccountIdAndStatusSuccess() {
        // given
        val userId = 4L
        val status = OrderStatus.PENDING
        
        // Order 모킹 대신 테스트용 Order 객체 리스트 생성
        val orders = listOf(
            Order.create(userId = userId, userCouponId = null, totalPrice = 10000.0),
            Order.create(userId = userId, userCouponId = null, totalPrice = 20000.0)
        )
        
        every { orderRepository.findByUserIdAndStatus(userId, status) } returns orders
        
        // when
        val result = orderService.getOrdersByUserIdAndStatus(userId, status)
        
        // then
        verify { orderRepository.findByUserIdAndStatus(userId, status) }
        assertEquals(2, result.size)
        result.forEach {
            assertEquals(userId, it.userId)
            assertEquals(OrderStatus.PENDING, it.status) // create 메소드의 기본값은 PENDING
        }
    }
    
    @Test
    @DisplayName("날짜 범위로 주문 목록을 가져온다")
    fun getOrdersByDateRangeSuccess() {
        // given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        
        // Order 모킹 대신 테스트용 Order 객체 리스트 생성
        val orders = listOf(
            Order.create(userId = 1L, userCouponId = null, totalPrice = 10000.0),
            Order.create(userId = 2L, userCouponId = null, totalPrice = 20000.0)
        )
        
        every { orderRepository.findByCreatedAtBetween(startDate, endDate) } returns orders
        
        // when
        val result = orderService.getOrdersByDateRange(startDate, endDate)
        
        // then
        verify { orderRepository.findByCreatedAtBetween(startDate, endDate) }
        assertEquals(2, result.size)
    }
    
    @Test
    @DisplayName("주문 상태를 성공적으로 업데이트한다")
    fun updateOrderStatusSuccess() {
        // given
        val orderId = 100L
        val userId = 1L
        val newStatus = OrderStatus.COMPLETED
        
        // 원본 Order 객체 생성
        val order = Order.create(
            userId = userId,
            userCouponId = null,
            totalPrice = 10000.0
        )
        
        // 업데이트된 Order 객체 생성
        val updatedOrder = order.updateStatus(newStatus)
        
        val command = OrderCommand.UpdateOrderStatusCommand(
            id = orderId,
            status = newStatus
        )
        
        every { orderRepository.findById(orderId) } returns order
        every { orderRepository.updateStatus(orderId, newStatus) } returns updatedOrder
        
        // when
        val result = orderService.updateOrderStatus(command)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateStatus(orderId, newStatus) }
        assertEquals(newStatus, result.status)
    }
    
    @Test
    @DisplayName("취소할 수 없는 주문을 취소하면 예외가 발생한다")
    fun cancelNonCancellableOrderFails() {
        // given
        val orderId = 100L
        val userId = 1L
        
        // Order 객체 생성 - COMPLETED 상태로 설정
        val completedOrder = Order.create(
            userId = userId,
            userCouponId = null,
            status = OrderStatus.COMPLETED,
            totalPrice = 10000.0
        )
        
        val command = OrderCommand.UpdateOrderStatusCommand(
            id = orderId,
            status = OrderStatus.CANCELLED
        )
        
        every { orderRepository.findById(orderId) } returns completedOrder
        
        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderService.updateOrderStatus(command)
        }
        
        verify { orderRepository.findById(orderId) }
        verify(exactly = 0) { orderRepository.updateStatus(any(), any()) }
        assertTrue(exception.message!!.contains("주문을 취소할 수 없습니다"))
    }
    
    @Test
    @DisplayName("주문을 성공적으로 취소한다")
    fun cancelOrderSuccess() {
        // given
        val orderId = 100L
        val userId = 1L
        
        // 원본 Order 객체 생성 - PENDING 상태
        val order = Order.create(
            userId = userId,
            userCouponId = null,
            totalPrice = 10000.0
        )
        
        // 취소된 Order 객체 생성
        val cancelledOrder = order.updateStatus(OrderStatus.CANCELLED)
        
        every { orderRepository.findById(orderId) } returns order
        every { orderRepository.updateStatus(orderId, OrderStatus.CANCELLED) } returns cancelledOrder
        
        // when
        val result = orderService.cancelOrder(orderId)
        
        // then
        verify { orderRepository.updateStatus(orderId, OrderStatus.CANCELLED) }
        assertEquals(OrderStatus.CANCELLED, result.status)
    }
    
    @Test
    @DisplayName("주문을 성공적으로 완료한다")
    fun completeOrderSuccess() {
        // given
        val orderId = 100L
        val userId = 1L
        
        // 원본 Order 객체 생성 - PENDING 상태
        val order = Order.create(
            userId = userId,
            userCouponId = null,
            totalPrice = 10000.0
        )
        
        // 완료된 Order 객체 생성
        val completedOrder = order.updateStatus(OrderStatus.COMPLETED)
        
        every { orderRepository.findById(orderId) } returns order
        every { orderRepository.updateStatus(orderId, OrderStatus.COMPLETED) } returns completedOrder
        
        // when
        val result = orderService.completeOrder(orderId)
        
        // then
        verify { orderRepository.updateStatus(orderId, OrderStatus.COMPLETED) }
        assertEquals(OrderStatus.COMPLETED, result.status)
    }
    
    @Test
    @DisplayName("주문 총 가격을 성공적으로 업데이트한다")
    fun updateOrderTotalPriceSuccess() {
        // given
        val orderId = 100L
        val userId = 1L
        val initialPrice = 10000.0
        val newTotalPrice = 20000.0
        
        // 원본 Order 객체 생성
        val order = Order.create(
            userId = userId,
            userCouponId = null,
            totalPrice = initialPrice
        )
        
        // 가격이 업데이트된 Order 객체 생성
        val updatedOrder = order.updateTotalPrice(newTotalPrice)
        
        val command = OrderCommand.UpdateOrderTotalPriceCommand(
            id = orderId,
            totalPrice = newTotalPrice
        )
        
        every { orderRepository.findById(orderId) } returns order
        every { orderRepository.updateTotalPrice(orderId, newTotalPrice) } returns updatedOrder
        
        // when
        val result = orderService.updateOrderTotalPrice(command)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateTotalPrice(orderId, newTotalPrice) }
        assertEquals(newTotalPrice, result.totalPrice)
    }
} 