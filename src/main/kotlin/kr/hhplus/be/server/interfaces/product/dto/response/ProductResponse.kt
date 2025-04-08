package kr.hhplus.be.server.controller.product.dto.response

import java.time.LocalDateTime

data class ProductResponse(
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

data class ProductDetailResponse(
    val productId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val options: List<ProductOptionResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 