package kr.hhplus.be.server.domain.order.repository

import kr.hhplus.be.server.domain.order.model.OrderItem
import java.time.LocalDateTime

interface OrderItemRepository {
    fun save(orderItem: OrderItem): OrderItem
    fun findById(id: Long): OrderItem?
    fun findByOrderId(orderId: Long): List<OrderItem>
    fun findByProductId(productId: Long): List<OrderItem>
    fun findByOrderIdAndProductOptionId(orderId: Long, productOptionId: Long): OrderItem?
    fun update(orderItem: OrderItem): OrderItem
    fun delete(id: Long)
    fun deleteByOrderId(orderId: Long)
    
    /**
     * 특정 기간 동안 가장 많이 팔린 상품 ID를 판매량 순으로 조회
     * @param startDate 조회 시작 일시
     * @param endDate 조회 종료 일시
     * @param limit 조회할 상품 수
     * @return 판매량 내림차순으로 정렬된 상품 ID 목록
     */
    fun findTopSellingProductIds(startDate: LocalDateTime, endDate: LocalDateTime, limit: Int): List<Long>
    
    /**
     * 특정 기간 동안의 상품별 판매량을 Map으로 반환
     * @param startDate 조회 시작 일시
     * @param endDate 조회 종료 일시
     * @return 상품ID와 판매량으로 구성된 Map
     */
    fun findProductQuantityMap(startDate: LocalDateTime, endDate: LocalDateTime): Map<Long, Int>
}
