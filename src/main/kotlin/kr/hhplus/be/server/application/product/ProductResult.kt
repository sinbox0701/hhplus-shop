package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption
import java.math.BigDecimal

class ProductResult {
    data class ProductWithOptions(
        val product: Product,
        val options: List<ProductOption>
    )
    
    data class Single(
        val id: Long?,
        val name: String,
        val description: String,
        val price: BigDecimal,
        val createdAt: String,
        val updatedAt: String
    ) {
        companion object {
            fun from(product: Product): Single {
                return Single(
                    id = product.id,
                    name = product.name,
                    description = product.description,
                    price = product.price,
                    createdAt = product.createdAt.toString(),
                    updatedAt = product.updatedAt.toString()
                )
            }
        }
    }
}
