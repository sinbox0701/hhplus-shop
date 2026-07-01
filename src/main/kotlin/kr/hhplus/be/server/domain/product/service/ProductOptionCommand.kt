package kr.hhplus.be.server.domain.product.service

import java.math.BigDecimal

class ProductOptionCommand {
    data class CreateProductOptionCommand(
        val productId: Long,
        val name: String,
        val availableQuantity: Int,
        val additionalPrice: BigDecimal
    )

    data class UpdateProductOptionCommand(
        val id: Long,
        val name: String? = null,
        val additionalPrice: BigDecimal? = null
    )

    data class UpdateQuantityCommand(
        val id: Long,
        val quantity: Int
    )
}
