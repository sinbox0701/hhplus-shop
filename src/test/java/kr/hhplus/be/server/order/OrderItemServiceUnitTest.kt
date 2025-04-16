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
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.model.Coupon
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OrderItemServiceUnitTest {

    private lateinit var orderItemRepository: OrderItemRepository
    private lateinit var orderItemService: OrderItemService

    companion object {
        private const val ORDER_ID = 1L
        private const val PRODUCT_ID = 2L
        private const val PRODUCT_OPTION_ID = 3L
        private const val USER_COUPON_ID = 5L
        private const val PRODUCT_PRICE = 10000.0
        private const val BASE_QUANTITY = 2
    }

    @BeforeEach
    fun setup() {
        orderItemRepository = mockk()
        orderItemService = OrderItemService(orderItemRepository)
    }

    @Test
    @DisplayName("주문 상품을 성공적으로 생성한다")
    fun createOrderItemSuccess() {
        // given
        val quantity = BASE_QUANTITY
        val discountRate = 10.0 // 10% 할인
        
        val command = OrderItemCommand.CreateOrderItemCommand(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            quantity = quantity,
            userCouponId = USER_COUPON_ID,
            discountRate = discountRate
        )
        
        // 저장될 OrderItem 객체 생성
        val createdOrderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = USER_COUPON_ID,
            quantity = quantity,
            price = PRODUCT_PRICE * (1 - discountRate / 100)
        )
        
        every { orderItemRepository.save(any()) } returns createdOrderItem
        
        // when
        val result = orderItemService.create(command)
        
        // then
        verify { orderItemRepository.save(any()) }
        assertEquals(ORDER_ID, result.orderId)
        assertEquals(PRODUCT_ID, result.productId)
        assertEquals(PRODUCT_OPTION_ID, result.productOptionId)
        assertEquals(quantity, result.quantity)
        assertEquals(PRODUCT_PRICE * (1 - discountRate / 100), result.price)
    }
    
    @Test
    @DisplayName("ID로 주문 상품을 가져온다")
    fun getOrderItemByIdSuccess() {
        // given
        val orderItemId = 123L
        
        // OrderItem 객체 생성
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = BASE_QUANTITY,
            price = PRODUCT_PRICE
        )
        
        every { orderItemRepository.findById(orderItemId) } returns orderItem
        
        // when
        val result = orderItemService.getById(orderItemId)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        assertEquals(ORDER_ID, result.orderId)
        assertEquals(PRODUCT_ID, result.productId)
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
        val orderId = ORDER_ID
        
        // OrderItem 리스트 생성
        val orderItems = listOf(
            OrderItem.create(
                orderId = orderId,
                productId = PRODUCT_ID,
                productOptionId = PRODUCT_OPTION_ID,
                userCouponId = null,
                quantity = BASE_QUANTITY,
                price = PRODUCT_PRICE
            ),
            OrderItem.create(
                orderId = orderId,
                productId = PRODUCT_ID + 1,
                productOptionId = PRODUCT_OPTION_ID + 1,
                userCouponId = null,
                quantity = BASE_QUANTITY + 1,
                price = PRODUCT_PRICE * 2
            )
        )
        
        every { orderItemRepository.findByOrderId(orderId) } returns orderItems
        
        // when
        val result = orderItemService.getByOrderId(orderId)
        
        // then
        verify { orderItemRepository.findByOrderId(orderId) }
        assertEquals(2, result.size)
        result.forEach { assertEquals(orderId, it.orderId) }
    }
    
    @Test
    @DisplayName("상품 ID로 주문 상품 목록을 가져온다")
    fun getOrderItemsByProductIdSuccess() {
        // given
        val productId = PRODUCT_ID
        
        // OrderItem 리스트 생성
        val orderItems = listOf(
            OrderItem.create(
                orderId = ORDER_ID,
                productId = productId,
                productOptionId = PRODUCT_OPTION_ID,
                userCouponId = null,
                quantity = BASE_QUANTITY,
                price = PRODUCT_PRICE
            ),
            OrderItem.create(
                orderId = ORDER_ID + 1,
                productId = productId,
                productOptionId = PRODUCT_OPTION_ID + 1,
                userCouponId = null,
                quantity = BASE_QUANTITY + 1,
                price = PRODUCT_PRICE * 2
            )
        )
        
        every { orderItemRepository.findByProductId(productId) } returns orderItems
        
        // when
        val result = orderItemService.getByProductId(productId)
        
        // then
        verify { orderItemRepository.findByProductId(productId) }
        assertEquals(2, result.size)
        result.forEach { assertEquals(productId, it.productId) }
    }
    
    @Test
    @DisplayName("주문 ID와 상품 옵션 ID로 주문 상품을 가져온다")
    fun getOrderItemByOrderIdAndProductOptionIdSuccess() {
        // given
        val orderId = ORDER_ID
        val productOptionId = PRODUCT_OPTION_ID
        
        // OrderItem 객체 생성
        val orderItem = OrderItem.create(
            orderId = orderId,
            productId = PRODUCT_ID,
            productOptionId = productOptionId,
            userCouponId = null,
            quantity = BASE_QUANTITY,
            price = PRODUCT_PRICE
        )
        
        every { orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId) } returns orderItem
        
        // when
        val result = orderItemService.getByOrderIdAndProductOptionId(orderId, productOptionId)
        
        // then
        verify { orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId) }
        assertNotNull(result)
        assertEquals(orderId, result?.orderId)
        assertEquals(productOptionId, result?.productOptionId)
    }
    
    @Test
    @DisplayName("주문 상품 수량을 성공적으로 업데이트한다")
    fun updateOrderItemQuantitySuccess() {
        // given
        val orderItemId = 123L
        val newQuantity = 3
        val productPrice = 15000.0
        
        // 원본 OrderItem 객체 생성
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = BASE_QUANTITY,
            price = PRODUCT_PRICE
        )
        
        // 업데이트된 OrderItem 객체 생성
        val updatedOrderItem = orderItem.update(newQuantity, productPrice)
        
        val command = OrderItemCommand.UpdateOrderItemCommand(
            id = orderItemId,
            quantity = newQuantity,
            productPrice = productPrice
        )
        
        every { orderItemRepository.findById(orderItemId) } returns orderItem
        every { orderItemRepository.update(any()) } returns updatedOrderItem
        
        // when
        val result = orderItemService.update(command)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { orderItemRepository.update(any()) }
        assertEquals(newQuantity, result.quantity)
        assertEquals(productPrice * newQuantity, result.price)
    }
    
    @Test
    @DisplayName("할인율과 함께 주문 상품 가격을 업데이트한다")
    fun updateOrderItemPriceWithDiscountSuccess() {
        // given
        val orderItemId = 123L
        val discountRate = 20.0 // 20% 할인
        
        // 원본 OrderItem 객체 생성
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = BASE_QUANTITY,
            price = PRODUCT_PRICE * BASE_QUANTITY
        )
        
        // 업데이트된 OrderItem 객체 생성
        val updatedOrderItem = orderItem.updatePrice(discountRate)
        
        val command = OrderItemCommand.UpdateOrderItemPriceCommand(
            id = orderItemId,
            discountRate = discountRate
        )
        
        every { orderItemRepository.findById(orderItemId) } returns orderItem
        every { orderItemRepository.update(any()) } returns updatedOrderItem
        
        // when
        val result = orderItemService.updatePrice(command)
        
        // then
        verify { orderItemRepository.findById(orderItemId) }
        verify { orderItemRepository.update(any()) }
        val expectedPrice = PRODUCT_PRICE * BASE_QUANTITY * (1 - discountRate / 100)
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
        val orderId = ORDER_ID
        
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
            OrderItem.create(
                orderId = ORDER_ID,
                productId = PRODUCT_ID,
                productOptionId = PRODUCT_OPTION_ID,
                userCouponId = null,
                quantity = BASE_QUANTITY,
                price = 20000.0
            ),
            OrderItem.create(
                orderId = ORDER_ID,
                productId = PRODUCT_ID + 1,
                productOptionId = PRODUCT_OPTION_ID + 1,
                userCouponId = null,
                quantity = BASE_QUANTITY + 1,
                price = 5000.0
            )
        )
        
        // when
        val result = orderItemService.calculateTotalPrice(orderItems)
        
        // then
        assertEquals(25000.0, result) // 20000.0 + 5000.0 = 25000.0
    }
} 