package kr.hhplus.be.server.order

import kr.hhplus.be.server.domain.order.model.OrderItem
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class OrderItemUnitTest {
    
    companion object {
        private const val ORDER_ID = 1L
        private const val PRODUCT_ID = 2L
        private const val PRODUCT_OPTION_ID = 3L
        private const val USER_COUPON_ID = 5L
        private const val PRODUCT_PRICE = 10000.0
        private const val BASE_QUANTITY = 2
    }
    
    @Test
    @DisplayName("주문 상품을 성공적으로 생성한다")
    fun createOrderItemSuccess() {
        // given
        val quantity = BASE_QUANTITY
        
        // when
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = quantity,
            price = PRODUCT_PRICE
        )
        
        // then
        assertEquals(ORDER_ID, orderItem.orderId)
        assertEquals(PRODUCT_ID, orderItem.productId)
        assertEquals(PRODUCT_OPTION_ID, orderItem.productOptionId)
        assertEquals(quantity, orderItem.quantity)
        assertEquals(PRODUCT_PRICE, orderItem.price)
        assertNull(orderItem.userCouponId)
        assertNotNull(orderItem.createdAt)
        assertNotNull(orderItem.updatedAt)
    }
    
    @Test
    @DisplayName("할인율이 적용된 주문 상품을 생성한다")
    fun createOrderItemWithDiscount() {
        // given
        val quantity = BASE_QUANTITY
        val discountRate = 10.0 // 10% 할인
        val basePrice = PRODUCT_PRICE
        val expectedPrice = basePrice * (1 - discountRate / 100)
        
        // when
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = USER_COUPON_ID,
            quantity = quantity,
            price = expectedPrice
        )
        
        // then
        assertEquals(ORDER_ID, orderItem.orderId)
        assertEquals(PRODUCT_ID, orderItem.productId)
        assertEquals(PRODUCT_OPTION_ID, orderItem.productOptionId)
        assertEquals(quantity, orderItem.quantity)
        assertEquals(expectedPrice, orderItem.price)
        assertEquals(USER_COUPON_ID, orderItem.userCouponId)
    }
    
    @Test
    @DisplayName("주문 상품 수량이 최소 수량보다 작으면 예외가 발생한다")
    fun createOrderItemWithQuantityLessThanMinFails() {
        // given
        val invalidQuantity = OrderItem.MIN_QUANTITY - 1 // 최소 수량 미만
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            OrderItem.create(
                orderId = ORDER_ID,
                productId = PRODUCT_ID,
                productOptionId = PRODUCT_OPTION_ID,
                userCouponId = null,
                quantity = invalidQuantity,
                price = PRODUCT_PRICE
            )
        }
        
        assertTrue(exception.message!!.contains("수량은"))
    }
    
    @Test
    @DisplayName("주문 상품 수량이 최대 수량보다 크면 예외가 발생한다")
    fun createOrderItemWithQuantityMoreThanMaxFails() {
        // given
        val invalidQuantity = OrderItem.MAX_QUANTITY + 1 // 최대 수량 초과
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            OrderItem.create(
                orderId = ORDER_ID,
                productId = PRODUCT_ID,
                productOptionId = PRODUCT_OPTION_ID,
                userCouponId = null,
                quantity = invalidQuantity,
                price = PRODUCT_PRICE
            )
        }
        
        assertTrue(exception.message!!.contains("수량은"))
    }
    
    @Test
    @DisplayName("주문 상품 가격이 최소 가격보다 작으면 예외가 발생한다")
    fun createOrderItemWithPriceLessThanMinFails() {
        // given
        val lowPrice = OrderItem.MIN_PRICE - 10 // 최소 가격보다 낮음
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            OrderItem.create(
                orderId = ORDER_ID,
                productId = PRODUCT_ID,
                productOptionId = PRODUCT_OPTION_ID,
                userCouponId = null,
                quantity = 1,
                price = lowPrice
            )
        }
        
        assertTrue(exception.message!!.contains("가격은"))
    }
    
    @Test
    @DisplayName("주문 상품 수량을 성공적으로 업데이트한다")
    fun updateOrderItemQuantitySuccess() {
        // given
        val initialQuantity = BASE_QUANTITY
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = initialQuantity,
            price = PRODUCT_PRICE
        )
        val initialPrice = orderItem.price
        val newQuantity = 3
        
        // when
        val updatedOrderItem = orderItem.update(newQuantity, null)
        
        // then
        assertEquals(newQuantity, updatedOrderItem.quantity)
        // 수량만 변경할 경우, update 메서드는 가격을 자동으로 계산하지 않음
        assertEquals(initialPrice, updatedOrderItem.price) // 가격은 변경되지 않음
    }
    
    @Test
    @DisplayName("주문 상품 가격을 성공적으로 업데이트한다")
    fun updateOrderItemPriceSuccess() {
        // given
        val quantity = BASE_QUANTITY
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = quantity,
            price = PRODUCT_PRICE
        )
        val newProductPrice = 15000.0
        
        // when
        val updatedOrderItem = orderItem.update(null, newProductPrice)
        
        // then
        assertEquals(quantity, updatedOrderItem.quantity) // 수량은 변경 없음
        assertEquals(newProductPrice * quantity, updatedOrderItem.price)
        assertTrue(updatedOrderItem.updatedAt.isAfter(orderItem.createdAt))
    }
    
    @Test
    @DisplayName("주문 상품 가격을 할인율과 함께 업데이트한다")
    fun updateOrderItemPriceWithDiscountSuccess() {
        // given
        val quantity = BASE_QUANTITY
        val initialPrice = PRODUCT_PRICE * quantity
        
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = quantity,
            price = initialPrice
        )
        val discountRate = 20.0 // 20% 할인
        
        // when
        val updatedOrderItem = orderItem.updatePrice(discountRate)
        
        // then
        val expectedPrice = initialPrice * (1 - discountRate / 100)
        assertEquals(expectedPrice, updatedOrderItem.price)
        assertTrue(updatedOrderItem.updatedAt.isAfter(orderItem.createdAt))
    }
    
    @Test
    @DisplayName("너무 큰 할인율로 가격이 최소 가격 미만이 되면 예외가 발생한다")
    fun updateOrderItemPriceWithTooHighDiscountRateFails() {
        // given
        val lowInitialPrice = 150.0  // 낮은 가격
        val orderItem = OrderItem.create(
            orderId = ORDER_ID,
            productId = PRODUCT_ID,
            productOptionId = PRODUCT_OPTION_ID,
            userCouponId = null,
            quantity = 1,
            price = lowInitialPrice
        )
        val tooHighDiscountRate = 50.0 // 50% 할인하여 최소 금액 이하로 만듦
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            orderItem.updatePrice(tooHighDiscountRate)
        }
        
        assertTrue(exception.message!!.contains("가격은"))
    }
}