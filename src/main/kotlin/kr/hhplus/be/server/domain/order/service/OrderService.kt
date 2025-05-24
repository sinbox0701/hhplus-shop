package kr.hhplus.be.server.domain.order.service

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.Optional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: OrderEventPublisher,
    private val orderItemService: OrderItemService,
    private val timeProvider: TimeProvider
) {
    /**
     * 주문 생성
     */
    @Transactional
    fun createOrder(command: OrderCommand.CreateOrderCommand): Order {
        try {
            // 주문 객체 생성 (핵심 로직)
            val order = Order.create(
                userId = command.userId,
                userCouponId = command.userCouponId,
                totalPrice = command.totalPrice,
                timeProvider = timeProvider
            )
            
            val savedOrder = orderRepository.save(order)
            
            return savedOrder
        } catch (e: Exception) {
            // 주문 생성 실패 이벤트 발행
            orderEventPublisher.publish(
                OrderEvent.Failed(
                    userId = command.userId,
                    reason = e.message ?: "Unknown error",
                    failedAt = LocalDateTime.now()
                )
            )
            throw e
        }
    }
    
    /**
     * 주문 생성 후 이벤트 발행
     */
    @Transactional
    fun createOrderAndPublishEvent(command: OrderCommand.CreateOrderCommand, orderItems: List<OrderItem>): Order {
        val order = createOrder(command)
        
        // 주문 생성 이벤트 발행 (부가 로직을 위한 이벤트)
        orderEventPublisher.publish(
            OrderEvent.Created(
                orderId = order.id!!,
                userId = order.userId,
                userCouponId = order.userCouponId,
                orderItems = orderItems,
                totalPrice = order.totalPrice,
                createdAt = order.createdAt
            )
        )
        
        return order
    }

    /**
     * 주문 완료 처리 및 이벤트 발행
     */
    @Transactional
    fun completeOrder(orderId: Long): Order {
        val order = getOrder(orderId)
        
        if (order.status != OrderStatus.PENDING) {
            throw IllegalStateException("완료할 수 없는 주문 상태입니다: ${order.status}")
        }
        
        // 주문 상태 업데이트 (핵심 로직)
        val updatedOrder = updateOrderStatus(OrderCommand.UpdateOrderStatusCommand(
            id = orderId,
            status = OrderStatus.COMPLETED
        ))
        
        // 주문 아이템 조회
        val orderItems = orderItemService.getByOrderId(orderId)
        
        // 주문 완료 이벤트 발행 (부가 로직을 위한 이벤트)
        orderEventPublisher.publish(
            OrderEvent.Completed(
                orderId = updatedOrder.id!!,
                userId = updatedOrder.userId,
                userCouponId = updatedOrder.userCouponId,
                totalPrice = updatedOrder.totalPrice,
                orderItems = orderItems,
                completedAt = LocalDateTime.now()
            )
        )
        
        return updatedOrder
    }
    
    /**
     * 주문 취소 처리 및 이벤트 발행
     */
    @Transactional
    fun cancelOrder(orderId: Long): Order {
        val order = getOrder(orderId)
        val previousStatus = order.status
        
        if (!order.isCancellable()) {
            throw IllegalStateException("취소할 수 없는 주문 상태입니다: ${order.status}")
        }
        
        // 주문 상태 업데이트 (핵심 로직)
        val updatedOrder = updateOrderStatus(OrderCommand.UpdateOrderStatusCommand(
            id = orderId, 
            status = OrderStatus.CANCELLED
        ))
        
        // 주문 아이템 조회
        val orderItems = orderItemService.getByOrderId(orderId)
        
        // 주문 취소 이벤트 발행 (부가 로직을 위한 이벤트)
        orderEventPublisher.publish(
            OrderEvent.Cancelled(
                orderId = updatedOrder.id!!,
                userId = updatedOrder.userId,
                userCouponId = updatedOrder.userCouponId,
                orderItems = orderItems,
                totalPrice = updatedOrder.totalPrice,
                previousStatus = previousStatus,
                cancelledAt = LocalDateTime.now()
            )
        )
        
        return updatedOrder
    }
    
    /**
     * 주문 상태 업데이트
     */
    @Transactional
    fun updateOrderStatus(command: OrderCommand.UpdateOrderStatusCommand): Order {
        val order = getOrder(command.id)
        val updatedOrder = order.updateStatus(command.status, timeProvider)
        return orderRepository.save(updatedOrder)
    }
    
    /**
     * 주문 총 가격 업데이트
     */
    @Transactional
    fun updateOrderTotalPrice(command: OrderCommand.UpdateOrderTotalPriceCommand): Order {
        val order = getOrder(command.id)
        val updatedOrder = order.updateTotalPrice(command.totalPrice, timeProvider)
        return orderRepository.save(updatedOrder)
    }
    
    /**
     * 주문 조회
     */
    @Transactional(readOnly = true)
    fun getOrder(id: Long): Order {
        return orderRepository.findById(id) ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
    }
    
    /**
     * 사용자 주문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getOrdersByUserId(userId: Long): List<Order> {
        return orderRepository.findByUserId(userId)
    }
    
    /**
     * 사용자의 특정 상태 주문 목록 조회
     */
    @Transactional(readOnly = true)
    fun getOrdersByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> {
        return orderRepository.findByUserIdAndStatus(userId, status)
    }
    
    fun getOrdersByStatus(status: OrderStatus): List<Order> {
        return orderRepository.findByStatus(status)
    }
    
    fun getOrdersByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<Order> {
        return orderRepository.findByCreatedAtBetween(startDate, endDate)
    }
}
