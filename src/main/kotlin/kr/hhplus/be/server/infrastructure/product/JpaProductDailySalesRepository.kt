package kr.hhplus.be.server.infrastructure.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface JpaProductDailySalesRepository : JpaRepository<ProductDailySalesEntity, Long> {
    
    @Query("""
        SELECT pds.productId FROM ProductDailySalesEntity pds 
        WHERE pds.saleDate >= :startDate 
        ORDER BY pds.saleDate DESC, pds.quantitySold DESC 
        LIMIT :limit
    """)
    fun findTopSellingProducts(startDate: LocalDate, limit: Int): List<Long>
    
    fun findBySaleDate(saleDate: LocalDate): List<ProductDailySalesEntity>
} 