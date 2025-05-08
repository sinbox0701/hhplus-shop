package kr.hhplus.be.server.domain.product.service

class ProductCommand {
    data class CreateProductCommand(
        val name: String,
        val description: String,
        val price: Double
    )

    data class UpdateProductCommand(
        val id: Long,
        val name: String? = null,
        val description: String? = null,
        val price: Double? = null
    )
    
    data class UpdateStockCommand(
        val productId: Long,
        val quantity: Int
    )
}