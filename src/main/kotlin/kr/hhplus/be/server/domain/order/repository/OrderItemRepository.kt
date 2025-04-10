package kr.hhplus.be.server.domain.order.repository

import kr.hhplus.be.server.domain.order.model.OrderItem

interface OrderItemRepository {
    fun save(orderItem: OrderItem): OrderItem
    fun findById(id: Long): OrderItem?
    fun findByOrderId(orderId: Long): List<OrderItem>
    fun findByProductId(productId: Long): List<OrderItem>
    fun findByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItem?
    fun update(orderItem: OrderItem): OrderItem
    fun delete(id: Long)
    fun deleteByOrderId(orderId: Long)
}
