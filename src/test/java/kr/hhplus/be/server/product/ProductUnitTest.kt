package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.model.Product
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ProductUnitTest {
    
    @Test
    @DisplayName("유효한 데이터로 Product 객체 생성 성공")
    fun createProductWithValidData() {
        // given
        val name = "테스트 상품"
        val description = "테스트 상품 설명"
        val price = 10000.0
        
        // when
        val product = Product.create(name, description, price)
        
        // then
        assertEquals(name, product.name)
        assertEquals(description, product.description)
        assertEquals(price, product.price)
        assertNotNull(product.createdAt)
        assertNotNull(product.updatedAt)
    }
    
    @Test
    @DisplayName("가격이 최소값보다 낮을 경우 예외 발생")
    fun createProductWithTooLowPrice() {
        // given
        val name = "테스트 상품"
        val description = "테스트 상품 설명"
        val price = 0.0 // 최소값은 1.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Product.create(name, description, price)
        }
        
        assertTrue(exception.message!!.contains("Initial amount must be between"))
    }
    
    @Test
    @DisplayName("가격이 최대값보다 높을 경우 예외 발생")
    fun createProductWithTooHighPrice() {
        // given
        val name = "테스트 상품"
        val description = "테스트 상품 설명"
        val price = 1000001.0 // 최대값은 1000000.0
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            Product.create(name, description, price)
        }
        
        assertTrue(exception.message!!.contains("Initial amount must be between"))
    }
    
    @Test
    @DisplayName("유효한 데이터로 상품 정보 업데이트 성공")
    fun updateProductWithValidData() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val newName = "업데이트된 상품"
        val newDescription = "업데이트된 상품 설명"
        val newPrice = 20000.0
        
        // when
        val updatedProduct = product.update(newName, newDescription, newPrice)
        
        // then
        assertEquals(newName, updatedProduct.name)
        assertEquals(newDescription, updatedProduct.description)
        assertEquals(newPrice, updatedProduct.price)
        assertNotEquals(updatedProduct.createdAt, updatedProduct.updatedAt)
    }
    
    @Test
    @DisplayName("이름만 업데이트 성공")
    fun updateOnlyName() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val originalDescription = product.description
        val originalPrice = product.price
        val newName = "업데이트된 상품"
        
        // when
        val updatedProduct = product.update(newName, null, null)
        
        // then
        assertEquals(newName, updatedProduct.name)
        assertEquals(originalDescription, updatedProduct.description)
        assertEquals(originalPrice, updatedProduct.price)
    }
    
    @Test
    @DisplayName("설명만 업데이트 성공")
    fun updateOnlyDescription() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val originalName = product.name
        val originalPrice = product.price
        val newDescription = "업데이트된 상품 설명"
        
        // when
        val updatedProduct = product.update(null, newDescription, null)
        
        // then
        assertEquals(originalName, updatedProduct.name)
        assertEquals(newDescription, updatedProduct.description)
        assertEquals(originalPrice, updatedProduct.price)
    }
    
    @Test
    @DisplayName("가격만 업데이트 성공")
    fun updateOnlyPrice() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val originalName = product.name
        val originalDescription = product.description
        val newPrice = 20000.0
        
        // when
        val updatedProduct = product.update(null, null, newPrice)
        
        // then
        assertEquals(originalName, updatedProduct.name)
        assertEquals(originalDescription, updatedProduct.description)
        assertEquals(newPrice, updatedProduct.price)
    }
    
    @Test
    @DisplayName("이름이 너무 짧을 경우 업데이트 시 예외 발생")
    fun updateWithTooShortName() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val invalidName = "테" // 최소 3자 필요
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            product.update(invalidName, null, null)
        }
        
        assertTrue(exception.message!!.contains("Name must be between"))
    }
    
    @Test
    @DisplayName("이름이 너무 길 경우 업데이트 시 예외 발생")
    fun updateWithTooLongName() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val invalidName = "아주아주아주아앙아아아아아아아아아ㅏ아앙주아주아주아주아주긴이름" // 최대 20자 필요
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            product.update(invalidName, null, null)
        }
        
        assertTrue(exception.message!!.contains("Name must be between"))
    }
    
    @Test
    @DisplayName("가격이 너무 낮을 경우 업데이트 시 예외 발생")
    fun updateWithTooLowPrice() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val invalidPrice = 0.0 // 최소 1.0 필요
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            product.update(null, null, invalidPrice)
        }
        
        assertTrue(exception.message!!.contains("Price must be between"))
    }
    
    @Test
    @DisplayName("가격이 너무 높을 경우 업데이트 시 예외 발생")
    fun updateWithTooHighPrice() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val invalidPrice = 1000001.0 // 최대 1000000.0 필요
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            product.update(null, null, invalidPrice)
        }
        
        assertTrue(exception.message!!.contains("Price must be between"))
    }
}