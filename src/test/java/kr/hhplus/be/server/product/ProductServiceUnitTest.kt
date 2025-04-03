package kr.hhplus.be.server.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.repository.product.ProductRepository
import kr.hhplus.be.server.service.product.ProductService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ProductServiceUnitTest {
    private lateinit var productRepository: ProductRepository
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() {
        productRepository = mockk()
        productService = ProductService(productRepository)
    }

    @Test
    fun `상품 조회 성공`() {
        // Arrange
        val productId = 1
        val product = Product.create(
            productId = productId,
            name = "Test Product",
            description = "A test product",
            price = BigDecimal("100.00")
        )
        every { productRepository.findById(productId) } returns product

        // Act
        val result = productService.getById(productId)

        // Assert
        assertEquals(product, result)
        verify(exactly = 1) { productRepository.findById(productId) }
    }

    @Test
    fun `존재하지 않는 상품 조회 시 예외 발생`() {
        // Arrange
        val productId = 1
        every { productRepository.findById(productId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productService.getById(productId)
        }
        assertEquals("Product not found for productId: $productId", exception.message)
        verify(exactly = 1) { productRepository.findById(productId) }
    }

    @Test
    fun `상품 저장 성공`() {
        // Arrange
        val productId = 1
        val name = "Test Product"
        val description = "A test product"
        val price = BigDecimal("100.00")
        
        val product = Product.create(productId, name, description, price)
        
        // Capture the product that is passed to save
        val capturedProduct = slot<Product>()
        every { productRepository.save(capture(capturedProduct)) } returns product
        
        // Act
        val result = productService.save(productId, name, description, price)
        
        // Assert
        assertEquals(product, result)
        assertEquals(productId, capturedProduct.captured.productId)
        assertEquals(name, capturedProduct.captured.name)
        assertEquals(description, capturedProduct.captured.description)
        assertEquals(price, capturedProduct.captured.price)
        verify(exactly = 1) { productRepository.save(any()) }
    }
    
    @Test
    fun `저장 시 레포지토리 예외 전파`() {
        // Arrange
        val productId = 1
        val name = "Test Product"
        val description = "A test product"
        val price = BigDecimal("100.00")
        val errorMessage = "Database connection error"
        
        every { productRepository.save(any()) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            productService.save(productId, name, description, price)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productRepository.save(any()) }
    }
    
    @Test
    fun `상품 업데이트 성공`() {
        // Arrange
        val productId = 1
        val originalProduct = Product.create(
            productId = productId,
            name = "Original Product",
            description = "Original description",
            price = BigDecimal("100.00")
        )
        
        val newName = "Updated Product"
        val capturedProduct = slot<Product>()
        
        every { productRepository.findById(productId) } returns originalProduct
        every { productRepository.update(capture(capturedProduct)) } answers { capturedProduct.captured }
        
        // Act
        val result = productService.update(
            productId = productId,
            name = newName,
            description = null,
            price = null
        )
        
        // Assert
        assertEquals(newName, result.name)
        assertEquals(originalProduct.description, result.description) // unchanged
        assertEquals(originalProduct.price, result.price) // unchanged
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    fun `모든 필드 동시에 업데이트 성공`() {
        // Arrange
        val productId = 1
        val originalProduct = Product.create(
            productId = productId,
            name = "Original Product",
            description = "Original description",
            price = BigDecimal("100.00")
        )
        
        val newName = "Complete Update"
        val newDescription = "Completely updated description"
        val newPrice = BigDecimal("500.00")
        val capturedProduct = slot<Product>()
        
        every { productRepository.findById(productId) } returns originalProduct
        every { productRepository.update(capture(capturedProduct)) } answers { capturedProduct.captured }
        
        // Act
        val result = productService.update(
            productId = productId,
            name = newName,
            description = newDescription,
            price = newPrice
        )
        
        // Assert
        assertEquals(newName, result.name)
        assertEquals(newDescription, result.description)
        assertEquals(newPrice, result.price)
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    fun `설명만 업데이트 성공`() {
        // Arrange
        val productId = 1
        val originalProduct = Product.create(
            productId = productId,
            name = "Original Product",
            description = "Original description",
            price = BigDecimal("100.00")
        )
        
        val newDescription = "Only description updated"
        val capturedProduct = slot<Product>()
        
        every { productRepository.findById(productId) } returns originalProduct
        every { productRepository.update(capture(capturedProduct)) } answers { capturedProduct.captured }
        
        // Act
        val result = productService.update(
            productId = productId,
            name = null,
            description = newDescription,
            price = null
        )
        
        // Assert
        assertEquals(originalProduct.name, result.name) // unchanged
        assertEquals(newDescription, result.description)
        assertEquals(originalProduct.price, result.price) // unchanged
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    fun `가격만 업데이트 성공`() {
        // Arrange
        val productId = 1
        val originalProduct = Product.create(
            productId = productId,
            name = "Original Product",
            description = "Original description",
            price = BigDecimal("100.00")
        )
        
        val newPrice = BigDecimal("999.99")
        val capturedProduct = slot<Product>()
        
        every { productRepository.findById(productId) } returns originalProduct
        every { productRepository.update(capture(capturedProduct)) } answers { capturedProduct.captured }
        
        // Act
        val result = productService.update(
            productId = productId,
            name = null,
            description = null,
            price = newPrice
        )
        
        // Assert
        assertEquals(originalProduct.name, result.name) // unchanged
        assertEquals(originalProduct.description, result.description) // unchanged
        assertEquals(newPrice, result.price)
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    fun `존재하지 않는 상품 업데이트 시 예외 발생`() {
        // Arrange
        val productId = 1
        every { productRepository.findById(productId) } returns null
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productService.update(
                productId = productId,
                name = "Updated Product",
                description = null,
                price = null
            )
        }
        assertEquals("Product not found for productId: $productId", exception.message)
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 0) { productRepository.update(any()) }
    }
    
    @Test
    fun `업데이트 시 레포지토리 예외 전파`() {
        // Arrange
        val productId = 1
        val originalProduct = Product.create(
            productId = productId,
            name = "Original Product",
            description = "Original description",
            price = BigDecimal("100.00")
        )
        val errorMessage = "Database error during update"
        
        every { productRepository.findById(productId) } returns originalProduct
        every { productRepository.update(any()) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            productService.update(
                productId = productId,
                name = "New Name",
                description = null,
                price = null
            )
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    fun `상품 삭제 성공`() {
        // Arrange
        val productId = 1
        val product = Product.create(
            productId = productId,
            name = "Test Product",
            description = "A test product",
            price = BigDecimal("100.00")
        )
        
        every { productRepository.findById(productId) } returns product
        every { productRepository.delete(productId) } returns Unit
        
        // Act
        productService.delete(productId)
        
        // Assert
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.delete(productId) }
    }
    
    @Test
    fun `존재하지 않는 상품 삭제 시 예외 발생`() {
        // Arrange
        val productId = 1
        every { productRepository.findById(productId) } returns null
        
        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productService.delete(productId)
        }
        assertEquals("Product not found for productId: $productId", exception.message)
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 0) { productRepository.delete(any()) }
    }
    
    @Test
    fun `삭제 시 레포지토리 예외 전파`() {
        // Arrange
        val productId = 1
        val product = Product.create(
            productId = productId,
            name = "Test Product",
            description = "A test product",
            price = BigDecimal("100.00")
        )
        val errorMessage = "Database error during delete"
        
        every { productRepository.findById(productId) } returns product
        every { productRepository.delete(productId) } throws RuntimeException(errorMessage)
        
        // Act & Assert
        val exception = assertThrows<RuntimeException> {
            productService.delete(productId)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.delete(productId) }
    }
}