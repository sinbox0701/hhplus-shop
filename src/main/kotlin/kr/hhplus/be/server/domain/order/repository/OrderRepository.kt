package kr.hhplus.be.server.domain.order.repository

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import java.time.LocalDateTime

interface OrderRepository {
    fun save(order: Order): Order
    fun findAll(): List<Order>
    fun findById(id: Long): Order?
    fun findByUserId(userId: Long): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>
    fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Order>
    fun update(order: Order): Order
    fun updateStatus(id: Long, status: OrderStatus): Order
    fun updateTotalPrice(id: Long, totalPrice: Double): Order
    fun delete(id: Long)
}
