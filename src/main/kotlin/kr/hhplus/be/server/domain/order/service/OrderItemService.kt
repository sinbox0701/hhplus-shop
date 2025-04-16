package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.repository.OrderItemRepository
import org.springframework.stereotype.Service

@Service
class OrderItemService(
    private val orderItemRepository: OrderItemRepository
) {
    fun create(command: OrderItemCommand.CreateOrderItemCommand): OrderItem {
        // 실제 상품 가격을 가져오는 로직이 필요합니다. 현재는 100.0으로 임시 설정
        val basePrice = 100.0 // 실제로는 상품 서비스 또는 레포지토리에서 가격을 가져와야 함
        val finalPrice = command.discountRate?.let { basePrice * (1 - it / 100) } ?: basePrice
        
        val orderItem = OrderItem.create(
            orderId = command.orderId,
            productId = command.productId, 
            productOptionId = command.productOptionId, 
            userCouponId = command.userCouponId,
            quantity = command.quantity, 
            price = finalPrice
        )
        return orderItemRepository.save(orderItem)
    }

    fun createAll(command: List<OrderItemCommand.CreateOrderItemCommand>): List<OrderItem> {
        return command.map { create(it) }
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
    
    fun update(command: OrderItemCommand.UpdateOrderItemCommand): OrderItem {
        val orderItem = getById(command.id)
        val updatedOrderItem = orderItem.update(command.quantity, command.productPrice)
        return orderItemRepository.update(updatedOrderItem)
    }
    
    fun updatePrice(command: OrderItemCommand.UpdateOrderItemPriceCommand): OrderItem {
        val orderItem = getById(command.id)
        val updatedOrderItem = orderItem.updatePrice(command.discountRate)
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
