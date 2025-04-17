package kr.hhplus.be.server.infrastructure.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaOrderItemRepository : JpaRepository<OrderItemEntity, Long> {
    fun findByOrderId(orderId: Long): List<OrderItemEntity>
    fun findByProductId(productId: Long): List<OrderItemEntity>
    fun findByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItemEntity?
    
    @Modifying
    @Query("DELETE FROM OrderItemEntity o WHERE o.orderId = :orderId")
    fun deleteByOrderId(@Param("orderId") orderId: Long): Int
} 