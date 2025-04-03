package kr.hhplus.be.server.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductOption
import kr.hhplus.be.server.repository.product.ProductOptionRepository
import kr.hhplus.be.server.service.product.ProductOptionService
import kr.hhplus.be.server.service.product.ProductService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class ProductOptionServiceUnitTest {
    private lateinit var productOptionRepository: ProductOptionRepository
    private lateinit var productService: ProductService
    private lateinit var productOptionService: ProductOptionService

    @BeforeEach
    fun setUp() {
        productOptionRepository = mockk()
        productService = mockk()
        productOptionService = ProductOptionService(productOptionRepository, productService)
    }

    @Test
    fun `create 성공`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val name = "옵션1"
        val additionalPrice = BigDecimal("500")
        val productPrice = BigDecimal("1000")

        val product = mockk<Product>()
        every { product.price } returns productPrice

        val option = mockk<ProductOption>()
        val capturedOption = slot<ProductOption>()

        every { productService.getById(productId) } returns product
        every { ProductOption.create(optionId, productId, name, additionalPrice, productPrice) } returns option
        every { productOptionRepository.save(option) } returns option

        // Act
        val result = productOptionService.create(optionId, productId, name, additionalPrice)

        // Assert
        assertSame(option, result)
        verify(exactly = 1) { productService.getById(productId) }
        verify(exactly = 1) { productOptionRepository.save(option) }
    }

    @Test
    fun `create 실패 - 존재하지 않는 상품`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val name = "옵션1"
        val additionalPrice = BigDecimal("500")
        val errorMessage = "Product not found for productId: $productId"

        every { productService.getById(productId) } throws IllegalArgumentException(errorMessage)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.create(optionId, productId, name, additionalPrice)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productService.getById(productId) }
        verify(exactly = 0) { productOptionRepository.save(any()) }
    }

    @Test
    fun `getById 성공`() {
        // Arrange
        val optionId = 1
        val option = mockk<ProductOption>()

        every { productOptionRepository.findById(optionId) } returns option

        // Act
        val result = productOptionService.getById(optionId)

        // Assert
        assertSame(option, result)
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
    }

    @Test
    fun `getById 실패 - 존재하지 않는 옵션`() {
        // Arrange
        val optionId = 1

        every { productOptionRepository.findById(optionId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getById(optionId)
        }
        assertEquals("Product option not found for optionId: $optionId", exception.message)
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
    }

    @Test
    fun `getByProductId 성공`() {
        // Arrange
        val productId = 1
        val options = listOf<ProductOption>(mockk(), mockk())

        every { productOptionRepository.findByProductId(productId) } returns options

        // Act
        val result = productOptionService.getByProductId(productId)

        // Assert
        assertEquals(options, result)
        verify(exactly = 1) { productOptionRepository.findByProductId(productId) }
    }

    @Test
    fun `update 성공`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val newName = "새옵션"
        val newPrice = BigDecimal("600")
        val productPrice = BigDecimal("1000")

        val originalOption = mockk<ProductOption>()
        every { originalOption.productId } returns productId
        
        val product = mockk<Product>()
        every { product.price } returns productPrice

        val updatedOption = mockk<ProductOption>()

        every { productOptionRepository.findById(optionId) } returns originalOption
        every { productService.getById(productId) } returns product
        every { originalOption.update(newName, newPrice, productPrice) } returns updatedOption
        every { productOptionRepository.update(updatedOption) } returns updatedOption

        // Act
        val result = productOptionService.update(optionId, newName, newPrice)

        // Assert
        assertSame(updatedOption, result)
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
        verify(exactly = 1) { productService.getById(productId) }
        verify(exactly = 1) { originalOption.update(newName, newPrice, productPrice) }
        verify(exactly = 1) { productOptionRepository.update(updatedOption) }
    }

    @Test
    fun `update 실패 - 존재하지 않는 옵션`() {
        // Arrange
        val optionId = 1
        val newName = "새옵션"
        val newPrice = BigDecimal("600")

        every { productOptionRepository.findById(optionId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.update(optionId, newName, newPrice)
        }
        assertEquals("Product option not found for optionId: $optionId", exception.message)
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
        verify(exactly = 0) { productService.getById(any()) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }

    @Test
    fun `update 실패 - 존재하지 않는 상품`() {
        // Arrange
        val optionId = 1
        val productId = 2
        val newName = "새옵션"
        val newPrice = BigDecimal("600")
        val errorMessage = "Product not found for productId: $productId"

        val originalOption = mockk<ProductOption>()
        every { originalOption.productId } returns productId

        every { productOptionRepository.findById(optionId) } returns originalOption
        every { productService.getById(productId) } throws IllegalArgumentException(errorMessage)

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.update(optionId, newName, newPrice)
        }
        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
        verify(exactly = 1) { productService.getById(productId) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }

    @Test
    fun `delete 성공`() {
        // Arrange
        val optionId = 1
        val option = mockk<ProductOption>()

        every { productOptionRepository.findById(optionId) } returns option
        every { productOptionRepository.delete(optionId) } returns Unit

        // Act
        productOptionService.delete(optionId)

        // Assert
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
        verify(exactly = 1) { productOptionRepository.delete(optionId) }
    }

    @Test
    fun `delete 실패 - 존재하지 않는 옵션`() {
        // Arrange
        val optionId = 1

        every { productOptionRepository.findById(optionId) } returns null

        // Act & Assert
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.delete(optionId)
        }
        assertEquals("Product option not found for optionId: $optionId", exception.message)
        verify(exactly = 1) { productOptionRepository.findById(optionId) }
        verify(exactly = 0) { productOptionRepository.delete(any()) }
    }
} 