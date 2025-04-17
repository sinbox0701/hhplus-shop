package kr.hhplus.be.server.infrastructure.order

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface JpaOrderItemRepository : JpaRepository<OrderItemEntity, Long> {
    fun findByOrderId(orderId: Long): List<OrderItemEntity>
    fun findByProductId(productId: Long): List<OrderItemEntity>
    fun findByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItemEntity?
    
    @Modifying
    @Query("DELETE FROM OrderItemEntity o WHERE o.orderId = :orderId")
    fun deleteByOrderId(@Param("orderId") orderId: Long): Int
    
    @Query("""
        SELECT o.productId, SUM(o.quantity) as total
        FROM OrderItemEntity o
        JOIN OrderEntity oe ON o.orderId = oe.id
        WHERE oe.status = 'COMPLETED' AND oe.orderDate BETWEEN :startDate AND :endDate
        GROUP BY o.productId
        ORDER BY total DESC
    """)
    fun findTopSellingProducts(@Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<Array<Any>>

    /**
     * 특정 기간 동안 가장 많이 팔린 상품 ID를 판매량 순으로 조회
     */
    @Query("""
        SELECT oi.productId FROM OrderItemEntity oi 
        JOIN oi.order o 
        WHERE o.createdAt BETWEEN :startDate AND :endDate 
        GROUP BY oi.productId 
        ORDER BY SUM(oi.quantity) DESC 
        LIMIT :limit
    """)
    fun findTopSellingProductIds(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int): List<Long>
} 