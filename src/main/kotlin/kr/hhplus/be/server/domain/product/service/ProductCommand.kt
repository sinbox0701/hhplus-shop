package kr.hhplus.be.server.domain.product.service

import java.math.BigDecimal

class ProductCommand {
    data class CreateProductCommand(
        val name: String,
        val description: String,
        val price: BigDecimal
    )

    data class UpdateProductCommand(
        val id: Long,
        val name: String? = null,
        val description: String? = null,
        val price: BigDecimal? = null
    )

    data class UpdateStockCommand(
        val productId: Long,
        val quantity: Int
    )
}
