package kr.hhplus.be.server.repository.product

import kr.hhplus.be.server.domain.product.Product
import java.math.BigDecimal

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(productId: Int): Product?
    fun update(productId: Int, name: String?, description: String?, price: BigDecimal?): Product
    fun delete(productId: Int)
}