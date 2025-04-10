package kr.hhplus.be.server.repository.product

import kr.hhplus.be.server.domain.product.Product

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(productId: Long): Product?
    fun update(product: Product): Product
    fun delete(productId: Long)
}