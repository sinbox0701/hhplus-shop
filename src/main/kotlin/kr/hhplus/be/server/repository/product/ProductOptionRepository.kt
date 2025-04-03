package kr.hhplus.be.server.repository.product

import kr.hhplus.be.server.domain.product.ProductOption

interface ProductOptionRepository {
    fun save(productOption: ProductOption): ProductOption
    fun findById(optionId: Long): ProductOption?
    fun findByProductId(productId: Long): List<ProductOption>
    fun update(productOption: ProductOption): ProductOption
    fun delete(optionId: Long)
} 