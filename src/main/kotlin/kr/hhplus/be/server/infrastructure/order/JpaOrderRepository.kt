package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.model.OrderStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface JpaOrderRepository : JpaRepository<OrderEntity, Long> {
    fun findByUserId(userId: Long): List<OrderEntity>
    fun findByStatus(status: OrderStatus): List<OrderEntity>
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<OrderEntity>
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<OrderEntity>
    
    @Modifying
    @Query("UPDATE OrderEntity o SET o.status = :status, o.orderDate = CASE WHEN :status = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE o.orderDate END, o.updatedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    fun updateStatus(@Param("id") id: Long, @Param("status") status: OrderStatus): Int
    
    @Modifying
    @Query("UPDATE OrderEntity o SET o.totalPrice = :totalPrice, o.updatedAt = CURRENT_TIMESTAMP WHERE o.id = :id")
    fun updateTotalPrice(@Param("id") id: Long, @Param("totalPrice") totalPrice: Double): Int
} 