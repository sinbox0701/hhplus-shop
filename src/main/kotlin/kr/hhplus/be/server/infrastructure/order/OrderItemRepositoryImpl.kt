package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class OrderItemRepositoryImpl(
    private val jpaOrderItemRepository: JpaOrderItemRepository
) : OrderItemRepository {
    
    override fun save(orderItem: OrderItem): OrderItem {
        val orderItemEntity = OrderItemEntity.fromOrderItem(orderItem)
        val savedEntity = jpaOrderItemRepository.save(orderItemEntity)
        return savedEntity.toOrderItem()
    }
    
    override fun findById(id: Long): OrderItem? {
        return jpaOrderItemRepository.findByIdOrNull(id)?.toOrderItem()
    }
    
    override fun findByOrderId(orderId: Long): List<OrderItem> {
        return jpaOrderItemRepository.findByOrderId(orderId).map { it.toOrderItem() }
    }
    
    override fun findByProductId(productId: Long): List<OrderItem> {
        return jpaOrderItemRepository.findByProductId(productId).map { it.toOrderItem() }
    }
    
    override fun findByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItem? {
        return jpaOrderItemRepository.findByOrderIdAndProductOptionId(orderId, productOptionId)?.toOrderItem()
    }
    
    override fun update(orderItem: OrderItem): OrderItem {
        val orderItemEntity = OrderItemEntity.fromOrderItem(orderItem)
        val savedEntity = jpaOrderItemRepository.save(orderItemEntity)
        return savedEntity.toOrderItem()
    }
    
    override fun delete(id: Long) {
        jpaOrderItemRepository.deleteById(id)
    }
    
    @Transactional
    override fun deleteByOrderId(orderId: Long) {
        jpaOrderItemRepository.deleteByOrderId(orderId)
    }
    
    override fun findTopSellingProductIds(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int): List<Long> {
        return jpaOrderItemRepository.findTopSellingProductIds(startDate, endDate, limit)
    }
} 