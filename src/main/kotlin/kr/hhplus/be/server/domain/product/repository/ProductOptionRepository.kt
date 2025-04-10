package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.model.ProductOption

interface ProductOptionRepository {
    fun save(productOption: ProductOption): ProductOption
    fun findById(id: Long): ProductOption?
    fun findByProductId(productId: Long): List<ProductOption>
    fun findByProductIdAndId(productId: Long, id: Long): ProductOption?
    fun update(productOption: ProductOption): ProductOption
    fun updateQuantity(id: Long, quantity: Int): ProductOption
    fun delete(id: Long)
} 