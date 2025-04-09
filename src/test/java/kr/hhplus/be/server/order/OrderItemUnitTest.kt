package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.order.model.OrderItem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class OrderItemUnitTest {
    
    @Test
    @DisplayName("주문 상품을 성공적으로 생성한다")
    fun createOrderItemSuccess() {
        // given
        val id = 1L
        val orderId = 100L
        val productId = 200L
        val productOptionId = 300L
        val quantity = 2
        val productPrice = 10000.0
        
        // when
        val orderItem = OrderItem.create(
            id, orderId, productId, productOptionId, quantity, productPrice, null, null
        )
        
        // then
        assertEquals(id, orderItem.id)
        assertEquals(orderId, orderItem.orderId)
        assertEquals(productId, orderItem.productId)
        assertEquals(productOptionId, orderItem.productOptionId)
        assertEquals(quantity, orderItem.quantity)
        assertEquals(productPrice * quantity, orderItem.price)
        assertNull(orderItem.accountCouponId)
    }
    
    @Test
    @DisplayName("할인율이 적용된 주문 상품을 생성한다")
    fun createOrderItemWithDiscount() {
        // given
        val id = 1L
        val orderId = 100L
        val productId = 200L
        val productOptionId = 300L
        val quantity = 2
        val productPrice = 10000.0
        val discountRate = 10.0 // 10% 할인
        
        // when
        val orderItem = OrderItem.create(
            id, orderId, productId, productOptionId, quantity, productPrice, null, discountRate
        )
        
        // then
        assertEquals(id, orderItem.id)
        assertEquals(orderId, orderItem.orderId)
        assertEquals(productId, orderItem.productId)
        assertEquals(productOptionId, orderItem.productOptionId)
        assertEquals(quantity, orderItem.quantity)
        val expectedPrice = productPrice * quantity * (1 - discountRate / 100)
        assertEquals(expectedPrice, orderItem.price)
    }
    
    @Test
    @DisplayName("주문 상품 수량이 최소 수량보다 작으면 예외가 발생한다")
    fun createOrderItemWithQuantityLessThanMinFails() {
        // given
        val id = 1L
        val orderId = 100L
        val productId = 200L
        val productOptionId = 300L
        val invalidQuantity = 0 // 최소 수량 미만
        val productPrice = 10000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            OrderItem.create(id, orderId, productId, productOptionId, invalidQuantity, productPrice, null, null)
        }
        
        assertTrue(exception.message!!.contains("수량은"))
    }
    
    @Test
    @DisplayName("주문 상품 수량이 최대 수량보다 크면 예외가 발생한다")
    fun createOrderItemWithQuantityMoreThanMaxFails() {
        // given
        val id = 1L
        val orderId = 100L
        val productId = 200L
        val productOptionId = 300L
        val invalidQuantity = OrderItem.MAX_QUANTITY + 1 // 최대 수량 초과
        val productPrice = 10000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            OrderItem.create(id, orderId, productId, productOptionId, invalidQuantity, productPrice, null, null)
        }
        
        assertTrue(exception.message!!.contains("수량은"))
    }
    
    @Test
    @DisplayName("주문 상품 가격이 최소 가격보다 작으면 예외가 발생한다")
    fun createOrderItemWithPriceLessThanMinFails() {
        // given
        val id = 1L
        val orderId = 100L
        val productId = 200L
        val productOptionId = 300L
        val quantity = 1
        val invalidPrice = 50.0 // 최소 가격보다 작음
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            OrderItem.create(id, orderId, productId, productOptionId, quantity, invalidPrice, null, null)
        }
        
        assertTrue(exception.message!!.contains("가격은"))
    }
    
    @Test
    @DisplayName("주문 상품 수량을 성공적으로 업데이트한다")
    fun updateOrderItemQuantitySuccess() {
        // given
        val orderItem = OrderItem.create(1L, 100L, 200L, 300L, 2, 10000.0, null, null)
        val newQuantity = 3
        
        // when
        val updatedOrderItem = orderItem.update(newQuantity, null)
        
        // then
        assertEquals(newQuantity, updatedOrderItem.quantity)
        assertEquals(10000.0 * newQuantity, updatedOrderItem.price)
    }
    
    @Test
    @DisplayName("주문 상품 가격을 성공적으로 업데이트한다")
    fun updateOrderItemPriceSuccess() {
        // given
        val orderItem = OrderItem.create(1L, 100L, 200L, 300L, 2, 10000.0, null, null)
        val newPrice = 15000.0
        
        // when
        val updatedOrderItem = orderItem.update(null, newPrice)
        
        // then
        assertEquals(2, updatedOrderItem.quantity) // 수량은 변경 없음
        assertEquals(newPrice * 2, updatedOrderItem.price)
    }
    
    @Test
    @DisplayName("주문 상품 가격을 할인율과 함께 업데이트한다")
    fun updateOrderItemPriceWithDiscountSuccess() {
        // given
        val orderItem = OrderItem.create(1L, 100L, 200L, 300L, 2, 10000.0, null, null)
        val discountRate = 20.0 // 20% 할인
        
        // when
        val updatedOrderItem = orderItem.updatePrice(discountRate)
        
        // then
        val expectedPrice = 20000.0 * (1 - discountRate / 100)
        assertEquals(expectedPrice, updatedOrderItem.price)
    }
}