package kr.hhplus.be.server.product

import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import java.time.LocalDateTime

object TestFixtures {
    // 상수 정의
    const val PRODUCT_ID = 1L
    const val PRODUCT_OPTION_ID = 1L
    const val PRODUCT_PRICE = 10000.0
    const val OPTION_PRICE = 1000.0
    const val AVAILABLE_QUANTITY = 100
    
    // 상품 Fixture
    fun createProduct(
        id: Long = PRODUCT_ID,
        name: String = "테스트 상품",
        description: String = "테스트 상품 설명",
        price: Double = PRODUCT_PRICE
    ): Product {
        val now = LocalDateTime.now()
        val product = mockk<Product>()
        
        every { product.id } returns id
        every { product.name } returns name
        every { product.description } returns description
        every { product.price } returns price
        every { product.createdAt } returns now.minusDays(2)
        every { product.updatedAt } returns now.minusDays(2)
        
        return product
    }
    
    // 상품 옵션 Fixture
    fun createProductOption(
        id: Long = PRODUCT_OPTION_ID,
        productId: Long = PRODUCT_ID,
        name: String = "테스트 옵션",
        availableQuantity: Int = AVAILABLE_QUANTITY,
        additionalPrice: Double = OPTION_PRICE
    ): ProductOption {
        val now = LocalDateTime.now()
        val option = mockk<ProductOption>()
        
        every { option.id } returns id
        every { option.productId } returns productId
        every { option.name } returns name
        every { option.availableQuantity } returns availableQuantity
        every { option.additionalPrice } returns additionalPrice
        every { option.createdAt } returns now.minusDays(2)
        every { option.updatedAt } returns now.minusDays(2)
        
        return option
    }
    
    // 상품 컬렉션 생성 헬퍼 메서드
    fun createProducts(count: Int): List<Product> {
        return (1..count).map {
            createProduct(id = it.toLong(), name = "테스트 상품 $it")
        }
    }
    
    // 상품 옵션 컬렉션 생성 헬퍼 메서드
    fun createProductOptions(count: Int, productId: Long = PRODUCT_ID): List<ProductOption> {
        return (1..count).map {
            createProductOption(id = it.toLong(), productId = productId, name = "옵션 $it")
        }
    }
} 