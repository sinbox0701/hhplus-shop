package kr.hhplus.be.server.controller.product.dto.response

import java.time.LocalDateTime

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