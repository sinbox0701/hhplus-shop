package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.ProductOption
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class ProductOptionUnitTest {

    @Test
    fun `create returns ProductOption when valid parameters provided`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val name = "옵션1"
        val additionalPrice = 500.0
        val productPrice = 1000.0

        // Act
        val option = ProductOption.create(optionId, productId, name, additionalPrice, productPrice)

        // Assert
        assertEquals(optionId, option.id)
        assertEquals(productId, option.productId)
        assertEquals(name, option.name)
        assertEquals(additionalPrice, option.additionalPrice)
    }

    @Test
    fun `create throws exception when name is too short`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val name = ""  // Empty name (less than minimum 1 character)
        val additionalPrice = 500.0
        val productPrice = 1000.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(optionId, productId, name, additionalPrice, productPrice)
        }
        assertEquals("Name must be between 1 and 10 characters", exception.message)
    }

    @Test
    fun `create throws exception when name is too long`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val name = "아주아주아주아주긴옵션이름"  // More than 10 characters
        val additionalPrice = 500.0
        val productPrice = 1000.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(optionId, productId, name, additionalPrice, productPrice)
        }
        assertEquals("Name must be between 1 and 10 characters", exception.message)
    }

    @Test
    fun `create throws exception when additionalPrice exceeds productPrice`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val name = "옵션1"
        val additionalPrice = 1500.0  // More than product price
        val productPrice = 1000.0

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            ProductOption.create(optionId, productId, name, additionalPrice, productPrice)
        }
        assertEquals("Additional price must not exceed product price", exception.message)
    }

    @Test
    fun `update returns ProductOption when valid parameters provided`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val initialName = "옵션1"
        val initialPrice = 500.0
        val productPrice = 1000.0
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val newName = "새옵션"
        val newPrice = 800.0

        // Act
        val updatedOption = option.update(newName, newPrice, productPrice)

        // Assert
        assertEquals(optionId, updatedOption.id)
        assertEquals(productId, updatedOption.productId)
        assertEquals(newName, updatedOption.name)
        assertEquals(newPrice, updatedOption.additionalPrice)
    }

    @Test
    fun `update throws exception when name is invalid`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val initialName = "옵션1"
        val initialPrice = 500.0
        val productPrice = 1000.0
        
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
        val optionId = 1L
        val productId = 2L
        val initialName = "옵션1"
        val initialPrice = 500.0
        val productPrice = 1000.0
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val invalidPrice = 1200.0  // More than product price

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            option.update(null, invalidPrice, productPrice)
        }
        assertEquals("Additional price must not exceed product price", exception.message)
    }

    @Test
    fun `update only updates provided fields`() {
        // Arrange
        val optionId = 1L
        val productId = 2L
        val initialName = "옵션1"
        val initialPrice = 500.0
        val productPrice = 1000.0
        
        val option = ProductOption.create(optionId, productId, initialName, initialPrice, productPrice)
        
        val newName = "새옵션"

        // Act - only update name
        val updatedOption = option.update(newName, null, productPrice)

        // Assert
        assertEquals(newName, updatedOption.name)
        assertEquals(initialPrice, updatedOption.additionalPrice)  // Price should remain unchanged
    }
} 