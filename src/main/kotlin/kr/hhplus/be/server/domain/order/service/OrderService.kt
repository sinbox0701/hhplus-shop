package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
) {
    fun createOrder(accountId: Long, accountCouponId: Long? = null): Order {
        val id = System.currentTimeMillis()
        val order = Order.create(id, accountId, accountCouponId, status = OrderStatus.PENDING, totalPrice = 0.0)
        return orderRepository.save(order)
    }
    
    fun getOrder(id: Long): Order {
        return orderRepository.findById(id) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
    }
    
    fun getOrdersByAccountId(accountId: Long): List<Order> {
        return orderRepository.findByAccountId(accountId)
    }
    
    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return orderRepository.findByStatus(status)
    }
    
    fun getOrdersByAccountIdAndStatus(accountId: Long, status: OrderStatus): List<Order> {
        return orderRepository.findByAccountIdAndStatus(accountId, status)
    }
    
    fun getOrdersByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Order> {
        return orderRepository.findByCreatedAtBetween(startDate, endDate)
    }
    
    fun updateOrderStatus(id: Long, status: OrderStatus): Order {
        val order = getOrder(id)
        if (status == OrderStatus.CANCELLED && !order.isCancellable()) {
            throw IllegalStateException("주문을 취소할 수 없습니다. 현재 상태: ${order.status}")
        }
        return orderRepository.updateStatus(id, status)
    }
    
    fun cancelOrder(id: Long): Order {
        return updateOrderStatus(id, OrderStatus.CANCELLED)
    }
    
    fun completeOrder(id: Long): Order {
        return updateOrderStatus(id, OrderStatus.COMPLETED)
    }
    
    fun updateOrderTotalPrice(id: Long, totalPrice: Double, discountRate: Double? = null): Order {
        val order = getOrder(id)
        return orderRepository.updateTotalPrice(id, totalPrice, discountRate)
    }
}
