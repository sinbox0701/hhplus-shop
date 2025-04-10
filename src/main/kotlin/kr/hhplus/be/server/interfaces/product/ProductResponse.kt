package kr.hhplus.be.server.interfaces.product

import kr.hhplus.be.server.application.product.ProductResult
import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import java.time.LocalDateTime

class ProductResponse {
    
    data class Response(
        val productId: Long,
        val name: String,
        val description: String,
        val price: Double,
        val options: List<ProductOptionResponse>,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    data class SimpleResponse(
        val productId: Long,
        val name: String,
        val description: String,
        val price: Double,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    data class ProductOptionResponse(
        val optionId: Long,
        val name: String,
        val additionalPrice: Double,
        val availableQuantity: Int
    )

    data class DetailResponse(
        val productId: Long,
        val name: String,
        val description: String,
        val price: Double,
        val options: List<ProductOptionResponse>,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )
    
    data class TopSellingProductResponse(
        val productId: Long,
        val name: String,
        val description: String,
        val price: Double,
        val totalSoldQuantity: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    )

    data class TopSellingProductsResponse(
        val products: List<TopSellingProductResponse>,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime
    )
    
    companion object {
        fun from(product: Product): Response {
            return Response(
                productId = product.id!!,
                name = product.name,
                description = product.description,
                price = product.price,
                options = emptyList(),
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
        
        fun simpleFrom(product: Product): SimpleResponse {
            return SimpleResponse(
                productId = product.id!!,
                name = product.name,
                description = product.description,
                price = product.price,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
        
        fun from(productWithOptions: ProductResult.ProductWithOptions): Response {
            return Response(
                productId = productWithOptions.product.id!!,
                name = productWithOptions.product.name,
                description = productWithOptions.product.description,
                price = productWithOptions.product.price,
                options = productWithOptions.options.map { option ->
                    ProductOptionResponse(
                        optionId = option.id!!,
                        name = option.name,
                        additionalPrice = option.additionalPrice,
                        availableQuantity = option.availableQuantity
                    )
                },
                createdAt = productWithOptions.product.createdAt,
                updatedAt = productWithOptions.product.updatedAt
            )
        }
        
        fun detailFrom(productWithOptions: ProductResult.ProductWithOptions): DetailResponse {
            return DetailResponse(
                productId = productWithOptions.product.id!!,
                name = productWithOptions.product.name,
                description = productWithOptions.product.description,
                price = productWithOptions.product.price,
                options = productWithOptions.options.map { option ->
                    ProductOptionResponse(
                        optionId = option.id!!,
                        name = option.name,
                        additionalPrice = option.additionalPrice,
                        availableQuantity = option.availableQuantity
                    )
                },
                createdAt = productWithOptions.product.createdAt,
                updatedAt = productWithOptions.product.updatedAt
            )
        }
        
        fun topSellingFrom(topSellingProducts: List<Product>): TopSellingProductsResponse {
            val now = LocalDateTime.now()
            val threeDaysAgo = now.minusDays(3)
            
            return TopSellingProductsResponse(
                products = topSellingProducts.map { product ->
                    TopSellingProductResponse(
                        productId = product.id!!,
                        name = product.name,
                        description = product.description,
                        price = product.price,
                        totalSoldQuantity = 0, // 실제 판매량은 주문 서비스에서 제공해야 함
                        createdAt = product.createdAt,
                        updatedAt = product.updatedAt
                    )
                },
                startDate = threeDaysAgo,
                endDate = now
            )
        }
    }
}
