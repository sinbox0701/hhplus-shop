package kr.hhplus.be.server.infrastructure.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface JpaProductRepository : JpaRepository<ProductEntity, Long> {
    fun findAllByIdIn(ids: List<Long>): List<ProductEntity>
} 