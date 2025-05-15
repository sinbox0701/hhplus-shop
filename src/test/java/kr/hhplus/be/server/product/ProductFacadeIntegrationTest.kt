package kr.hhplus.be.server.product

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.server.application.product.ProductCriteria
import kr.hhplus.be.server.application.product.ProductFacade
import kr.hhplus.be.server.application.product.ProductResult
import kr.hhplus.be.server.domain.order.service.OrderItemService
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import kr.hhplus.be.server.domain.product.service.ProductOptionService
import kr.hhplus.be.server.domain.product.service.ProductSalesService
import kr.hhplus.be.server.domain.product.service.ProductService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.cache.CacheManager
import java.time.LocalDate
import java.time.LocalDateTime

class ProductFacadeIntegrationTest {

    private lateinit var productService: ProductService
    private lateinit var productOptionService: ProductOptionService
    private lateinit var orderItemService: OrderItemService
    private lateinit var productSalesService: ProductSalesService
    private lateinit var productFacade: ProductFacade

    companion object {
        private const val TEST_PRODUCT_ID = 1L
        private const val TEST_OPTION_ID = 2L
        private const val TEST_PRODUCT_NAME = "테스트 상품"
        private const val TEST_PRODUCT_DESCRIPTION = "테스트 상품 설명"
        private const val TEST_PRODUCT_PRICE = 10000.0
        private const val TEST_OPTION_NAME = "테스트 옵션"
        private const val TEST_OPTION_PRICE = 1000.0
        private const val TEST_OPTION_QUANTITY = 100
    }

    @BeforeEach
    fun setup() {
        productService = mockk()
        productOptionService = mockk()
        orderItemService = mockk()
        productSalesService = mockk()
        val cacheManager = mockk<CacheManager>(relaxed = true)
        productFacade = ProductFacade(
            productService = productService,
            productOptionService = productOptionService,
            orderItemService = orderItemService,
            productSalesService = productSalesService,
            cacheManager = cacheManager
        )
    }

    @Test
    @DisplayName("모든 상품과 옵션 조회 성공")
    fun getAllProductsWithOptionsSuccess() {
        // given
        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID
        every { product.name } returns TEST_PRODUCT_NAME
        every { product.description } returns TEST_PRODUCT_DESCRIPTION
        every { product.price } returns TEST_PRODUCT_PRICE

        val option = mockk<ProductOption>()
        every { option.id } returns TEST_OPTION_ID
        every { option.productId } returns TEST_PRODUCT_ID
        every { option.name } returns TEST_OPTION_NAME
        every { option.additionalPrice } returns TEST_OPTION_PRICE
        every { option.availableQuantity } returns TEST_OPTION_QUANTITY

        every { productService.getAll() } returns listOf(product)
        every { productOptionService.getAllByProductIds(listOf(TEST_PRODUCT_ID)) } returns listOf(option)

        // when
        val results = productFacade.getAllProductsWithOptions()

        // then
        assertNotNull(results)
        assertEquals(1, results.size)
        assertEquals(TEST_PRODUCT_ID, results[0].product.id)
        assertEquals(TEST_PRODUCT_NAME, results[0].product.name)
        assertEquals(TEST_PRODUCT_DESCRIPTION, results[0].product.description)
        assertEquals(TEST_PRODUCT_PRICE, results[0].product.price)
        assertEquals(1, results[0].options.size)
        assertEquals(TEST_OPTION_ID, results[0].options[0].id)
        assertEquals(TEST_OPTION_NAME, results[0].options[0].name)
        assertEquals(TEST_OPTION_PRICE, results[0].options[0].additionalPrice)
        assertEquals(TEST_OPTION_QUANTITY, results[0].options[0].availableQuantity)

        verify(exactly = 1) { productService.getAll() }
        verify(exactly = 1) { productOptionService.getAllByProductIds(listOf(TEST_PRODUCT_ID)) }
        verify(exactly = 0) { productOptionService.getAllByProductId(any()) }
    }

    @Test
    @DisplayName("상품과 옵션 생성 성공")
    fun createProductWithOptionsSuccess() {
        // given
        val optionCriteria = ProductCriteria.CreateProductOptionCriteria(
            name = TEST_OPTION_NAME,
            price = TEST_OPTION_PRICE,
            availableQuantity = TEST_OPTION_QUANTITY
        )

        val criteria = ProductCriteria.CreateProductCriteria(
            name = TEST_PRODUCT_NAME,
            description = TEST_PRODUCT_DESCRIPTION,
            price = TEST_PRODUCT_PRICE,
            options = listOf(optionCriteria)
        )

        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID
        every { product.name } returns TEST_PRODUCT_NAME
        every { product.description } returns TEST_PRODUCT_DESCRIPTION
        every { product.price } returns TEST_PRODUCT_PRICE

        val option = mockk<ProductOption>()
        every { option.id } returns TEST_OPTION_ID
        every { option.productId } returns TEST_PRODUCT_ID
        every { option.name } returns TEST_OPTION_NAME
        every { option.additionalPrice } returns TEST_OPTION_PRICE
        every { option.availableQuantity } returns TEST_OPTION_QUANTITY

        every { productService.create(any()) } returns product
        every { productOptionService.createAll(any()) } returns listOf(option)

        // when
        val result = productFacade.createProductWithOptions(criteria)

        // then
        assertNotNull(result)
        assertEquals(TEST_PRODUCT_ID, result.product.id)
        assertEquals(TEST_PRODUCT_NAME, result.product.name)
        assertEquals(TEST_PRODUCT_DESCRIPTION, result.product.description)
        assertEquals(TEST_PRODUCT_PRICE, result.product.price)
        assertEquals(1, result.options.size)
        assertEquals(TEST_OPTION_ID, result.options[0].id)
        assertEquals(TEST_OPTION_NAME, result.options[0].name)
        assertEquals(TEST_OPTION_PRICE, result.options[0].additionalPrice)
        assertEquals(TEST_OPTION_QUANTITY, result.options[0].availableQuantity)

        verify(exactly = 1) { productService.create(any()) }
        verify(exactly = 1) { productOptionService.createAll(any()) }
    }

    @Test
    @DisplayName("상품과 옵션 조회 성공")
    fun getProductWithOptionsSuccess() {
        // given
        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID
        every { product.name } returns TEST_PRODUCT_NAME
        every { product.description } returns TEST_PRODUCT_DESCRIPTION
        every { product.price } returns TEST_PRODUCT_PRICE

        val option = mockk<ProductOption>()
        every { option.id } returns TEST_OPTION_ID
        every { option.productId } returns TEST_PRODUCT_ID
        every { option.name } returns TEST_OPTION_NAME
        every { option.additionalPrice } returns TEST_OPTION_PRICE
        every { option.availableQuantity } returns TEST_OPTION_QUANTITY

        every { productService.get(TEST_PRODUCT_ID) } returns product
        every { productOptionService.getAllByProductId(TEST_PRODUCT_ID) } returns listOf(option)

        // when
        val result = productFacade.getProductWithOptions(TEST_PRODUCT_ID)

        // then
        assertNotNull(result)
        assertEquals(TEST_PRODUCT_ID, result.product.id)
        assertEquals(TEST_PRODUCT_NAME, result.product.name)
        assertEquals(TEST_PRODUCT_DESCRIPTION, result.product.description)
        assertEquals(TEST_PRODUCT_PRICE, result.product.price)
        assertEquals(1, result.options.size)
        assertEquals(TEST_OPTION_ID, result.options[0].id)
        assertEquals(TEST_OPTION_NAME, result.options[0].name)
        assertEquals(TEST_OPTION_PRICE, result.options[0].additionalPrice)
        assertEquals(TEST_OPTION_QUANTITY, result.options[0].availableQuantity)

        verify(exactly = 1) { productService.get(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.getAllByProductId(TEST_PRODUCT_ID) }
    }

    @Test
    @DisplayName("존재하지 않는 상품 조회 시 예외 발생")
    fun getNonExistentProductWithOptionsThrowsException() {
        // given
        val nonExistentProductId = 999L
        val errorMessage = "Product not found with id: $nonExistentProductId"

        every { productService.get(nonExistentProductId) } throws IllegalArgumentException(errorMessage)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productFacade.getProductWithOptions(nonExistentProductId)
        }

        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productService.get(nonExistentProductId) }
        verify(exactly = 0) { productOptionService.getAllByProductId(any()) }
    }

    @Test
    @DisplayName("인기 상품 조회 성공")
    fun getTopSellingProductsSuccess() {
        // given
        val startDate = LocalDate.now().minusDays(3)
        val topProductIds = listOf(1L, 2L, 3L)

        val product1 = mockk<Product>()
        every { product1.id } returns 1L
        every { product1.name } returns "인기 상품 1"
        every { product1.description } returns "인기 상품 설명 1"
        every { product1.price } returns 10000.0

        val product2 = mockk<Product>()
        every { product2.id } returns 2L
        every { product2.name } returns "인기 상품 2"
        every { product2.description } returns "인기 상품 설명 2"
        every { product2.price } returns 20000.0

        val product3 = mockk<Product>()
        every { product3.id } returns 3L
        every { product3.name } returns "인기 상품 3"
        every { product3.description } returns "인기 상품 설명 3"
        every { product3.price } returns 30000.0

        every { productSalesService.getTopSellingProductIds(startDate, 5) } returns topProductIds
        every { productService.getByIds(topProductIds) } returns listOf(product1, product2, product3)

        // when
        val results = productFacade.getTopSellingProducts()

        // then
        assertNotNull(results)
        assertEquals(3, results.size)
        assertEquals(1L, results[0].id)
        assertEquals("인기 상품 1", results[0].name)
        assertEquals(2L, results[1].id)
        assertEquals("인기 상품 2", results[1].name)
        assertEquals(3L, results[2].id)
        assertEquals("인기 상품 3", results[2].name)

        verify(exactly = 1) { productSalesService.getTopSellingProductIds(any(), 5) }
        verify(exactly = 1) { productService.getByIds(topProductIds) }
    }

    @Test
    @DisplayName("판매 데이터가 없을 때 인기 상품 조회 성공")
    fun getTopSellingProductsWithNoSalesDataSuccess() {
        // given
        val startDate = LocalDate.now().minusDays(3)
        val topProductIds = emptyList<Long>()
        
        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID
        every { product.name } returns TEST_PRODUCT_NAME
        every { product.description } returns TEST_PRODUCT_DESCRIPTION
        every { product.price } returns TEST_PRODUCT_PRICE

        every { productSalesService.getTopSellingProductIds(startDate, 5) } returns topProductIds
        every { productService.getAll() } returns listOf(product)

        // when
        val results = productFacade.getTopSellingProducts()

        // then
        assertNotNull(results)
        assertEquals(1, results.size)
        assertEquals(TEST_PRODUCT_ID, results[0].id)
        assertEquals(TEST_PRODUCT_NAME, results[0].name)
        assertEquals(TEST_PRODUCT_DESCRIPTION, results[0].description)
        assertEquals(TEST_PRODUCT_PRICE, results[0].price)

        verify(exactly = 1) { productSalesService.getTopSellingProductIds(any(), 5) }
        verify(exactly = 1) { productService.getAll() }
    }

    @Test
    @DisplayName("상품과 옵션 업데이트 성공")
    fun updateProductWithOptionsSuccess() {
        // given
        val optionToUpdate = ProductCriteria.UpdateProductOptionCriteria(
            id = TEST_OPTION_ID,
            name = "업데이트된 옵션",
            availableQuantity = 200,
            additionalPrice = 2000.0
        )

        val newOption = ProductCriteria.CreateProductOptionCriteria(
            name = "새 옵션",
            price = 1500.0,
            availableQuantity = 150
        )

        val criteria = ProductCriteria.UpdateProductCriteria(
            id = TEST_PRODUCT_ID,
            name = "업데이트된 상품",
            description = "업데이트된 설명",
            price = 12000.0,
            optionsToUpdate = listOf(optionToUpdate),
            optionsToAdd = listOf(newOption)
        )

        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID
        every { product.name } returns "업데이트된 상품"
        every { product.description } returns "업데이트된 설명"
        every { product.price } returns 12000.0

        val updatedOption = mockk<ProductOption>()
        every { updatedOption.id } returns TEST_OPTION_ID
        every { updatedOption.productId } returns TEST_PRODUCT_ID
        every { updatedOption.name } returns "업데이트된 옵션"
        every { updatedOption.additionalPrice } returns 2000.0
        every { updatedOption.availableQuantity } returns 200

        val existingOption = mockk<ProductOption>()
        every { existingOption.id } returns TEST_OPTION_ID
        every { existingOption.productId } returns TEST_PRODUCT_ID

        every { productService.update(any()) } returns product
        every { productOptionService.get(TEST_OPTION_ID) } returns existingOption
        every { productOptionService.updateAll(any()) } returns listOf(updatedOption)
        every { productOptionService.createAll(any()) } returns listOf(mockk())
        every { productService.get(TEST_PRODUCT_ID) } returns product
        every { productOptionService.getAllByProductId(TEST_PRODUCT_ID) } returns listOf(updatedOption)

        // when
        val result = productFacade.updateProductWithOptions(criteria)

        // then
        assertNotNull(result)
        assertEquals(TEST_PRODUCT_ID, result.product.id)
        assertEquals("업데이트된 상품", result.product.name)
        assertEquals("업데이트된 설명", result.product.description)
        assertEquals(12000.0, result.product.price)
        assertEquals(1, result.options.size)
        assertEquals(TEST_OPTION_ID, result.options[0].id)
        assertEquals("업데이트된 옵션", result.options[0].name)
        assertEquals(2000.0, result.options[0].additionalPrice)
        assertEquals(200, result.options[0].availableQuantity)

        verify(exactly = 1) { productService.update(any()) }
        verify(exactly = 1) { productOptionService.get(TEST_OPTION_ID) }
        verify(exactly = 1) { productOptionService.updateAll(any()) }
        verify(exactly = 1) { productOptionService.createAll(any()) }
        verify(exactly = 1) { productService.get(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.getAllByProductId(TEST_PRODUCT_ID) }
    }

    @Test
    @DisplayName("상품에 옵션 추가 성공")
    fun addOptionsToProductSuccess() {
        // given
        val newOption = ProductCriteria.CreateProductOptionCriteria(
            name = "새 옵션",
            price = 1500.0,
            availableQuantity = 150
        )

        val criteria = ProductCriteria.UpdateProductCriteria(
            id = TEST_PRODUCT_ID,
            optionsToAdd = listOf(newOption)
        )

        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID

        val option = mockk<ProductOption>()
        every { option.id } returns 3L
        every { option.productId } returns TEST_PRODUCT_ID
        every { option.name } returns "새 옵션"
        every { option.additionalPrice } returns 1500.0
        every { option.availableQuantity } returns 150

        every { productService.get(TEST_PRODUCT_ID) } returns product
        every { productOptionService.createAll(any()) } returns listOf(option)

        // when
        val results = productFacade.addOptionsToProduct(criteria)

        // then
        assertNotNull(results)
        assertEquals(1, results.size)
        assertEquals(3L, results[0].id)
        assertEquals(TEST_PRODUCT_ID, results[0].productId)
        assertEquals("새 옵션", results[0].name)
        assertEquals(1500.0, results[0].additionalPrice)
        assertEquals(150, results[0].availableQuantity)

        verify(exactly = 1) { productService.get(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.createAll(any()) }
    }

    @Test
    @DisplayName("상품에서 옵션 제거 성공")
    fun removeOptionsFromProductSuccess() {
        // given
        val criteria = ProductCriteria.UpdateProductCriteria(
            id = TEST_PRODUCT_ID,
            optionsToRemove = listOf(TEST_OPTION_ID)
        )

        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID

        val option = mockk<ProductOption>()
        every { option.id } returns TEST_OPTION_ID
        every { option.productId } returns TEST_PRODUCT_ID

        every { productService.get(TEST_PRODUCT_ID) } returns product
        every { productOptionService.get(TEST_OPTION_ID) } returns option
        every { productOptionService.delete(TEST_OPTION_ID) } returns Unit

        // when
        productFacade.removeOptionsFromProduct(criteria)

        // then
        verify(exactly = 1) { productService.get(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.get(TEST_OPTION_ID) }
        verify(exactly = 1) { productOptionService.delete(TEST_OPTION_ID) }
    }

    @Test
    @DisplayName("다른 상품의 옵션 제거 시 예외 발생")
    fun removeOptionsFromDifferentProductThrowsException() {
        // given
        val criteria = ProductCriteria.UpdateProductCriteria(
            id = TEST_PRODUCT_ID,
            optionsToRemove = listOf(TEST_OPTION_ID)
        )

        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID

        val option = mockk<ProductOption>()
        every { option.id } returns TEST_OPTION_ID
        every { option.productId } returns 999L // 다른 상품의 ID

        every { productService.get(TEST_PRODUCT_ID) } returns product
        every { productOptionService.get(TEST_OPTION_ID) } returns option

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productFacade.removeOptionsFromProduct(criteria)
        }

        assertEquals("Option with id $TEST_OPTION_ID does not belong to product with id $TEST_PRODUCT_ID", exception.message)
        verify(exactly = 1) { productService.get(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.get(TEST_OPTION_ID) }
        verify(exactly = 0) { productOptionService.delete(any()) }
    }

    @Test
    @DisplayName("상품과 옵션 삭제 성공")
    fun deleteProductWithOptionsSuccess() {
        // given
        val product = mockk<Product>()
        every { product.id } returns TEST_PRODUCT_ID

        every { productService.get(TEST_PRODUCT_ID) } returns product
        every { productOptionService.deleteAll(TEST_PRODUCT_ID) } returns Unit
        every { productService.delete(TEST_PRODUCT_ID) } returns Unit

        // when
        productFacade.deleteProductWithOptions(TEST_PRODUCT_ID)

        // then
        verify(exactly = 1) { productService.get(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productOptionService.deleteAll(TEST_PRODUCT_ID) }
        verify(exactly = 1) { productService.delete(TEST_PRODUCT_ID) }
    }

    @Test
    @DisplayName("존재하지 않는 상품 삭제 시 예외 발생")
    fun deleteNonExistentProductThrowsException() {
        // given
        val nonExistentProductId = 999L
        val errorMessage = "Product not found with id: $nonExistentProductId"

        every { productService.get(nonExistentProductId) } throws IllegalArgumentException(errorMessage)

        // when & then
        val exception = assertThrows<IllegalArgumentException> {
            productFacade.deleteProductWithOptions(nonExistentProductId)
        }

        assertEquals(errorMessage, exception.message)
        verify(exactly = 1) { productService.get(nonExistentProductId) }
        verify(exactly = 0) { productOptionService.deleteAll(any()) }
        verify(exactly = 0) { productService.delete(any()) }
    }
} 