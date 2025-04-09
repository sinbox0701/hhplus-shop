package kr.hhplus.be.server.order

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.order.service.OrderService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class OrderServiceUnitTest {

    @MockK
    private lateinit var orderRepository: OrderRepository

    @InjectMockKs
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    @DisplayName("주문을 성공적으로 생성한다")
    fun createOrderSuccess() {
        // given
        val accountId = 1L
        val accountCouponId = 2L
        val orderId = 100L
        val mockOrder = Order.create(orderId, accountId, accountCouponId, OrderStatus.PENDING, 0.0)
        
        every { orderRepository.save(any()) } returns mockOrder
        
        // when
        val result = orderService.createOrder(accountId, accountCouponId)
        
        // then
        verify { orderRepository.save(any()) }
        assertEquals(accountId, result.accountId)
        assertEquals(accountCouponId, result.accountCouponId)
        assertEquals(OrderStatus.PENDING, result.status)
        assertEquals(0.0, result.totalPrice)
    }
    
    @Test
    @DisplayName("ID로 주문을 가져온다")
    fun getOrderByIdSuccess() {
        // given
        val orderId = 100L
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, 1000.0)
        
        every { orderRepository.findById(orderId) } returns mockOrder
        
        // when
        val result = orderService.getOrder(orderId)
        
        // then
        verify { orderRepository.findById(orderId) }
        assertEquals(orderId, result.id)
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
        val accountId = 1L
        val mockOrders = listOf(
            Order.create(101L, accountId, null, OrderStatus.PENDING, 1000.0),
            Order.create(102L, accountId, null, OrderStatus.COMPLETED, 2000.0)
        )
        
        every { orderRepository.findByAccountId(accountId) } returns mockOrders
        
        // when
        val result = orderService.getOrdersByAccountId(accountId)
        
        // then
        verify { orderRepository.findByAccountId(accountId) }
        assertEquals(2, result.size)
        assertEquals(accountId, result[0].accountId)
        assertEquals(accountId, result[1].accountId)
    }
    
    @Test
    @DisplayName("주문 상태별로 주문 목록을 가져온다")
    fun getOrdersByStatusSuccess() {
        // given
        val status = OrderStatus.PENDING
        val mockOrders = listOf(
            Order.create(101L, 1L, null, status, 1000.0),
            Order.create(102L, 2L, null, status, 2000.0)
        )
        
        every { orderRepository.findByStatus(status) } returns mockOrders
        
        // when
        val result = orderService.getOrdersByStatus(status)
        
        // then
        verify { orderRepository.findByStatus(status) }
        assertEquals(2, result.size)
        assertEquals(status, result[0].status)
        assertEquals(status, result[1].status)
    }
    
    @Test
    @DisplayName("계정 ID와 상태로 주문 목록을 가져온다")
    fun getOrdersByAccountIdAndStatusSuccess() {
        // given
        val accountId = 1L
        val status = OrderStatus.PENDING
        val mockOrders = listOf(
            Order.create(101L, accountId, null, status, 1000.0),
            Order.create(102L, accountId, null, status, 2000.0)
        )
        
        every { orderRepository.findByAccountIdAndStatus(accountId, status) } returns mockOrders
        
        // when
        val result = orderService.getOrdersByAccountIdAndStatus(accountId, status)
        
        // then
        verify { orderRepository.findByAccountIdAndStatus(accountId, status) }
        assertEquals(2, result.size)
        assertEquals(accountId, result[0].accountId)
        assertEquals(status, result[0].status)
    }
    
    @Test
    @DisplayName("날짜 범위로 주문 목록을 가져온다")
    fun getOrdersByDateRangeSuccess() {
        // given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val mockOrders = listOf(
            Order.create(101L, 1L, null, OrderStatus.PENDING, 1000.0),
            Order.create(102L, 2L, null, OrderStatus.COMPLETED, 2000.0)
        )
        
        every { orderRepository.findByCreatedAtBetween(startDate, endDate) } returns mockOrders
        
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
        val newStatus = OrderStatus.COMPLETED
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, 1000.0)
        val updatedMockOrder = Order.create(orderId, 1L, null, newStatus, 1000.0)
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateStatus(orderId, newStatus) } returns updatedMockOrder
        
        // when
        val result = orderService.updateOrderStatus(orderId, newStatus)
        
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
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.COMPLETED, 1000.0).apply {
            status = OrderStatus.COMPLETED // 완료된 주문은 isCancellable()이 false 반환
        }
        
        every { orderRepository.findById(orderId) } returns mockOrder
        
        // when & then
        val exception = assertThrows<IllegalStateException> {
            orderService.cancelOrder(orderId)
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
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, 1000.0)
        val cancelledMockOrder = Order.create(orderId, 1L, null, OrderStatus.CANCELLED, 1000.0)
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateStatus(orderId, OrderStatus.CANCELLED) } returns cancelledMockOrder
        
        // when
        val result = orderService.cancelOrder(orderId)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateStatus(orderId, OrderStatus.CANCELLED) }
        assertEquals(OrderStatus.CANCELLED, result.status)
    }
    
    @Test
    @DisplayName("주문을 성공적으로 완료한다")
    fun completeOrderSuccess() {
        // given
        val orderId = 100L
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, 1000.0)
        val completedMockOrder = Order.create(orderId, 1L, null, OrderStatus.COMPLETED, 1000.0)
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateStatus(orderId, OrderStatus.COMPLETED) } returns completedMockOrder
        
        // when
        val result = orderService.completeOrder(orderId)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateStatus(orderId, OrderStatus.COMPLETED) }
        assertEquals(OrderStatus.COMPLETED, result.status)
    }
    
    @Test
    @DisplayName("주문 총 가격을 성공적으로 업데이트한다")
    fun updateOrderTotalPriceSuccess() {
        // given
        val orderId = 100L
        val newTotalPrice = 2000.0
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, 1000.0)
        val updatedMockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, newTotalPrice)
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateTotalPrice(orderId, newTotalPrice, null) } returns updatedMockOrder
        
        // when
        val result = orderService.updateOrderTotalPrice(orderId, newTotalPrice)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateTotalPrice(orderId, newTotalPrice, null) }
        assertEquals(newTotalPrice, result.totalPrice)
    }
    
    @Test
    @DisplayName("할인율과 함께 주문 총 가격을 업데이트한다")
    fun updateOrderTotalPriceWithDiscountSuccess() {
        // given
        val orderId = 100L
        val newTotalPrice = 2000.0
        val discountRate = 10.0
        val mockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, 1000.0)
        val expectedPrice = newTotalPrice * (1 - discountRate / 100)
        val updatedMockOrder = Order.create(orderId, 1L, null, OrderStatus.PENDING, expectedPrice)
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateTotalPrice(orderId, newTotalPrice, discountRate) } returns updatedMockOrder
        
        // when
        val result = orderService.updateOrderTotalPrice(orderId, newTotalPrice, discountRate)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateTotalPrice(orderId, newTotalPrice, discountRate) }
        assertEquals(expectedPrice, result.totalPrice)
    }
} 