package kr.hhplus.be.server.domain.product.model

import java.time.LocalDate
import java.time.LocalDateTime

data class ProductDailySales(
    val id: Long? = null,
    val productId: Long,
    val salesDate: LocalDate,
    val quantitySold: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

