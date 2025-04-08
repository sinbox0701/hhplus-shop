package kr.hhplus.be.server.interfaces.product

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
}
