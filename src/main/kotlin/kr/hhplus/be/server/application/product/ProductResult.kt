package kr.hhplus.be.server.application.product

import kr.hhplus.be.server.domain.product.model.Product
import kr.hhplus.be.server.domain.product.model.ProductOption

class ProductResult {
    data class ProductWithOptions(
        val product: Product,
        val options: List<ProductOption>
    )
}
