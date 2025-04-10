package kr.hhplus.be.server.order

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import kr.hhplus.be.server.domain.order.service.OrderItemService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderItemServiceUnitTest {

    @MockK
    private lateinit var orderItemRepository: OrderItemRepository

    @InjectMockKs
    private lateinit var orderItemService: OrderItemService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    @DisplayName("주문 상품을 성공적으로 생성한다")
    fun createOrderItemSuccess() {
        // given
        val orderId = 100L
        val productId = 200L
        val productOptionId = 300L
        val quantity = 2
        val productPrice = 10000.0
        val mockOrderItem = OrderItem.create(123L, orderId, productId, productOptionId, quantity, productPrice, null, null)
        
        every { orderItemRepository.save(any()) } returns mockOrderItem
        
        // when
        val result = orderItemService.create(orderId, productId, productOptionId, quantity, productPrice)
        
        // then
        verify { orderItemRepository.save(any()) }
        assertEquals(orderId, result.orderId)
        assertEquals(productId, result.productId)
        assertEquals(productOptionId, result.productOptionId)
        assertEquals(quantity, result.quantity)
        assertEquals(productPrice * quantity, result.price)
    }
    
    @Test
    @DisplayName("ID로 주문 상품을 가져온다")
    fun getOrderItemByIdSuccess() {
        // given
        val orderItemId = 123L
        val mockOrderItem = OrderItem.create(orderItemId, 100L, 200L, 300L, 2, 10000.0, null, null)
        
        every { orderItemRepository.findById(orderItemId) } returns mockOrderItem
        
        // when
        val result = orderItemService.getById(orderItemId)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        assertEquals(orderItemId, result.id)
    }
    
    @Test
    @DisplayName("ID로 주문 상품을 가져올 때 없으면 예외가 발생한다")
    fun getOrderItemByIdNotFound() {
        // given
        val orderItemId = 123L
        
        every { orderItemRepository.findById(orderItemId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderItemService.getById(orderItemId)
        }
        
        verify { orderItemRepository.findById(orderItemId) }
        assertTrue(exception.message!!.contains("주문 상품을 찾을 수 없습니다"))
    }
    
    @Test
    @DisplayName("주문 ID로 주문 상품 목록을 가져온다")
    fun getOrderItemsByOrderIdSuccess() {
        // given
        val orderId = 100L
        val mockOrderItems = listOf(
            OrderItem.create(123L, orderId, 200L, 300L, 2, 10000.0, null, null),
            OrderItem.create(124L, orderId, 201L, 301L, 1, 5000.0, null, null)
        )
        
        every { orderItemRepository.findByOrderId(orderId) } returns mockOrderItems
        
        // when
        val result = orderItemService.getByOrderId(orderId)
        
        // then
        verify { orderItemRepository.findByOrderId(orderId) }
        assertEquals(2, result.size)
        assertEquals(orderId, result[0].orderId)
        assertEquals(orderId, result[1].orderId)
    }
    
    @Test
    @DisplayName("상품 ID로 주문 상품 목록을 가져온다")
    fun getOrderItemsByProductIdSuccess() {
        // given
        val productId = 200L
        val mockOrderItems = listOf(
            OrderItem.create(123L, 100L, productId, 300L, 2, 10000.0, null, null),
            OrderItem.create(124L, 101L, productId, 301L, 1, 5000.0, null, null)
        )
        
        every { orderItemRepository.findByProductId(productId) } returns mockOrderItems
        
        // when
        val result = orderItemService.getByProductId(productId)
        
        // then
        verify { orderItemRepository.findByProductId(productId) }
        assertEquals(2, result.size)
        assertEquals(productId, result[0].productId)
        assertEquals(productId, result[1].productId)
    }
    
    @Test
    @DisplayName("주문 ID와 상품 옵션 ID로 주문 상품을 가져온다")
    fun getOrderItemByOrderIdAndProductOptionIdSuccess() {
        // given
        val orderId = 100L
        val productOptionId = 300L
        val mockOrderItem = OrderItem.create(123L, orderId, 200L, productOptionId, 2, 10000.0, null, null)
        
        every { orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId) } returns mockOrderItem
        
        // when
        val result = orderItemService.getByOrderIdAndProductOptionId(orderId, productOptionId)
        
        // then
        verify { orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId) }
        assertNotNull(result)
        assertEquals(orderId, result!!.orderId)
        assertEquals(productOptionId, result.productOptionId)
    }
    
    @Test
    @DisplayName("주문 상품 수량을 성공적으로 업데이트한다")
    fun updateOrderItemQuantitySuccess() {
        // given
        val orderItemId = 123L
        val newQuantity = 3
        val mockOrderItem = OrderItem.create(orderItemId, 100L, 200L, 300L, 2, 10000.0, null, null)
        val updatedMockOrderItem = mockOrderItem.update(newQuantity, null)
        
        every { orderItemRepository.findById(orderItemId) } returns mockOrderItem
        every { orderItemRepository.update(any()) } returns updatedMockOrderItem
        
        // when
        val result = orderItemService.update(orderItemId, newQuantity, null)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { orderItemRepository.update(any()) }
        assertEquals(newQuantity, result.quantity)
        assertEquals(10000.0 * newQuantity, result.price)
    }
    
    @Test
    @DisplayName("주문 상품 가격을 성공적으로 업데이트한다")
    fun updateOrderItemPriceSuccess() {
        // given
        val orderItemId = 123L
        val newPrice = 15000.0
        val mockOrderItem = OrderItem.create(orderItemId, 100L, 200L, 300L, 2, 10000.0, null, null)
        val updatedMockOrderItem = mockOrderItem.update(null, newPrice)
        
        every { orderItemRepository.findById(orderItemId) } returns mockOrderItem
        every { orderItemRepository.update(any()) } returns updatedMockOrderItem
        
        // when
        val result = orderItemService.update(orderItemId, null, newPrice)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { orderItemRepository.update(any()) }
        assertEquals(2, result.quantity) // 수량은 변경 없음
        assertEquals(newPrice * 2, result.price)
    }
    
    @Test
    @DisplayName("할인율과 함께 주문 상품 가격을 업데이트한다")
    fun updateOrderItemPriceWithDiscountSuccess() {
        // given
        val orderItemId = 123L
        val discountRate = 20.0 // 20% 할인
        val mockOrderItem = OrderItem.create(orderItemId, 100L, 200L, 300L, 2, 10000.0, null, null)
        val updatedMockOrderItem = mockOrderItem.updatePrice(discountRate)
        
        every { orderItemRepository.findById(orderItemId) } returns mockOrderItem
        every { orderItemRepository.update(any()) } returns updatedMockOrderItem
        
        // when
        val result = orderItemService.updatePrice(orderItemId, discountRate)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { orderItemRepository.update(any()) }
        val expectedPrice = 20000.0 * (1 - discountRate / 100)
        assertEquals(expectedPrice, result.price)
    }
    
    @Test
    @DisplayName("주문 상품을 ID로 삭제한다")
    fun deleteOrderItemByIdSuccess() {
        // given
        val orderItemId = 123L
        
        every { orderItemRepository.delete(orderItemId) } just runs
        
        // when
        orderItemService.deleteById(orderItemId)
        
        // then
        verify { orderItemRepository.delete(orderItemId) }
    }
    
    @Test
    @DisplayName("주문 ID로 모든 주문 상품을 삭제한다")
    fun deleteAllOrderItemsByOrderIdSuccess() {
        // given
        val orderId = 100L
        
        every { orderItemRepository.deleteByOrderId(orderId) } just runs
        
        // when
        orderItemService.deleteAllByOrderId(orderId)
        
        // then
        verify { orderItemRepository.deleteByOrderId(orderId) }
    }
    
    @Test
    @DisplayName("주문 상품 목록의 총 가격을 계산한다")
    fun calculateTotalPriceSuccess() {
        // given
        val orderItems = listOf(
            OrderItem.create(123L, 100L, 200L, 300L, 2, 10000.0, null, null), // 가격 = 20000.0
            OrderItem.create(124L, 100L, 201L, 301L, 1, 5000.0, null, null)   // 가격 = 5000.0
        )
        
        // when
        val result = orderItemService.calculateTotalPrice(orderItems)
        
        // then
        assertEquals(25000.0, result) // 20000.0 + 5000.0 = 25000.0
    }
} 