package kr.hhplus.be.server.order

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import kr.hhplus.be.server.domain.order.service.OrderCommand
import kr.hhplus.be.server.domain.order.service.OrderService
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.model.Coupon
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
        val user = mockk<User>{
            every { id } returns 1L
        }
        val coupon = mockk<Coupon>{
            every { id } returns 2L
        }
        val userCoupon = mockk<UserCoupon>{
            every { id } returns 3L
            every { coupon.id } returns 2L
            every { used } returns false
        }

        val totalPrice = 0.0
        val mockOrder = mockk<Order>()

        val command = OrderCommand.CreateOrderCommand(
            user = user,
            userCoupon = userCoupon,
            totalPrice = totalPrice
        )

        every { mockOrder.user } returns user
        every { mockOrder.userCoupon } returns userCoupon
        every { mockOrder.status } returns OrderStatus.PENDING
        every { mockOrder.totalPrice } returns totalPrice
        
        every { orderRepository.save(any()) } returns mockOrder
        
        // when
        val result = orderService.createOrder(command)
        
        // then
        verify { orderRepository.save(any()) }
        assertEquals(user, result.user)
        assertEquals(userCoupon, result.userCoupon)
        assertEquals(OrderStatus.PENDING, result.status)
        assertEquals(totalPrice, result.totalPrice)
    }
    
    @Test
    @DisplayName("ID로 주문을 가져온다")
    fun getOrderByIdSuccess() {
        // given
        val orderId = 100L
        val mockOrder = mockk<Order>()
        
        every { mockOrder.id } returns orderId
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
            mockk<Order>{
                every { id } returns 2L
                every { user.id } returns 4L
            },
            mockk<Order>{
                every { id } returns 3L
                every { user.id } returns 4L
            }
        )
        val user1 = mockk<User>{
            every { id } returns 4L
        }
        
        every { mockOrders[0].user.id } returns 4L
        every { mockOrders[1].user.id } returns 4L
        
        every { orderRepository.findByUserId(4L) } returns mockOrders
        
        // when
        val result = orderService.getOrdersByUserId(4L)
        
        // then
        verify { orderRepository.findByUserId(4L) }
        assertEquals(2, result.size)
    }
    
    @Test
    @DisplayName("주문 상태별로 주문 목록을 가져온다")
    fun getOrdersByStatusSuccess() {
        // given
        val status = OrderStatus.PENDING
        val mockOrders = listOf(
            mockk<Order>{
                every { id } returns 2L
                every { user.id } returns 4L
            },
            mockk<Order>{
                every { id } returns 3L
                every { user.id } returns 4L
            }
        )
        
        every { mockOrders[0].status } returns status
        every { mockOrders[1].status } returns status
        every { orderRepository.findByStatus(status) } returns mockOrders
        
        // when
        val result = orderService.getOrdersByStatus(status)
        
        // then
        verify { orderRepository.findByStatus(status) }
        assertEquals(2, result.size)
    }
    
    @Test
    @DisplayName("계정 ID와 상태로 주문 목록을 가져온다")
    fun getOrdersByAccountIdAndStatusSuccess() {
        // given
        val status = OrderStatus.PENDING
        val mockOrders = listOf(
            mockk<Order>{
                every { id } returns 2L
                every { user.id } returns 4L
            },
            mockk<Order>{
                every { id } returns 3L
                every { user.id } returns 4L
            }
        )
        val user1 = mockk<User>{
            every { id } returns 4L
        }
        
        every { mockOrders[0].user.id } returns 4L
        every { mockOrders[1].user.id } returns 4L
        every { mockOrders[0].status } returns status
        every { mockOrders[1].status } returns status
        
        every { orderRepository.findByUserIdAndStatus(4L, status) } returns mockOrders
        
        // when
        val result = orderService.getOrdersByUserIdAndStatus(4L, status)
        
        // then
        verify { orderRepository.findByUserIdAndStatus(4L, status) }
        assertEquals(2, result.size)
    }
    
    @Test
    @DisplayName("날짜 범위로 주문 목록을 가져온다")
    fun getOrdersByDateRangeSuccess() {
        // given
        val startDate = LocalDateTime.now().minusDays(7)
        val endDate = LocalDateTime.now()
        val mockOrders = listOf(
            mockk<Order>{
                every { id } returns 2L
                every { user.id } returns 4L
            },
            mockk<Order>{
                every { id } returns 3L
                every { user.id } returns 4L
            }
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
        val mockOrder = mockk<Order>()
        val updatedMockOrder = mockk<Order>()
        
        val command = OrderCommand.UpdateOrderStatusCommand(
            id = orderId,
            status = newStatus
        )
        
        every { mockOrder.id } returns orderId
        every { mockOrder.status } returns OrderStatus.PENDING
        every { mockOrder.isCancellable() } returns true
        
        every { updatedMockOrder.id } returns orderId
        every { updatedMockOrder.status } returns newStatus
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateStatus(orderId, newStatus) } returns updatedMockOrder
        
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
        val mockOrder = mockk<Order>()
        
        every { mockOrder.id } returns orderId
        every { mockOrder.status } returns OrderStatus.COMPLETED
        every { mockOrder.isCancellable() } returns false
        
        every { orderRepository.findById(orderId) } returns mockOrder
        
        val command = OrderCommand.UpdateOrderStatusCommand(
            id = orderId,
            status = OrderStatus.CANCELLED
        )
        
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
        val mockOrder = mockk<Order>()
        val cancelledMockOrder = mockk<Order>()
        
        every { mockOrder.id } returns orderId
        every { mockOrder.status } returns OrderStatus.PENDING
        every { cancelledMockOrder.id } returns orderId
        every { cancelledMockOrder.status } returns OrderStatus.CANCELLED
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateStatus(orderId, OrderStatus.CANCELLED) } returns cancelledMockOrder
        
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
        val mockOrder = mockk<Order>()
        val completedMockOrder = mockk<Order>()
        
        every { mockOrder.id } returns orderId
        every { mockOrder.status } returns OrderStatus.PENDING
        every { completedMockOrder.id } returns orderId
        every { completedMockOrder.status } returns OrderStatus.COMPLETED
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateStatus(orderId, OrderStatus.COMPLETED) } returns completedMockOrder
        
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
        val newTotalPrice = 2000.0
        val mockOrder = mockk<Order>()
        val updatedMockOrder = mockk<Order>()
        
        val command = OrderCommand.UpdateOrderTotalPriceCommand(
            id = orderId,
            totalPrice = newTotalPrice
        )
        
        every { mockOrder.id } returns orderId
        every { mockOrder.totalPrice } returns 1000.0
        every { updatedMockOrder.id } returns orderId
        every { updatedMockOrder.totalPrice } returns newTotalPrice
        
        every { orderRepository.findById(orderId) } returns mockOrder
        every { orderRepository.updateTotalPrice(orderId, newTotalPrice) } returns updatedMockOrder
        
        // when
        val result = orderService.updateOrderTotalPrice(command)
        
        // then
        verify { orderRepository.findById(orderId) }
        verify { orderRepository.updateTotalPrice(orderId, newTotalPrice) }
        assertEquals(newTotalPrice, result.totalPrice)
    }
} 