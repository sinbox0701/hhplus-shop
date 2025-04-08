package kr.hhplus.be.server.product

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import kr.hhplus.be.server.domain.product.service.ProductService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductServiceUnitTest {

    @MockK
    private lateinit var productRepository: ProductRepository

    @InjectMockKs
    private lateinit var productService: ProductService

    private val testId = 1L
    private val testName = "테스트 상품"
    private val testDescription = "테스트 상품 설명"
    private val testPrice = 10000.0

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    @DisplayName("새로운 상품 생성 성공")
    fun createProductSuccess() {
        // given
        val product = Product.create(testId, testName, testDescription, testPrice)
        
        every { productRepository.save(any()) } returns product
        
        // when
        val createdProduct = productService.createProduct(testName, testDescription, testPrice)
        
        // then
        assertEquals(testName, createdProduct.name)
        assertEquals(testDescription, createdProduct.description)
        assertEquals(testPrice, createdProduct.price)
        
        verify(exactly = 1) { productRepository.save(any()) }
    }
    
    @Test
    @DisplayName("ID로 상품 조회 성공")
    fun getProductByIdSuccess() {
        // given
        val product = Product.create(testId, testName, testDescription, testPrice)
        
        every { productRepository.findById(testId) } returns product
        
        // when
        val foundProduct = productService.getProduct(testId)
        
        // then
        assertEquals(testId, foundProduct.id)
        assertEquals(testName, foundProduct.name)
        assertEquals(testDescription, foundProduct.description)
        assertEquals(testPrice, foundProduct.price)
        
        verify(exactly = 1) { productRepository.findById(testId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 상품 조회 시 예외 발생")
    fun getProductByIdNotFound() {
        // given
        every { productRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.getProduct(testId)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testId) }
    }
    
    @Test
    @DisplayName("상품 정보 업데이트 성공")
    fun updateProductSuccess() {
        // given
        val product = Product.create(testId, testName, testDescription, testPrice)
        val newName = "업데이트된 상품"
        val newDescription = "업데이트된 상품 설명"
        val newPrice = 20000.0
        
        val updatedProduct = product.update(newName, newDescription, newPrice)
        
        every { productRepository.findById(testId) } returns product
        every { productRepository.update(any()) } returns updatedProduct
        
        // when
        val result = productService.updateProduct(testId, newName, newDescription, newPrice)
        
        // then
        assertEquals(newName, result.name)
        assertEquals(newDescription, result.description)
        assertEquals(newPrice, result.price)
        
        verify(exactly = 1) { productRepository.findById(testId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 업데이트 시 예외 발생")
    fun updateNonExistentProduct() {
        // given
        val newName = "업데이트된 상품"
        val newDescription = "업데이트된 상품 설명"
        val newPrice = 20000.0
        
        every { productRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.updateProduct(testId, newName, newDescription, newPrice)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testId) }
        verify(exactly = 0) { productRepository.update(any()) }
    }
    
    @Test
    @DisplayName("일부 필드만 업데이트 성공")
    fun updatePartialFieldsSuccess() {
        // given
        val product = Product.create(testId, testName, testDescription, testPrice)
        val newName = "업데이트된 상품"
        
        val updatedProduct = product.update(newName, null, null)
        
        every { productRepository.findById(testId) } returns product
        every { productRepository.update(any()) } returns updatedProduct
        
        // when
        val result = productService.updateProduct(testId, newName, null, null)
        
        // then
        assertEquals(newName, result.name)
        assertEquals(testDescription, result.description)
        assertEquals(testPrice, result.price)
        
        verify(exactly = 1) { productRepository.findById(testId) }
        verify(exactly = 1) { productRepository.update(any()) }
    }
    
    @Test
    @DisplayName("상품 삭제 성공")
    fun deleteProductSuccess() {
        // given
        val product = Product.create(testId, testName, testDescription, testPrice)
        
        every { productRepository.findById(testId) } returns product
        every { productRepository.delete(testId) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productService.deleteProduct(testId)
        }
        
        verify(exactly = 1) { productRepository.findById(testId) }
        verify(exactly = 1) { productRepository.delete(testId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외 발생")
    fun deleteNonExistentProduct() {
        // given
        every { productRepository.findById(testId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.deleteProduct(testId)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testId) }
        verify(exactly = 0) { productRepository.delete(any()) }
    }
} 