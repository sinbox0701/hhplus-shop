package kr.hhplus.be.server.controller.product.dto.response

import java.math.BigDecimal
import java.time.LocalDateTime

data class ProductResponse(
    val productId: Int,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val options: List<ProductOptionResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ProductOptionResponse(
    val optionId: Int,
    val name: String,
    val additionalPrice: BigDecimal,
    val availableQuantity: Int
)

data class ProductDetailResponse(
    val productId: Int,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val options: List<ProductOptionResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 