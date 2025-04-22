package kr.hhplus.be.server.product

import io.mockk.*
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.repository.ProductOptionRepository
import kr.hhplus.be.server.domain.product.service.ProductOptionCommand
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(MockKExtension::class)
class ProductOptionServiceUnitTest {

    private lateinit var productOptionRepository: ProductOptionRepository
    private lateinit var productOptionService: ProductOptionService
    
    // 테스트 상수 정의
    companion object {
        private const val PRODUCT_ID = 1L
        private const val PRODUCT_NAME = "테스트 상품"
        private const val PRODUCT_DESCRIPTION = "테스트 상품 설명"
        private const val PRODUCT_PRICE = 10000.0
        
        private const val OPTION_NAME = "옵션명"
        private const val AVAILABLE_QUANTITY = 100
        private const val ADDITIONAL_PRICE = 1000.0
        
        private const val NEW_OPTION_NAME = "새 옵션명"
        private const val NEW_ADDITIONAL_PRICE = 2000.0
        
        private const val QUANTITY_CHANGE = 50
    }

    @BeforeEach
    fun setup() {
        productOptionRepository = mockk()
        productOptionService = ProductOptionService(productOptionRepository)
    }

    @Test
    @DisplayName("유효한 데이터로 상품 옵션 생성 성공")
    fun createProductOptionSuccess() {
        // given
        val productOption = mockk<ProductOption> {
            every { name } returns OPTION_NAME
            every { availableQuantity } returns AVAILABLE_QUANTITY
            every { additionalPrice } returns ADDITIONAL_PRICE
            every { productId } returns PRODUCT_ID
        }
        
        every { productOptionRepository.save(any()) } returns productOption
        
        val command = ProductOptionCommand.CreateProductOptionCommand(
            PRODUCT_ID,
            OPTION_NAME, 
            AVAILABLE_QUANTITY, 
            ADDITIONAL_PRICE
        )
        
        // when
        val createdOption = productOptionService.create(command)
        
        // then
        assertEquals(OPTION_NAME, createdOption.name)
        assertEquals(AVAILABLE_QUANTITY, createdOption.availableQuantity)
        assertEquals(ADDITIONAL_PRICE, createdOption.additionalPrice)
        
        verify(exactly = 1) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("존재하지 않는 상품에 옵션 생성 시 예외 발생")
    fun createProductOptionWithNonExistentProduct() {
        // given  
        val command = ProductOptionCommand.CreateProductOptionCommand(
            PRODUCT_ID, 
            OPTION_NAME, 
            AVAILABLE_QUANTITY, 
            ADDITIONAL_PRICE
        )
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.create(command)
        }
        
        assertTrue(exception.message!!.contains("Product not found"))
        
        verify(exactly = 0) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("여러 옵션 한번에 생성 성공")
    fun createAllOptionsSuccess() {
        // given
        val option1 = mockk<ProductOption> {
            every { name } returns "옵션1"
            every { availableQuantity } returns 100
            every { additionalPrice } returns 1000.0
            every { productId } returns PRODUCT_ID
        }
        val option2 = mockk<ProductOption> {
            every { name } returns "옵션2"
            every { availableQuantity } returns 200
            every { additionalPrice } returns 2000.0
            every { productId } returns PRODUCT_ID
        }
        
        every { productOptionRepository.save(any()) } returnsMany listOf(option1, option2)
        
        val commands = listOf(
            ProductOptionCommand.CreateProductOptionCommand(PRODUCT_ID, "옵션1", 100, 1000.0),
            ProductOptionCommand.CreateProductOptionCommand(PRODUCT_ID, "옵션2", 200, 2000.0)
        )
        
        // when
        val createdOptions = productOptionService.createAll(commands)
        
        // then
        assertEquals(2, createdOptions.size)
        assertEquals("옵션1", createdOptions[0].name)
        assertEquals("옵션2", createdOptions[1].name)
        
        verify(exactly = 2) { productOptionRepository.save(any()) }
    }
    
    @Test
    @DisplayName("ID로 옵션 조회 성공")
    fun getProductOptionByIdSuccess() {
        // given
        val productOption = mockk<ProductOption> {
            every { id } returns 2L
            every { name } returns OPTION_NAME
            every { productId } returns PRODUCT_ID
        }
        
        every { productOptionRepository.findById(2L) } returns productOption
        
        // when
        val foundOption = productOptionService.get(2L)
        
        // then
        assertEquals(2L, foundOption.id)
        assertEquals(OPTION_NAME, foundOption.name)
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 옵션 조회 시 예외 발생")
    fun getProductOptionByIdNotFound() {
        // given
        every { productOptionRepository.findById(1L) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.get(1L)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(1L) }
    }
    
    @Test
    @DisplayName("상품 ID와 옵션 ID로 조회 실패 시 예외 발생")
    fun getProductOptionByProductIdAndIdNotFound() {
        // given
        every { productOptionRepository.findByProductIdAndId(1L, 2L) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.getByProductIdAndId(1L, 2L)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findByProductIdAndId(1L, 2L) }
    }
    
    @Test
    @DisplayName("상품 ID로 옵션 목록 조회 성공")
    fun getProductOptionsByProductIdSuccess() {
        // given
        val option1 = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 2L
            every { name } returns "옵션1"
        }
        val option2 = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 3L
            every { name } returns "옵션2"
        }
        val options = listOf(option1, option2)

        every { productOptionRepository.findByProductId(PRODUCT_ID) } returns options
        
        // when
        val foundOptions = productOptionService.getAllByProductId(PRODUCT_ID)
        
        // then
        assertEquals(2, foundOptions.size)
        assertEquals("옵션1", foundOptions[0].name)
        assertEquals("옵션2", foundOptions[1].name)

        verify(exactly = 1) { productOptionRepository.findByProductId(PRODUCT_ID) }
    }
    
    @Test
    @DisplayName("옵션 업데이트 성공")
    fun updateProductOptionSuccess() {
        // given
        val productOption = mockk<ProductOption> {
            every { id } returns 2L
            every { productId } returns PRODUCT_ID
            every { update(any(), any()) } returns mockk {
                every { name } returns NEW_OPTION_NAME
                every { additionalPrice } returns NEW_ADDITIONAL_PRICE
            }
        }
        
        every { productOptionRepository.findById(2L) } returns productOption
        every { productOptionRepository.update(any()) } answers { 
            firstArg<ProductOption>().run {
                mockk {
                    every { name } returns NEW_OPTION_NAME
                    every { additionalPrice } returns NEW_ADDITIONAL_PRICE
                }
            }
        }
        
        val command = ProductOptionCommand.UpdateProductOptionCommand(
            id = 2L,
            name = NEW_OPTION_NAME,
            additionalPrice = NEW_ADDITIONAL_PRICE
        )
        
        // when
        val result = productOptionService.update(command)
        
        // then
        assertEquals(NEW_OPTION_NAME, result.name)
        assertEquals(NEW_ADDITIONAL_PRICE, result.additionalPrice)
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
        verify(exactly = 1) { productOption.update(NEW_OPTION_NAME, NEW_ADDITIONAL_PRICE) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션 업데이트 시 예외 발생")
    fun updateNonExistentProductOption() {
        // given
        every { productOptionRepository.findById(1L) } returns null
        
        val command = ProductOptionCommand.UpdateProductOptionCommand(
            id = 1L,
            name = NEW_OPTION_NAME,
            additionalPrice = NEW_ADDITIONAL_PRICE
        )
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.update(command)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(1L) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("수량 추가 성공")
    fun addQuantitySuccess() {
        // given
        val productOption = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 2L
            every { add(any()) } returns mockk {
                every { availableQuantity } returns AVAILABLE_QUANTITY + QUANTITY_CHANGE
            }
        }
        
        every { productOptionRepository.findById(2L) } returns productOption
        every { productOptionRepository.update(any()) } answers { 
            firstArg<ProductOption>().run {
                mockk {
                    every { availableQuantity } returns AVAILABLE_QUANTITY + QUANTITY_CHANGE
                }
            }
        }
        
        val command = ProductOptionCommand.UpdateQuantityCommand(
            id = 2L,
            quantity = QUANTITY_CHANGE
        )
        
        // when
        val result = productOptionService.updateQuantity(command)
        
        // then
        assertEquals(AVAILABLE_QUANTITY + QUANTITY_CHANGE, result.availableQuantity)
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
        verify(exactly = 1) { productOption.add(QUANTITY_CHANGE) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션의 수량 추가 시 예외 발생")
    fun addQuantityToNonExistentOption() {
        // given
        every { productOptionRepository.findById(2L) } returns null
        
        val command = ProductOptionCommand.UpdateQuantityCommand(
            id = 2L,
            quantity = QUANTITY_CHANGE
        )
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.updateQuantity(command)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("수량 차감 성공")
    fun subtractQuantitySuccess() {
        // given    
        val productOption = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 2L
            every { subtract(any()) } returns mockk {
                every { availableQuantity } returns AVAILABLE_QUANTITY - QUANTITY_CHANGE
            }
        }
        
        every { productOptionRepository.findById(2L) } returns productOption
        every { productOptionRepository.update(any()) } answers { 
            firstArg<ProductOption>().run {
                mockk {
                    every { availableQuantity } returns AVAILABLE_QUANTITY - QUANTITY_CHANGE
                }
            }
        }
        
        val command = ProductOptionCommand.UpdateQuantityCommand(
            id = 2L,
            quantity = QUANTITY_CHANGE
        )
        
        // when
        val result = productOptionService.subtractQuantity(command)
        
        // then
        assertEquals(AVAILABLE_QUANTITY - QUANTITY_CHANGE, result.availableQuantity)
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 1) { productOptionRepository.update(any()) }
        verify(exactly = 1) { productOption.subtract(QUANTITY_CHANGE) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션의 수량 차감 시 예외 발생")
    fun subtractQuantityFromNonExistentOption() {
        // given
        every { productOptionRepository.findById(2L) } returns null
        
        val command = ProductOptionCommand.UpdateQuantityCommand(
            id = 2L,
            quantity = QUANTITY_CHANGE
        )
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.subtractQuantity(command)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 0) { productOptionRepository.update(any()) }
    }
    
    @Test
    @DisplayName("옵션 삭제 성공")
    fun deleteProductOptionSuccess() {
        // given
        val productOption = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 2L
        }
        
        every { productOptionRepository.findById(2L) } returns productOption
        every { productOptionRepository.delete(2L) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productOptionService.delete(2L)
        }
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 1) { productOptionRepository.delete(2L) }
    }
    
    @Test
    @DisplayName("존재하지 않는 옵션 삭제 시 예외 발생")
    fun deleteNonExistentProductOption() {
        // given
        every { productOptionRepository.findById(2L) } returns null
        
        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productOptionService.delete(2L)
        }
        
        assertTrue(exception.message!!.contains("Product option not found"))
        
        verify(exactly = 1) { productOptionRepository.findById(2L) }
        verify(exactly = 0) { productOptionRepository.delete(2L) }
    }
    
    @Test
    @DisplayName("상품 ID로 모든 옵션 삭제 성공")
    fun deleteAllOptionsSuccess() {
        // given
        val option1 = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 2L
        }
        val option2 = mockk<ProductOption> {
            every { productId } returns PRODUCT_ID
            every { id } returns 3L
        }
        val options = listOf(option1, option2)
        
        every { productOptionRepository.findByProductId(PRODUCT_ID) } returns options
        every { productOptionRepository.delete(any()) } returns Unit
        
        // when & then
        assertDoesNotThrow {
            productOptionService.deleteAll(1L)
        }
        
        verify(exactly = 1) { productOptionRepository.findByProductId(PRODUCT_ID) }
        verify(exactly = 2) { productOptionRepository.delete(any()) }
    }
    
}