package kr.hhplus.be.server.product

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import kr.hhplus.be.server.domain.product.service.ProductCommand
import kr.hhplus.be.server.domain.product.service.ProductService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProductServiceUnitTest {

    @MockK
    private lateinit var productRepository: ProductRepository
    private lateinit var productService: ProductService

    // 테스트 상수 정의
    companion object {
        private const val TEST_NAME = "테스트 상품"
        private const val TEST_DESCRIPTION = "테스트 상품 설명"
        private const val TEST_PRICE = 10000.0
        
        private const val UPDATED_NAME = "업데이트된 상품"
        private const val UPDATED_DESCRIPTION = "업데이트된 상품 설명"
        private const val UPDATED_PRICE = 20000.0
    }

    @BeforeEach
    fun setup() {
        productService = ProductService(productRepository)
    }

    @Test
    @DisplayName("새로운 상품 생성 성공")
    fun createProductSuccess() {
        // given
        val command = ProductCommand.CreateProductCommand(TEST_NAME, TEST_DESCRIPTION, TEST_PRICE)
        val product = mockk<Product> {
            every { name } returns TEST_NAME
            every { description } returns TEST_DESCRIPTION
            every { price } returns TEST_PRICE
        }
        
        every { productRepository.save(any()) } returns product
        
        // when
        val createdProduct = productService.create(command)
        
        // then
        assertEquals(TEST_NAME, createdProduct.name)
        assertEquals(TEST_DESCRIPTION, createdProduct.description)
        assertEquals(TEST_PRICE, createdProduct.price)
        
        verify(exactly = 1) { productRepository.save(any()) }
    }
    
    @Test
    @DisplayName("ID로 상품 조회 성공")
    fun getProductByIdSuccess() {
        // given
        val product = mockk<Product> {
            every { id } returns 1L
            every { name } returns TEST_NAME
            every { description } returns TEST_DESCRIPTION
            every { price } returns TEST_PRICE
        }
        
        every { productRepository.findById(1L) } returns product
        
        // when
        val foundProduct = productService.get(1L)
        
        // then
        assertEquals(TEST_NAME, foundProduct.name)
        assertEquals(TEST_DESCRIPTION, foundProduct.description)
        assertEquals(TEST_PRICE, foundProduct.price)
        
        verify(exactly = 1) { productRepository.findById(1L) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 상품 조회 시 예외 발생")
    fun getProductByIdNotFound() {
        // given
        every { productRepository.findById(1L) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.get(1L)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(1L) }
    }
    
    @Test
    @DisplayName("모든 상품 조회 성공")
    fun getAllProductsSuccess() {
        // given
        val product1 = mockk<Product> {
            every { name } returns TEST_NAME
            every { description } returns TEST_DESCRIPTION
            every { price } returns TEST_PRICE
        }
        val product2 = mockk<Product> {
            every { name } returns "상품2"
            every { description } returns "설명2"
            every { price } returns 20000.0
        }
        val products = listOf(product1, product2)
        
        every { productRepository.findAll() } returns products
        
        // when
        val result = productService.getAll()
        
        // then
        assertEquals(2, result.size)
        assertEquals(TEST_NAME, result[0].name)
        assertEquals("상품2", result[1].name)
        
        verify(exactly = 1) { productRepository.findAll() }
    }
    
    @Test
    @DisplayName("상품 정보 업데이트 성공")
    fun updateProductSuccess() {
        // given
        val product = mockk<Product> {
            every { id } returns 1L
            every { update(any(), any(), any()) } returns mockk {
                every { name } returns UPDATED_NAME
                every { description } returns UPDATED_DESCRIPTION
                every { price } returns UPDATED_PRICE
            }
        }
        
        val command = ProductCommand.UpdateProductCommand(1L, UPDATED_NAME, UPDATED_DESCRIPTION, UPDATED_PRICE)
        
        every { productRepository.findById(1L) } returns product
        every { productRepository.save(any()) } answers {
            firstArg<Product>().run {
                mockk {
                    every { name } returns UPDATED_NAME
                    every { description } returns UPDATED_DESCRIPTION
                    every { price } returns UPDATED_PRICE
                }
            }
        }
        
        // when
        val result = productService.update(command)
        
        // then
        assertEquals(UPDATED_NAME, result.name)
        assertEquals(UPDATED_DESCRIPTION, result.description)
        assertEquals(UPDATED_PRICE, result.price)
        
        verify(exactly = 1) { productRepository.findById(1L) }
        verify(exactly = 1) { productRepository.save(any()) }
        verify(exactly = 1) { product.update(UPDATED_NAME, UPDATED_DESCRIPTION, UPDATED_PRICE) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 업데이트 시 예외 발생")
    fun updateNonExistentProduct() {
        // given
        val command = ProductCommand.UpdateProductCommand(1L, UPDATED_NAME, UPDATED_DESCRIPTION, UPDATED_PRICE)
        
        every { productRepository.findById(1L) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.update(command)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(1L) }
        verify(exactly = 0) { productRepository.save(any()) }
    }
    
    @Test
    @DisplayName("일부 필드만 업데이트 성공")
    fun updatePartialFieldsSuccess() {
        // given
        val product = mockk<Product> {
            every { id } returns 1L
            every { name } returns TEST_NAME
            every { description } returns TEST_DESCRIPTION
            every { price } returns TEST_PRICE
            every { update(any(), any(), any()) } returns mockk {
                every { name } returns UPDATED_NAME
                every { description } returns TEST_DESCRIPTION
                every { price } returns TEST_PRICE
            }
        }
        
        val command = ProductCommand.UpdateProductCommand(1L, UPDATED_NAME)
        
        every { productRepository.findById(1L) } returns product
        every { productRepository.save(any()) } answers {
            firstArg<Product>().run {
                mockk {
                    every { name } returns UPDATED_NAME
                    every { description } returns TEST_DESCRIPTION
                    every { price } returns TEST_PRICE
                }
            }
        }
        
        // when
        val result = productService.update(command)
        
        // then
        assertEquals(UPDATED_NAME, result.name)
        assertEquals(TEST_DESCRIPTION, result.description)
        assertEquals(TEST_PRICE, result.price)
        
        verify(exactly = 1) { productRepository.findById(1L) }
        verify(exactly = 1) { productRepository.save(any()) }
        verify(exactly = 1) { product.update(UPDATED_NAME, null, null) }
    }
    
    @Test
    @DisplayName("상품 삭제 성공")
    fun deleteProductSuccess() {
        // given
        val product = mockk<Product> {
            every { id } returns 1L
        }
        
        every { productRepository.findById(1L) } returns product
        every { productRepository.delete(1L) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productService.delete(1L)
        }
        
        verify(exactly = 1) { productRepository.findById(1L) }
        verify(exactly = 1) { productRepository.delete(1L) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외 발생")
    fun deleteNonExistentProduct() {
        // given
        every { productRepository.findById(1L) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.delete(1L)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(1L) }
        verify(exactly = 0) { productRepository.delete(any()) }
    }
} 