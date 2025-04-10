package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.model.Product
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import io.mockk.mockk

class ProductOptionUnitTest {
    
    @Test
    @DisplayName("유효한 데이터로 ProductOption 객체 생성 성공")
    fun createProductOptionWithValidData() {
        // given
        val product = mockk<Product>()
        val name = "옵션"
        val availableQuantity = 100
        val additionalPrice = 1000.0
        
        // when
        val productOption = ProductOption.create(product, name, availableQuantity, additionalPrice)
        
        // then
        assertEquals(product, productOption.product)
        assertEquals(name, productOption.name)
        assertEquals(availableQuantity, productOption.availableQuantity)
        assertEquals(additionalPrice, productOption.additionalPrice)
        assertNotNull(productOption.createdAt)
        assertNotNull(productOption.updatedAt)
    }
    
    @Test
    @DisplayName("이름이 최소 길이보다 짧을 경우 예외 발생")
    fun createProductOptionWithTooShortName() {
        // given
        val product = mockk<Product>()
        val name = "" // 최소 길이는 1
        val availableQuantity = 100
        val additionalPrice = 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(product, name, availableQuantity, additionalPrice)
        }
        
        assertTrue(exception.message!!.contains("Name must be between"))
    }
    
    @Test
    @DisplayName("이름이 최대 길이보다 길 경우 예외 발생")
    fun createProductOptionWithTooLongName() {
        // given
        val product = mockk<Product>()
        val productId = 10L
        val name = "아주아주아주길다" // 최대 길이는 10
        val availableQuantity = 100
        val additionalPrice = 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(product, name, availableQuantity, additionalPrice)
        }
        
        assertTrue(exception.message!!.contains("Name must be between"))
    }
    
    @Test
    @DisplayName("수량이 최소값보다 작을 경우 예외 발생")
    fun createProductOptionWithTooSmallQuantity() {
        // given
        val product = mockk<Product>()
        val name = "옵션"
        val availableQuantity = -1 // 최소값은 0
        val additionalPrice = 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(product, name, availableQuantity, additionalPrice)
        }
        
        assertTrue(exception.message!!.contains("Available quantity must be between"))
    }
    
    @Test
    @DisplayName("수량이 최대값보다 클 경우 예외 발생")
    fun createProductOptionWithTooLargeQuantity() {
        // given
        val product = mockk<Product>()
        val name = "옵션"
        val availableQuantity = 1001 // 최대값은 1000
        val additionalPrice = 1000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(product, name, availableQuantity, additionalPrice)
        }
        
        assertTrue(exception.message!!.contains("Available quantity must be between"))
    }
    
    @Test
    @DisplayName("유효한 데이터로 상품 옵션 정보 업데이트 성공")
    fun updateProductOptionWithValidData() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val newName = "새옵션"
        val newAdditionalPrice = 2000.0
        
        // when
        val updatedOption = productOption.update(newName, newAdditionalPrice)
        
        // then
        assertEquals(newName, updatedOption.name)
        assertEquals(newAdditionalPrice, updatedOption.additionalPrice)
        assertNotEquals(updatedOption.createdAt, updatedOption.updatedAt)
    }
    
    @Test
    @DisplayName("이름만 업데이트 성공")
    fun updateOnlyName() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val originalAdditionalPrice = productOption.additionalPrice
        val newName = "새옵션"
        
        // when
        val updatedOption = productOption.update(newName, null)
        
        // then
        assertEquals(newName, updatedOption.name)
        assertEquals(originalAdditionalPrice, updatedOption.additionalPrice)
    }
    
    @Test
    @DisplayName("추가 가격만 업데이트 성공")
    fun updateOnlyAdditionalPrice() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val originalName = productOption.name
        val newAdditionalPrice = 2000.0
        
        // when
        val updatedOption = productOption.update(null, newAdditionalPrice)
        
        // then
        assertEquals(originalName, updatedOption.name)
        assertEquals(newAdditionalPrice, updatedOption.additionalPrice)
    }
    
    @Test
    @DisplayName("이름이 너무 짧을 경우 업데이트 시 예외 발생")
    fun updateWithTooShortName() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val invalidName = "" // 최소 1자 필요
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOption.update(invalidName, null)
        }
        
        assertTrue(exception.message!!.contains("Name must be between"))
    }
    
    @Test
    @DisplayName("이름이 너무 길 경우 업데이트 시 예외 발생")
    fun updateWithTooLongName() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val invalidName = "아주아주아주길다" // 최대 10자 필요
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOption.update(invalidName, null)
        }
        
        assertTrue(exception.message!!.contains("Name must be between"))
    }
    
    @Test
    @DisplayName("수량 추가 성공")
    fun addQuantitySuccess() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val quantityToAdd = 50
        val expectedQuantity = 150
        
        // when
        val updatedOption = productOption.add(quantityToAdd)
        
        // then
        assertEquals(expectedQuantity, updatedOption.availableQuantity)
        assertNotEquals(updatedOption.createdAt, updatedOption.updatedAt)
    }
    
    @Test
    @DisplayName("수량 추가 시 음수값 입력하면 예외 발생")
    fun addNegativeQuantity() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val invalidQuantity = -10
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOption.add(invalidQuantity)
        }
        
        assertTrue(exception.message!!.contains("Available quantity must be between"))
    }
    
    @Test
    @DisplayName("수량 추가 시 최댓값 초과할 경우 예외 발생")
    fun addTooLargeQuantity() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val invalidQuantity = 901 // 현재 100 + 901 = 1001 > 최대값 1000
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOption.add(invalidQuantity)
        }
        
        assertTrue(exception.message!!.contains("Available quantity must be between"))
    }
    
    @Test
    @DisplayName("수량 차감 성공")
    fun subtractQuantitySuccess() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val quantityToSubtract = 50
        val expectedQuantity = 50
        
        // when
        val updatedOption = productOption.subtract(quantityToSubtract)
        
        // then
        assertEquals(expectedQuantity, updatedOption.availableQuantity)
        assertNotEquals(updatedOption.createdAt, updatedOption.updatedAt)
    }
    
    @Test
    @DisplayName("수량 차감 시 음수값 입력하면 예외 발생")
    fun subtractNegativeQuantity() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val invalidQuantity = -10
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOption.subtract(invalidQuantity)
        }
        
        assertTrue(exception.message!!.contains("Available quantity must be between"))
    }
    
    @Test
    @DisplayName("수량 차감 시 최대값보다 클 경우 예외 발생")
    fun subtractTooLargeQuantity() {
        // given
        val product = mockk<Product>()
        val productOption = ProductOption.create(product, "옵션", 100, 1000.0)
        val invalidQuantity = 1001 // 최대값 1000보다 큼
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOption.subtract(invalidQuantity)
        }
        
        assertTrue(exception.message!!.contains("Available quantity must be between"))
    }
} 