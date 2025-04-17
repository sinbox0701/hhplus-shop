package kr.hhplus.be.server.domain.product.repository

import kr.hhplus.be.server.domain.product.model.ProductDailySales
import java.time.LocalDate

interface ProductDailySalesRepository {
    fun findTopSellingProducts(startDate: LocalDate, limit: Int): List<Long>
    fun save(productDailySales: ProductDailySales): ProductDailySales
    fun saveAll(productDailySales: List<ProductDailySales>): List<ProductDailySales>
    fun delete(id: Long)
    fun findBySaleDate(saleDate: LocalDate): List<ProductDailySales>
}