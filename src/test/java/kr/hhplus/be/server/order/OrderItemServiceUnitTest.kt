package kr.hhplus.be.server.order

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import kr.hhplus.be.server.domain.order.service.OrderItemCommand
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.user.model.Account
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
        val order = mockk<Order> {
            every { id } returns 1L
        }
        val product = mockk<Product> {
            every { id } returns 2L
            every { price } returns 10000.0
        }
        val productOption = mockk<ProductOption> {
            every { id } returns 3L
            every { additionalPrice } returns 0.0
        }
        val quantity = 2
        
        val command = OrderItemCommand.CreateOrderItemCommand(
            order = order,
            product = product,
            productOption = productOption,
            quantity = quantity,
            accountCouponId = null,
            discountRate = null
        )
        
        val mockOrderItem = mockk<OrderItem>()
        
        every { mockOrderItem.id } returns 4L
        every { mockOrderItem.order } returns order
        every { mockOrderItem.product } returns product
        every { mockOrderItem.productOption } returns productOption
        every { mockOrderItem.quantity } returns quantity
        every { mockOrderItem.price } returns 20000.0
        
        every { orderItemRepository.save(any()) } returns mockOrderItem
        
        // when
        val result = orderItemService.create(command)
        
        // then
        verify { orderItemRepository.save(any()) }
        assertEquals(4L, result.id)
        assertEquals(order, result.order)
        assertEquals(product, result.product)
        assertEquals(productOption, result.productOption)
        assertEquals(quantity, result.quantity)
        assertEquals(20000.0, result.price)
    }
    
    @Test
    @DisplayName("ID로 주문 상품을 가져온다")
    fun getOrderItemByIdSuccess() {
        // given
        val orderItemId = 123L
        val mockOrderItem = mockk<OrderItem>()
        
        every { mockOrderItem.id } returns orderItemId
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
        val order = mockk<Order>()
        val mockOrderItems = listOf(
            mockk<OrderItem>(),
            mockk<OrderItem>()
        )
        
        every { order.id } returns orderId
        every { mockOrderItems[0].order } returns order
        every { mockOrderItems[1].order } returns order
        every { orderItemRepository.findByOrderId(orderId) } returns mockOrderItems
        
        // when
        val result = orderItemService.getByOrderId(orderId)
        
        // then
        verify { orderItemRepository.findByOrderId(orderId) }
        assertEquals(2, result.size)
    }
    
    @Test
    @DisplayName("상품 ID로 주문 상품 목록을 가져온다")
    fun getOrderItemsByProductIdSuccess() {
        // given
        val productId = 200L
        val product = mockk<Product>()
        val mockOrderItems = listOf(
            mockk<OrderItem>(),
            mockk<OrderItem>()
        )
        
        every { product.id } returns productId
        every { mockOrderItems[0].product } returns product
        every { mockOrderItems[1].product } returns product
        every { orderItemRepository.findByProductId(productId) } returns mockOrderItems
        
        // when
        val result = orderItemService.getByProductId(productId)
        
        // then
        verify { orderItemRepository.findByProductId(productId) }
        assertEquals(2, result.size)
    }
    
    @Test
    @DisplayName("주문 ID와 상품 옵션 ID로 주문 상품을 가져온다")
    fun getOrderItemByOrderIdAndProductOptionIdSuccess() {
        // given
        val orderId = 100L
        val order = mockk<Order>()
        val productOptionId = 300L
        val productOption = mockk<ProductOption>()
        val mockOrderItem = mockk<OrderItem>()
        
        every { order.id } returns orderId
        every { productOption.id } returns productOptionId
        every { mockOrderItem.order } returns order
        every { mockOrderItem.productOption } returns productOption
        every { orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId) } returns mockOrderItem
        
        // when
        val result = orderItemService.getByOrderIdAndProductOptionId(orderId, productOptionId)
        
        // then
        verify { orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId) }
        assertNotNull(result)
    }
    
    @Test
    @DisplayName("주문 상품 수량을 성공적으로 업데이트한다")
    fun updateOrderItemQuantitySuccess() {
        // given
        val orderItemId = 123L
        val newQuantity = 3
        val productPrice = 10000.0
        val mockOrderItem = mockk<OrderItem>()
        val updatedMockOrderItem = mockk<OrderItem>()
        
        val command = OrderItemCommand.UpdateOrderItemCommand(
            id = orderItemId,
            quantity = newQuantity,
            productPrice = productPrice
        )
        
        every { mockOrderItem.quantity } returns 2
        every { updatedMockOrderItem.quantity } returns newQuantity
        every { updatedMockOrderItem.price } returns productPrice * newQuantity
        
        every { orderItemRepository.findById(orderItemId) } returns mockOrderItem
        every { mockOrderItem.update(newQuantity, productPrice) } returns updatedMockOrderItem
        every { orderItemRepository.update(updatedMockOrderItem) } returns updatedMockOrderItem
        
        // when
        val result = orderItemService.update(command)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { mockOrderItem.update(newQuantity, productPrice) }
        verify { orderItemRepository.update(updatedMockOrderItem) }
        assertEquals(newQuantity, result.quantity)
        assertEquals(productPrice * newQuantity, result.price)
    }
    
    @Test
    @DisplayName("할인율과 함께 주문 상품 가격을 업데이트한다")
    fun updateOrderItemPriceWithDiscountSuccess() {
        // given
        val orderItemId = 123L
        val discountRate = 20.0 // 20% 할인
        val originalPrice = 20000.0
        val mockOrderItem = mockk<OrderItem>()
        val updatedMockOrderItem = mockk<OrderItem>()
        
        val command = OrderItemCommand.UpdateOrderItemPriceCommand(
            id = orderItemId,
            price = discountRate
        )
        
        every { mockOrderItem.price } returns originalPrice
        every { updatedMockOrderItem.price } returns originalPrice * (1 - discountRate / 100)
        
        every { orderItemRepository.findById(orderItemId) } returns mockOrderItem
        every { mockOrderItem.updatePrice(discountRate) } returns updatedMockOrderItem
        every { orderItemRepository.update(updatedMockOrderItem) } returns updatedMockOrderItem
        
        // when
        val result = orderItemService.updatePrice(command)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { mockOrderItem.updatePrice(discountRate) }
        verify { orderItemRepository.update(updatedMockOrderItem) }
        val expectedPrice = originalPrice * (1 - discountRate / 100)
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
        val mockOrderItem1 = mockk<OrderItem>()
        val mockOrderItem2 = mockk<OrderItem>()
        val orderItems = listOf(mockOrderItem1, mockOrderItem2)
        
        every { mockOrderItem1.price } returns 20000.0
        every { mockOrderItem2.price } returns 5000.0
        
        // when
        val result = orderItemService.calculateTotalPrice(orderItems)
        
        // then
        assertEquals(25000.0, result) // 20000.0 + 5000.0 = 25000.0
    }
} 