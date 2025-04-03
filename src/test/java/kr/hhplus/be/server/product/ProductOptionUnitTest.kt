package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.ProductOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ProductOptionUnitTest {

    @Test
    fun `create returns ProductOption when valid parameters provided`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val name = "옵션1"
        val additionalPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")

        // Act
        val option = ProductOption.create(optionId, productId, name, additionalPrice, productPrice)

        // Assert
        assertEquals(optionId, option.optionId)
        assertEquals(productId, option.productId)
        assertEquals(name, option.name)
        assertEquals(additionalPrice, option.additionalPrice)
    }

    @Test
    fun `create throws exception when name is too short`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val name = ""  // Empty name (less than minimum 1 character)
        val additionalPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(optionId, productId, name, additionalPrice, productPrice)
        }
        assertEquals("Name must be between 1 and 10 characters", exception.message)
    }

    @Test
    fun `create throws exception when name is too long`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val name = "아주아주아주아주긴옵션이름"  // More than 10 characters
        val additionalPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(optionId, productId, name, additionalPrice, productPrice)
        }
        assertEquals("Name must be between 1 and 10 characters", exception.message)
    }

    @Test
    fun `create throws exception when additionalPrice exceeds productPrice`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val name = "옵션1"
        val additionalPrice = BigDecimal("1500")  // More than product price
        val productPrice = BigDecimal("1000")

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(optionId, productId, name, additionalPrice, productPrice)
        }
        assertEquals("Additional price must not exceed product price", exception.message)
    }

    @Test
    fun `update returns ProductOption when valid parameters provided`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val initialName = "옵션1"
        val initialPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val newName = "새옵션"
        val newPrice = BigDecimal("800")

        // Act
        val updatedOption = option.update(newName, newPrice, productPrice)

        // Assert
        assertEquals(optionId, updatedOption.optionId)
        assertEquals(productId, updatedOption.productId)
        assertEquals(newName, updatedOption.name)
        assertEquals(newPrice, updatedOption.additionalPrice)
    }

    @Test
    fun `update throws exception when name is invalid`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val initialName = "옵션1"
        val initialPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val invalidName = "이건너무긴옵션이름입니다"  // More than 10 characters

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            option.update(invalidName, null, productPrice)
        }
        assertEquals("Name must be between 1 and 10 characters", exception.message)
    }

    @Test
    fun `update throws exception when additionalPrice exceeds productPrice`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val initialName = "옵션1"
        val initialPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val invalidPrice = BigDecimal("1200")  // More than product price

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            option.update(null, invalidPrice, productPrice)
        }
        assertEquals("Additional price must not exceed product price", exception.message)
    }

    @Test
    fun `update only updates provided fields`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val initialName = "옵션1"
        val initialPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val newName = "새옵션"

        // Act - only update name
        val updatedOption = option.update(newName, null, productPrice)

        // Assert
        assertEquals(newName, updatedOption.name)
        assertEquals(initialPrice, updatedOption.additionalPrice)  // Price should remain unchanged
    }
} 