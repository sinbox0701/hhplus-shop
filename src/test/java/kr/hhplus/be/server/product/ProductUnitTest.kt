package kr.hhplus.be.server.product

import kr.hhplus.be.server.domain.product.Product
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class ProductUnitTest {

    @Test
    fun `create returns Product when valid parameters provided`() {
        // Arrange
        val productId = 1L
        val name = "Test Product"
        val description = "A test product"
        val price = 100.0

        // Act
        val product = Product.create(productId, name, description, price)

        // Assert
        assertEquals(productId, product.id)
        assertEquals(name, product.name)
        assertEquals(description, product.description)
        assertEquals(price, product.price)
    }

    @Test
    fun `create throws exception when price below minimum`() {
        // Arrange
        val productId = 1L
        val name = "Test Product"
        val description = "A test product"
        val invalidPrice = 0.5 // Below minimum price of 1

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Product.create(productId, name, description, invalidPrice)
        }
        assertEquals("Initial amount must be between 1 and 1000000", exception.message)
    }

    @Test
    fun `create throws exception when price above maximum`() {
        // Arrange
        val productId = 1L
        val name = "Test Product"
        val description = "A test product"
        val invalidPrice = 1000001.0 // Above maximum price of 1000000

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            Product.create(productId, name, description, invalidPrice)
        }
        assertEquals("Initial amount must be between 1 and 1000000", exception.message)
    }

    @Test
    fun `create with boundary values succeeds`() {
        // Arrange
        val productId = 1L
        val name = "Test Product"
        val description = "A test product"
        
        // Minimum price boundary
        val minPrice = 1.0
        // Maximum price boundary
        val maxPrice = 1000000.0
        
        // Act & Assert - Min price
        val minPriceProduct = Product.create(productId, name, description, minPrice)
        assertEquals(minPrice, minPriceProduct.price)
        
        // Act & Assert - Max price
        val maxPriceProduct = Product.create(productId, name, description, maxPrice)
        assertEquals(maxPrice, maxPriceProduct.price)
    }

    @Test
    fun `create with empty description succeeds`() {
        // Arrange
        val productId = 1L
        val name = "Test Product"
        val emptyDescription = ""
        val price = 100.0

        // Act
        val product = Product.create(productId, name, emptyDescription, price)

        // Assert
        assertEquals(emptyDescription, product.description)
    }
    
    @Test
    fun `update name with boundary length values`() {
        // Arrange
        val product = Product.create(
            id = 1L,
            name = "Original",
            description = "Original description",
            price = 100.0
        )
        
        // Minimum name length (3 characters)
        val minLengthName = "Min"
        // Maximum name length (20 characters)
        val maxLengthName = "ThisIsAVeryLongName12"
        
        // Act & Assert - Min length
        val minLengthUpdated = product.update(name = minLengthName)
        assertEquals(minLengthName, minLengthUpdated.name)
        
        // Act & Assert - Max length
        val maxLengthUpdated = product.update(name = maxLengthName)
        assertEquals(maxLengthName, maxLengthUpdated.name)
    }
    
    @Test
    fun `update with name exceeding maximum length throws exception`() {
        // Arrange
        val product = Product.create(
            id = 1L,
            name = "Original Product",
            description = "Original description",
            price = 100.0
        )
        val invalidName = "ThisNameIsTooLongAndExceedsTwentyCharacters" // 더 긴 이름 (> 20자)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            product.update(name = invalidName)
        }
        assertEquals("Name must be between 3 and 20 characters", exception.message)
    }

    @Test
    fun `update with valid values successfully updates the product`() {
        // Arrange
        val product = Product.create(
            id = 1L,
            name = "Original Product",
            description = "Original description",
            price = 100.0
        )
        val newName = "Updated Product"
        val newDescription = "Updated description"
        val newPrice = 200.0

        // Act
        val updatedProduct = product.update(
            name = newName,
            description = newDescription,
            price = newPrice
        )

        // Assert
        assertEquals(newName, updatedProduct.name)
        assertEquals(newDescription, updatedProduct.description)
        assertEquals(newPrice, updatedProduct.price)
    }

    @Test
    fun `update with invalid name throws exception`() {
        // Arrange
        val product = Product.create(
            id = 1L,
            name = "Original Product",
            description = "Original description",
            price = 100.0
        )
        val invalidName = "AB" // Too short (< 3 characters)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            product.update(name = invalidName)
        }
        assertEquals("Name must be between 3 and 20 characters", exception.message)
    }

    @Test
    fun `update with invalid price throws exception`() {
        // Arrange
        val product = Product.create(
            id = 1L,
            name = "Original Product",
            description = "Original description",
            price = 100.0
        )
        val invalidPrice = 1000001.0 // Above maximum

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            product.update(price = invalidPrice)
        }
        assertEquals("Price must be between 1 and 1000000", exception.message)
    }

    @Test
    fun `partial update only updates provided fields`() {
        // Arrange
        val originalName = "Original Product"
        val originalDescription = "Original description"
        val originalPrice = 100.0

        val product = Product.create(
            id = 1L,
            name = originalName,
            description = originalDescription,
            price = originalPrice
        )

        val newName = "Updated Product"

        // Act - only update name
        val updatedProduct = product.update(name = newName)

        // Assert
        assertEquals(newName, updatedProduct.name)
        assertEquals(originalDescription, updatedProduct.description)
        assertEquals(originalPrice, updatedProduct.price)
    }
}