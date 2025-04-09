package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import org.springframework.stereotype.Service

@Service
class OrderItemService(
    private val orderItemRepository: OrderItemRepository
) {
    fun create(
        orderId: Long,
        productId: Long,
        productOptionId: Long,
        quantity: Int,
        productPrice: Double,
        accountCouponId: Long? = null,
        discountRate: Double? = null
    ): OrderItem {
        val id = System.currentTimeMillis()
        val orderItem = OrderItem.create(
            id, orderId, productId, productOptionId, quantity, productPrice, accountCouponId, discountRate
        )
        return orderItemRepository.save(orderItem)
    }
    
    fun getById(id: Long): OrderItem {
        return orderItemRepository.findById(id) ?: throw IllegalArgumentException("주문 상품을 찾을 수 없습니다: $id")
    }
    
    fun getByOrderId(orderId: Long): List<OrderItem> {
        return orderItemRepository.findByOrderId(orderId)
    }
    
    fun getByProductId(productId: Long): List<OrderItem> {
        return orderItemRepository.findByProductId(productId)
    }
    
    fun getByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItem? {
        return orderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId)
    }
    
    fun update(id: Long, quantity: Int?, productPrice: Double?): OrderItem {
        val orderItem = getById(id)
        val updatedOrderItem = orderItem.update(quantity, productPrice)
        return orderItemRepository.update(updatedOrderItem)
    }
    
    fun updatePrice(id: Long, discountRate: Double?): OrderItem {
        val orderItem = getById(id)
        val updatedOrderItem = orderItem.updatePrice(discountRate)
        return orderItemRepository.update(updatedOrderItem)
    }
    
    fun deleteById(id: Long) {
        orderItemRepository.delete(id)
    }
    
    fun deleteAllByOrderId(orderId: Long) {
        orderItemRepository.deleteByOrderId(orderId)
    }
    
    fun calculateTotalPrice(orderItems: List<OrderItem>): Double {
        return orderItems.sumOf { it.price }
    }
}
