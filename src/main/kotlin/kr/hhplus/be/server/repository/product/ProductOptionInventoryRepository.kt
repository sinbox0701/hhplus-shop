package kr.hhplus.be.server.repository.product

import kr.hhplus.be.server.domain.product.ProductOptionInventory

interface ProductOptionInventoryRepository {
    fun save(productOptionInventory: ProductOptionInventory): ProductOptionInventory
    fun findById(inventoryId: Long): ProductOptionInventory?
    fun findByOptionId(optionId: Long): ProductOptionInventory?
    fun update(productOptionInventory: ProductOptionInventory): ProductOptionInventory
    fun delete(inventoryId: Long)
} 