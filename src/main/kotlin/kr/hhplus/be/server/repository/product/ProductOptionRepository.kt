package kr.hhplus.be.server.repository.product

import kr.hhplus.be.server.domain.product.ProductOption
import java.math.BigDecimal

interface ProductOptionRepository {
    fun save(productOption: ProductOption): ProductOption
    fun findById(optionId: Int): ProductOption?
    fun findByProductId(productId: Int): List<ProductOption>
    fun update(productOption: ProductOption): ProductOption
    fun delete(optionId: Int)
} 