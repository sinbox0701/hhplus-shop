package kr.hhplus.be.server.infrastructure.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderStatus
import kr.hhplus.be.server.domain.order.repository.OrderRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
class OrderRepositoryImpl(
    private val jpaOrderRepository: JpaOrderRepository
) : OrderRepository {
    
    override fun save(order: Order): Order {
        val orderEntity = OrderEntity.fromOrder(order)
        val savedEntity = jpaOrderRepository.save(orderEntity)
        return savedEntity.toOrder()
    }
    
    override fun findAll(): List<Order> {
        return jpaOrderRepository.findAll().map { it.toOrder() }
    }
    
    override fun findById(id: Long): Order? {
        return jpaOrderRepository.findByIdOrNull(id)?.toOrder()
    }
    
    override fun findByUserId(userId: Long): List<Order> {
        return jpaOrderRepository.findByUserId(userId).map { it.toOrder() }
    }
    
    override fun findByStatus(status: OrderStatus): List<Order> {
        return jpaOrderRepository.findByStatus(status).map { it.toOrder() }
    }
    
    override fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order> {
        return jpaOrderRepository.findByUserIdAndStatus(userId, status).map { it.toOrder() }
    }
    
    override fun findByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<Order> {
        return jpaOrderRepository.findByCreatedAtBetween(startDate, endDate).map { it.toOrder() }
    }
    
    override fun update(order: Order): Order {
        val orderEntity = OrderEntity.fromOrder(order)
        val savedEntity = jpaOrderRepository.save(orderEntity)
        return savedEntity.toOrder()
    }
    
    @Transactional
    override fun updateStatus(id: Long, status: OrderStatus): Order {
        // 먼저 엔티티 존재 여부 확인
        val orderEntity = jpaOrderRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
        
        // 상태 업데이트 쿼리 실행
        jpaOrderRepository.updateStatus(id, status)
        
        // 업데이트된 엔티티 다시 조회하여 반환
        return jpaOrderRepository.findByIdOrNull(id)!!.toOrder()
    }
    
    @Transactional
    override fun updateTotalPrice(id: Long, totalPrice: Double): Order {
        // 먼저 엔티티 존재 여부 확인
        val orderEntity = jpaOrderRepository.findByIdOrNull(id)
            ?: throw IllegalArgumentException("주문을 찾을 수 없습니다: $id")
        
        // 금액 업데이트 쿼리 실행
        jpaOrderRepository.updateTotalPrice(id, totalPrice)
        
        // 업데이트된 엔티티 다시 조회하여 반환
        return jpaOrderRepository.findByIdOrNull(id)!!.toOrder()
    }
    
    override fun delete(id: Long) {
        jpaOrderRepository.deleteById(id)
    }
} 