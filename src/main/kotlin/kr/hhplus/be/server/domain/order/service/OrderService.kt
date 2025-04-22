package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val timeProvider: TimeProvider
) {
    fun createOrder(command: OrderCommand.CreateOrderCommand): Order {
        val order = Order.create(
            userId = command.userId, 
            userCouponId = command.userCouponId, 
            totalPrice = command.totalPrice,
            timeProvider = timeProvider
        )
        return orderRepository.save(order)
    }
    
    fun getOrder(id: Long): Order {
        return orderRepository.findById(id) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
    }
    
    fun getOrdersByUserId(userId: Long): List<Order> {
        return orderRepository.findByUserId(userId)
    }
    
    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return orderRepository.findByStatus(status)
    }
    
    fun getOrdersByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> {
        return orderRepository.findByUserIdAndStatus(userId, status)
    }
    
    fun getOrdersByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Order> {
        return orderRepository.findByCreatedAtBetween(startDate, endDate)
    }
    
    fun updateOrderStatus(command: OrderCommand.UpdateOrderStatusCommand): Order {
        val order = getOrder(command.id)
        
        when (command.status) {
            OrderStatus.CANCELLED -> {
                if (!order.isCancellable()) {
                    throw IllegalStateException("주문을 취소할 수 없습니다. 현재 상태: ${order.status}")
                }
            }
            OrderStatus.COMPLETED -> {
                if (order.status != OrderStatus.PENDING) {
                    throw IllegalStateException("주문을 완료할 수 없습니다. 현재 상태: ${order.status}")
                }
            }
            OrderStatus.PENDING -> {
                if (order.status != OrderStatus.PENDING) {
                    throw IllegalStateException("이미 처리된 주문은 대기 상태로 변경할 수 없습니다. 현재 상태: ${order.status}")
                }
            }
        }
        
        val updatedOrder = order.updateStatus(command.status, timeProvider)
        return orderRepository.save(updatedOrder)
    }
    
    fun cancelOrder(id: Long): Order {
        val order = getOrder(id)
        if (!order.isCancellable()) {
            throw IllegalStateException("주문을 취소할 수 없습니다. 현재 상태: ${order.status}")
        }
        val updatedOrder = order.updateStatus(OrderStatus.CANCELLED, timeProvider)
        return orderRepository.save(updatedOrder)
    }
    
    fun completeOrder(id: Long): Order {
        val order = getOrder(id)
        if (order.status != OrderStatus.PENDING) {
            throw IllegalStateException("주문을 완료할 수 없습니다. 현재 상태: ${order.status}")
        }
        val updatedOrder = order.updateStatus(OrderStatus.COMPLETED, timeProvider)
        return orderRepository.save(updatedOrder)
    }
    
    fun updateOrderTotalPrice(command: OrderCommand.UpdateOrderTotalPriceCommand): Order {
        val order = getOrder(command.id)
        if (command.totalPrice <= 0) {
            throw IllegalArgumentException("총 가격은 0보다 커야 합니다.")
        }
        val updatedOrder = order.updateTotalPrice(command.totalPrice, timeProvider)
        return orderRepository.save(updatedOrder)
    }
}
