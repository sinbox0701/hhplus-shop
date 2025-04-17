package kr.hhplus.be.server.infrastructure.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaProductOptionRepository : JpaRepository<ProductOptionEntity, Long> {
    fun findByProductId(productId: Long): List<ProductOptionEntity>
    fun findByProductIdAndId(productId: Long, id: Long): ProductOptionEntity?
    
    @Modifying
    @Query("UPDATE ProductOptionEntity p SET p.availableQuantity = :quantity, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updateQuantity(@Param("id") id: Long, @Param("quantity") quantity: Int): Int
} 