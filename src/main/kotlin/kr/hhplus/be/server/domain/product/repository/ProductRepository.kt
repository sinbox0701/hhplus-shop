package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.model.Product

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: Long): Product?
    fun findAll(): List<Product>
    fun update(product: Product): Product
    fun delete(id: Long)
}