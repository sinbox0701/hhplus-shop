package kr.hhplus.be.server.domain.product.service

class ProductOptionCommand {
    data class CreateProductOptionCommand(
        val productId: Long,
        val name: String,
        val availableQuantity: Int,
        val additionalPrice: Double
    )

    data class UpdateProductOptionCommand(
        val id: Long,
        val name: String? = null,
        val additionalPrice: Double? = null
    )

    data class UpdateQuantityCommand(
        val id: Long,
        val quantity: Int
    )
}