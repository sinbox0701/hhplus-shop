package kr.hhplus.be.server.product

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import kr.hhplus.be.server.domain.product.service.ProductOptionRequest
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductOptionServiceUnitTest {

    @MockK
    private lateinit var productOptionRepository: ProductOptionRepository

    @MockK
    private lateinit var productRepository: ProductRepository

    @InjectMockKs
    private lateinit var productOptionService: ProductOptionService

    private val testProductId = 10L
    private val testOptionId = 1L
    private val testOptionName = "옵션명"
    private val testAvailableQuantity = 100
    private val testAdditionalPrice = 1000.0

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    @DisplayName("유효한 데이터로 상품 옵션 생성 성공")
    fun createProductOptionSuccess() {
        // given
        val product = Product.create(testProductId, "테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productRepository.findById(testProductId) } returns product
        every { productOptionRepository.save(any()) } returns productOption
        
        // when
        val createdOption = productOptionService.createProductOption(
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        // then
        assertEquals(testOptionName, createdOption.name)
        assertEquals(testAvailableQuantity, createdOption.availableQuantity)
        assertEquals(testAdditionalPrice, createdOption.additionalPrice)
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 1) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품에 옵션 생성 시 예외 발생")
    fun createProductOptionWithNonExistentProduct() {
        // given
        every { productRepository.findById(testProductId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.createProductOption(
                testProductId, 
                testOptionName, 
                testAvailableQuantity, 
                testAdditionalPrice
            )
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 0) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("여러 옵션 한번에 생성 성공")
    fun createAllOptionsSuccess() {
        // given
        val product = Product.create(testProductId, "테스트 상품", "테스트 상품 설명", 10000.0)
        val options = listOf(
            ProductOptionRequest("옵션1", 100, 1000.0),
            ProductOptionRequest("옵션2", 200, 2000.0)
        )
        
        val option1 = ProductOption.create(1L, testProductId, "옵션1", 100, 1000.0)
        val option2 = ProductOption.create(2L, testProductId, "옵션2", 200, 2000.0)
        
        every { productRepository.findById(testProductId) } returns product
        every { productOptionRepository.save(any()) } returnsMany listOf(option1, option2)
        
        // when
        val createdOptions = productOptionService.createAll(testProductId, options)
        
        // then
        assertEquals(2, createdOptions.size)
        assertEquals("옵션1", createdOptions[0].name)
        assertEquals("옵션2", createdOptions[1].name)
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 2) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품에 여러 옵션 생성 시 예외 발생")
    fun createAllOptionsWithNonExistentProduct() {
        // given
        val options = listOf(
            ProductOptionRequest("옵션1", 100, 1000.0),
            ProductOptionRequest("옵션2", 200, 2000.0)
        )
        
        every { productRepository.findById(testProductId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.createAll(testProductId, options)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 0) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("ID로 옵션 조회 성공")
    fun getProductOptionByIdSuccess() {
        // given
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findById(testOptionId) } returns productOption
        
        // when
        val foundOption = productOptionService.getProductOption(testOptionId)
        
        // then
        assertEquals(testOptionId, foundOption.id)
        assertEquals(testProductId, foundOption.productId)
        assertEquals(testOptionName, foundOption.name)
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 옵션 조회 시 예외 발생")
    fun getProductOptionByIdNotFound() {
        // given
        every { productOptionRepository.findById(testOptionId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getProductOption(testOptionId)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
    }
    
    @Test
    @DisplayName("상품 ID와 옵션 ID로 조회 성공")
    fun getProductOptionByProductIdAndIdSuccess() {
        // given
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findByProductIdAndId(testProductId, testOptionId) } returns productOption
        
        // when
        val foundOption = productOptionService.getProductOptionByProductIdAndId(testProductId, testOptionId)
        
        // then
        assertEquals(testOptionId, foundOption.id)
        assertEquals(testProductId, foundOption.productId)
        assertEquals(testOptionName, foundOption.name)
        
        verify(exactly = 1) { productOptionRepository.findByProductIdAndId(testProductId, testOptionId) }
    }
    
    @Test
    @DisplayName("상품 ID와 옵션 ID로 조회 실패 시 예외 발생")
    fun getProductOptionByProductIdAndIdNotFound() {
        // given
        every { productOptionRepository.findByProductIdAndId(testProductId, testOptionId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getProductOptionByProductIdAndId(testProductId, testOptionId)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findByProductIdAndId(testProductId, testOptionId) }
    }
    
    @Test
    @DisplayName("상품 ID로 옵션 목록 조회 성공")
    fun getProductOptionsByProductIdSuccess() {
        // given
        val product = Product.create(testProductId, "테스트 상품", "테스트 상품 설명", 10000.0)
        val option1 = ProductOption.create(1L, testProductId, "옵션1", 100, 1000.0)
        val option2 = ProductOption.create(2L, testProductId, "옵션2", 200, 2000.0)
        val options = listOf(option1, option2)
        
        every { productRepository.findById(testProductId) } returns product
        every { productOptionRepository.findByProductId(testProductId) } returns options
        
        // when
        val foundOptions = productOptionService.getProductOptionsByProductId(testProductId)
        
        // then
        assertEquals(2, foundOptions.size)
        assertEquals("옵션1", foundOptions[0].name)
        assertEquals("옵션2", foundOptions[1].name)
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 1) { productOptionRepository.findByProductId(testProductId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 ID로 옵션 목록 조회 시 예외 발생")
    fun getProductOptionsByNonExistentProductId() {
        // given
        every { productRepository.findById(testProductId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getProductOptionsByProductId(testProductId)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 0) { productOptionRepository.findByProductId(testProductId) }
    }
    
    @Test
    @DisplayName("옵션 업데이트 성공")
    fun updateProductOptionSuccess() {
        // given
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        val newName = "새 옵션명"
        val newAdditionalPrice = 2000.0
        
        val updatedOption = productOption.update(newName, newAdditionalPrice)
        
        every { productOptionRepository.findById(testOptionId) } returns productOption
        every { productOptionRepository.update(any()) } returns updatedOption
        
        // when
        val result = productOptionService.updateProductOption(testOptionId, newName, newAdditionalPrice)
        
        // then
        assertEquals(newName, result.name)
        assertEquals(newAdditionalPrice, result.additionalPrice)
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션 업데이트 시 예외 발생")
    fun updateNonExistentProductOption() {
        // given
        val newName = "새 옵션명"
        val newAdditionalPrice = 2000.0
        
        every { productOptionRepository.findById(testOptionId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.updateProductOption(testOptionId, newName, newAdditionalPrice)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("수량 추가 성공")
    fun addQuantitySuccess() {
        // given
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        val quantityToAdd = 50
        val expectedQuantity = testAvailableQuantity + quantityToAdd
        
        val updatedOption = productOption.add(quantityToAdd)
        
        every { productOptionRepository.findById(testOptionId) } returns productOption
        every { productOptionRepository.update(any()) } returns updatedOption
        
        // when
        val result = productOptionService.addQuantity(testOptionId, quantityToAdd)
        
        // then
        assertEquals(expectedQuantity, result.availableQuantity)
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션의 수량 추가 시 예외 발생")
    fun addQuantityToNonExistentOption() {
        // given
        val quantityToAdd = 50
        
        every { productOptionRepository.findById(testOptionId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.addQuantity(testOptionId, quantityToAdd)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("수량 차감 성공")
    fun subtractQuantitySuccess() {
        // given
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        val quantityToSubtract = 50
        val expectedQuantity = testAvailableQuantity - quantityToSubtract
        
        val updatedOption = productOption.subtract(quantityToSubtract)
        
        every { productOptionRepository.findById(testOptionId) } returns productOption
        every { productOptionRepository.update(any()) } returns updatedOption
        
        // when
        val result = productOptionService.subtractQuantity(testOptionId, quantityToSubtract)
        
        // then
        assertEquals(expectedQuantity, result.availableQuantity)
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션의 수량 차감 시 예외 발생")
    fun subtractQuantityFromNonExistentOption() {
        // given
        val quantityToSubtract = 50
        
        every { productOptionRepository.findById(testOptionId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.subtractQuantity(testOptionId, quantityToSubtract)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("옵션 삭제 성공")
    fun deleteProductOptionSuccess() {
        // given
        val productOption = ProductOption.create(
            testOptionId, 
            testProductId, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findById(testOptionId) } returns productOption
        every { productOptionRepository.delete(testOptionId) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productOptionService.deleteProductOption(testOptionId)
        }
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 1) { productOptionRepository.delete(testOptionId) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션 삭제 시 예외 발생")
    fun deleteNonExistentProductOption() {
        // given
        every { productOptionRepository.findById(testOptionId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.deleteProductOption(testOptionId)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(testOptionId) }
        verify(exactly = 0) { productOptionRepository.delete(any()) }
    }
    
    @Test
    @DisplayName("상품 ID로 모든 옵션 삭제 성공")
    fun deleteAllOptionsSuccess() {
        // given
        val product = Product.create(testProductId, "테스트 상품", "테스트 상품 설명", 10000.0)
        val option1 = ProductOption.create(1L, testProductId, "옵션1", 100, 1000.0)
        val option2 = ProductOption.create(2L, testProductId, "옵션2", 200, 2000.0)
        val options = listOf(option1, option2)
        
        every { productRepository.findById(testProductId) } returns product
        every { productOptionRepository.findByProductId(testProductId) } returns options
        every { productOptionRepository.delete(any()) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productOptionService.deleteAll(testProductId)
        }
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 1) { productOptionRepository.findByProductId(testProductId) }
        verify(exactly = 2) { productOptionRepository.delete(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 ID로 모든 옵션 삭제 시 예외 발생")
    fun deleteAllOptionsFromNonExistentProduct() {
        // given
        every { productRepository.findById(testProductId) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.deleteAll(testProductId)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(testProductId) }
        verify(exactly = 0) { productOptionRepository.findByProductId(any()) }
        verify(exactly = 0) { productOptionRepository.delete(any()) }
    }
} 