package kr.hhplus.be.server.product

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import kr.hhplus.be.server.domain.product.service.ProductCommand
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
        val command = ProductCommand.CreateProductCommand(testName, testDescription, testPrice)
        val product = Product.create(testName, testDescription, testPrice)
        
        every { productRepository.save(any()) } returns product
        
        // when
        val createdProduct = productService.create(command)
        
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
        val product = Product.create(testName, testDescription, testPrice)
        
        every { productRepository.findById(product.id!!) } returns product
        
        // when
        val foundProduct = productService.get(product.id!!)
        
        // then
        assertEquals(testName, foundProduct.name)
        assertEquals(testDescription, foundProduct.description)
        assertEquals(testPrice, foundProduct.price)
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 상품 조회 시 예외 발생")
    fun getProductByIdNotFound() {
        // given
        val product = Product.create(testName, testDescription, testPrice)
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.get(product.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
    }
    
    @Test
    @DisplayName("모든 상품 조회 성공")
    fun getAllProductsSuccess() {
        // given
        val product1 = Product.create(testName, testDescription, testPrice)
        val product2 = Product.create("상품2", "설명2", 20000.0)
        val products = listOf(product1, product2)
        
        every { productRepository.findAll() } returns products
        
        // when
        val result = productService.getAll()
        
        // then
        assertEquals(2, result.size)
        assertEquals(testName, result[0].name)
        assertEquals("상품2", result[1].name)
        
        verify(exactly = 1) { productRepository.findAll() }
    }
    
    @Test
    @DisplayName("상품 정보 업데이트 성공")
    fun updateProductSuccess() {
        // given
        val product = Product.create(testName, testDescription, testPrice)
        val newName = "업데이트된 상품"
        val newDescription = "업데이트된 상품 설명"
        val newPrice = 20000.0
        
        val command = ProductCommand.UpdateProductCommand(product.id!!, newName, newDescription, newPrice)
        val updatedProduct = product.update(newName, newDescription, newPrice)
        
        every { productRepository.findById(product.id!!) } returns product
        every { productRepository.save(any()) } returns updatedProduct
        
        // when
        val result = productService.update(command)
        
        // then
        assertEquals(newName, result.name)
        assertEquals(newDescription, result.description)
        assertEquals(newPrice, result.price)
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 1) { productRepository.save(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 업데이트 시 예외 발생")
    fun updateNonExistentProduct() {
        // given
        val product = Product.create(testName, testDescription, testPrice)
        val newName = "업데이트된 상품"
        val newDescription = "업데이트된 상품 설명"
        val newPrice = 20000.0
        
        val command = ProductCommand.UpdateProductCommand(product.id!!, newName, newDescription, newPrice)
        
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.update(command)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 0) { productRepository.save(any()) }
    }
    
    @Test
    @DisplayName("일부 필드만 업데이트 성공")
    fun updatePartialFieldsSuccess() {
        // given
        val product = Product.create(testName, testDescription, testPrice)
        val newName = "업데이트된 상품"
        
        val command = ProductCommand.UpdateProductCommand(product.id!!, newName)
        val updatedProduct = product.update(newName, null, null)
        
        every { productRepository.findById(product.id!!) } returns product
        every { productRepository.save(any()) } returns updatedProduct
        
        // when
        val result = productService.update(command)
        
        // then
        assertEquals(newName, result.name)
        assertEquals(testDescription, result.description)
        assertEquals(testPrice, result.price)
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 1) { productRepository.save(any()) }
    }
    
    @Test
    @DisplayName("상품 삭제 성공")
    fun deleteProductSuccess() {
        // given
        val product = Product.create(testName, testDescription, testPrice)
        
        every { productRepository.findById(product.id!!) } returns product
        every { productRepository.delete(product.id!!) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productService.delete(product.id!!)
        }
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 1) { productRepository.delete(product.id!!) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외 발생")
    fun deleteNonExistentProduct() {
        // given
        val product = Product.create(testName, testDescription, testPrice)
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productService.delete(product.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 0) { productRepository.delete(any()) }
    }
} 