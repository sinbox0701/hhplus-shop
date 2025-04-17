package kr.hhplus.be.server.infrastructure.product

import kr.hhplus.be.server.domain.product.model.ProductDailySales
import kr.hhplus.be.server.domain.product.repository.ProductDailySalesRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
class ProductDailySalesRepositoryImpl(
    private val jpaProductDailySalesRepository: JpaProductDailySalesRepository
) : ProductDailySalesRepository {
    
    override fun findTopSellingProducts(startDate: LocalDate, limit: Int): List<Long> {
        return jpaProductDailySalesRepository.findTopSellingProducts(startDate, limit)
    }
    
    override fun save(productDailySales: ProductDailySales): ProductDailySales {
        val entity = ProductDailySalesEntity.fromProductDailySales(productDailySales)
        val savedEntity = jpaProductDailySalesRepository.save(entity)
        return savedEntity.toProductDailySales()
    }
    
    override fun saveAll(productDailySales: List<ProductDailySales>): List<ProductDailySales> {
        val entities = productDailySales.map { ProductDailySalesEntity.fromProductDailySales(it) }
        val savedEntities = jpaProductDailySalesRepository.saveAll(entities)
        return savedEntities.map { it.toProductDailySales() }
    }
    
    override fun delete(id: Long) {
        jpaProductDailySalesRepository.deleteById(id)
    }
    
    override fun findBySaleDate(saleDate: LocalDate): List<ProductDailySales> {
        return jpaProductDailySalesRepository.findBySaleDate(saleDate)
            .map { it.toProductDailySales() }
    }
} 