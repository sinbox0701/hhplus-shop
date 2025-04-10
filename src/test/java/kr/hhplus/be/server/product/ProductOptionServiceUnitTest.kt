package kr.hhplus.be.server.product

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import kr.hhplus.be.server.domain.product.repository.ProductRepository
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
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
        val product = Product.create( "테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product,
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productRepository.findById(product.id!!) } returns product
        every { productOptionRepository.save(any()) } returns productOption
        
        // when
        val createdOption = productOptionService.create(
            ProductOptionCommand.CreateProductOptionCommand(
                product.id!!,
                testOptionName, 
                testAvailableQuantity, 
                testAdditionalPrice
            )
        )
        
        // then
        assertEquals(testOptionName, createdOption.name)
        assertEquals(testAvailableQuantity, createdOption.availableQuantity)
        assertEquals(testAdditionalPrice, createdOption.additionalPrice)
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 1) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품에 옵션 생성 시 예외 발생")
    fun createProductOptionWithNonExistentProduct() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.create(
                ProductOptionCommand.CreateProductOptionCommand(
                    product.id!!, 
                    testOptionName, 
                    testAvailableQuantity, 
                    testAdditionalPrice
            )
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 0) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("여러 옵션 한번에 생성 성공")
    fun createAllOptionsSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val options = listOf(
            ProductOptionCommand.CreateProductOptionCommand(
                product.id!!, 
                "옵션1", 
                100, 
                1000.0
            ),
            ProductOptionCommand.CreateProductOptionCommand(
                product.id!!, "옵션2", 200, 2000.0)
        )
        
        val option1 = ProductOption.create(product, "옵션1", 100, 1000.0)
        val option2 = ProductOption.create(product, "옵션2", 200, 2000.0)
        
        every { productRepository.findById(product.id!!) } returns product
        every { productOptionRepository.save(any()) } returnsMany listOf(option1, option2)
        
        // when
        val createdOptions = productOptionService.createAll(options)
        
        // then
        assertEquals(2, createdOptions.size)
        assertEquals("옵션1", createdOptions[0].name)
        assertEquals("옵션2", createdOptions[1].name)
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 2) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품에 여러 옵션 생성 시 예외 발생")
    fun createAllOptionsWithNonExistentProduct() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val options = listOf(
            ProductOptionCommand.CreateProductOptionCommand(
                product.id!!, 
                "옵션1", 
                100, 
                1000.0
            ),
            ProductOptionCommand.CreateProductOptionCommand(
                product.id!!, 
                "옵션2", 
                200, 
                2000.0
            )
        )
        
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.createAll(options)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 0) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("ID로 옵션 조회 성공")
    fun getProductOptionByIdSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findById(productOption.id!!) } returns productOption
        
        // when
        val foundOption = productOptionService.get(productOption.id!!)
        
        // then
        assertEquals(productOption.id!!, foundOption.id)
        assertEquals(product.id!!, foundOption.product.id)
        assertEquals(testOptionName, foundOption.name)
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 옵션 조회 시 예외 발생")
    fun getProductOptionByIdNotFound() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        every { productOptionRepository.findById(productOption.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.get(productOption.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
    }
    
    @Test
    @DisplayName("상품 ID와 옵션 ID로 조회 성공")
    fun getProductOptionByProductIdAndIdSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findByProductIdAndId(product.id!!, productOption.id!!) } returns productOption
        
        // when
        val foundOption = productOptionService.getByProductIdAndId(product.id!!, productOption.id!!)
        
        // then
        assertEquals(productOption.id!!, foundOption.id)
        assertEquals(product.id!!, foundOption.product.id)
        assertEquals(testOptionName, foundOption.name)
        
        verify(exactly = 1) { productOptionRepository.findByProductIdAndId(product.id!!, productOption.id!!) }
    }
    
    @Test
    @DisplayName("상품 ID와 옵션 ID로 조회 실패 시 예외 발생")
    fun getProductOptionByProductIdAndIdNotFound() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        every { productOptionRepository.findByProductIdAndId(product.id!!, productOption.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getByProductIdAndId(product.id!!, productOption.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findByProductIdAndId(product.id!!, productOption.id!!) }
    }
    
    @Test
    @DisplayName("상품 ID로 옵션 목록 조회 성공")
    fun getProductOptionsByProductIdSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val option1 = ProductOption.create(product, "옵션1", 100, 1000.0)
        val option2 = ProductOption.create(product, "옵션2", 200, 2000.0)
        val options = listOf(option1, option2)

        every { productRepository.findById(product.id!!) } returns product
        every { productOptionRepository.findByProductId(product.id!!) } returns options
        
        // when
        val foundOptions = productOptionService.getAllByProductId(product.id!!)
        
        // then
        assertEquals(2, foundOptions.size)
        assertEquals("옵션1", foundOptions[0].name)
        assertEquals("옵션2", foundOptions[1].name)
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 1) { productOptionRepository.findByProductId(product.id!!) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 ID로 옵션 목록 조회 시 예외 발생")
    fun getProductOptionsByNonExistentProductId() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getAllByProductId(product.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 0) { productOptionRepository.findByProductId(product.id!!) }
    }
    
    @Test
    @DisplayName("옵션 업데이트 성공")
    fun updateProductOptionSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        val newName = "새 옵션명"
        val newAdditionalPrice = 2000.0
        
        val updatedOption = productOption.update(newName, newAdditionalPrice)
        
        every { productOptionRepository.findById(productOption.id!!) } returns productOption
        every { productOptionRepository.update(any()) } returns updatedOption
        
        // when
        val result = productOptionService.update(
            ProductOptionCommand.UpdateProductOptionCommand(
                id=productOption.id!!,
                name=newName,
                additionalPrice=newAdditionalPrice
            )
        )
        
        // then
        assertEquals(newName, result.name)
        assertEquals(newAdditionalPrice, result.additionalPrice)
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션 업데이트 시 예외 발생")
    fun updateNonExistentProductOption() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        val newName = "새 옵션명"
        val newAdditionalPrice = 2000.0
        
        every { productOptionRepository.findById(productOption.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.update(
                ProductOptionCommand.UpdateProductOptionCommand(
                    id=productOption.id!!,
                    name=newName,
                    additionalPrice=newAdditionalPrice
                )
            )
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("수량 추가 성공")
    fun addQuantitySuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        val quantityToAdd = 50
        val expectedQuantity = testAvailableQuantity + quantityToAdd
        
        val updatedOption = productOption.add(quantityToAdd)
        
        every { productOptionRepository.findById(productOption.id!!) } returns productOption
        every { productOptionRepository.update(any()) } returns updatedOption
        
        // when
        val result = productOptionService.updateQuantity(
            ProductOptionCommand.UpdateQuantityCommand(
                id=productOption.id!!,
                quantity=quantityToAdd
            )
        )
        
        // then
        assertEquals(expectedQuantity, result.availableQuantity)
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션의 수량 추가 시 예외 발생")
    fun addQuantityToNonExistentOption() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        val quantityToAdd = 50
        
        every { productOptionRepository.findById(productOption.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.updateQuantity(
                ProductOptionCommand.UpdateQuantityCommand(
                    id=productOption.id!!,
                    quantity=quantityToAdd
                )
            )
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("수량 차감 성공")
    fun subtractQuantitySuccess() {
        // given    
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        val quantityToSubtract = 50
        val expectedQuantity = testAvailableQuantity - quantityToSubtract
        
        val updatedOption = productOption.subtract(quantityToSubtract)
        
        every { productOptionRepository.findById(productOption.id!!) } returns productOption
        every { productOptionRepository.update(any()) } returns updatedOption
        
        // when
        val result = productOptionService.updateQuantity(
            ProductOptionCommand.UpdateQuantityCommand(
                id=productOption.id!!,
                quantity=quantityToSubtract
            )
        )
        
        // then
        assertEquals(expectedQuantity, result.availableQuantity)
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션의 수량 차감 시 예외 발생")
    fun subtractQuantityFromNonExistentOption() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        val quantityToSubtract = 50
        
        every { productOptionRepository.findById(productOption.id!!) } returns null
        
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.updateQuantity(
                ProductOptionCommand.UpdateQuantityCommand(
                    id=productOption.id!!,
                    quantity=quantityToSubtract
                )
            )
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("옵션 삭제 성공")
    fun deleteProductOptionSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findById(productOption.id!!) } returns productOption
        every { productOptionRepository.delete(productOption.id!!) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productOptionService.delete(productOption.id!!)
        }
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 1) { productOptionRepository.delete(productOption.id!!) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션 삭제 시 예외 발생")
    fun deleteNonExistentProductOption() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val productOption = ProductOption.create(
            product, 
            testOptionName, 
            testAvailableQuantity, 
            testAdditionalPrice
        )
        
        every { productOptionRepository.findById(productOption.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.delete(productOption.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(productOption.id!!) }
        verify(exactly = 0) { productOptionRepository.delete(productOption.id!!) }
    }
    
    @Test
    @DisplayName("상품 ID로 모든 옵션 삭제 성공")
    fun deleteAllOptionsSuccess() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        val option1 = ProductOption.create(product, "옵션1", 100, 1000.0)
        val option2 = ProductOption.create(product, "옵션2", 200, 2000.0)
        val options = listOf(option1, option2)
        
        every { productRepository.findById(product.id!!) } returns product
        every { productOptionRepository.findByProductId(product.id!!) } returns options
        every { productOptionRepository.delete(any()) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productOptionService.deleteAll(product.id!!)
        }
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 1) { productOptionRepository.findByProductId(product.id!!) }
        verify(exactly = 2) { productOptionRepository.delete(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품 ID로 모든 옵션 삭제 시 예외 발생")
    fun deleteAllOptionsFromNonExistentProduct() {
        // given
        val product = Product.create("테스트 상품", "테스트 상품 설명", 10000.0)
        every { productRepository.findById(product.id!!) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.deleteAll(product.id!!)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 1) { productRepository.findById(product.id!!) }
        verify(exactly = 0) { productOptionRepository.findByProductId(product.id!!) }
        verify(exactly = 0) { productOptionRepository.delete(any()) }
    }
} 