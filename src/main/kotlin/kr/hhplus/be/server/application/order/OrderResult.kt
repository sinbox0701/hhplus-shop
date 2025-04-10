package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.model.Order
import kr.hhplus.be.server.domain.order.model.OrderItem

class OrderResult{
    /**
     * 주문과 주문 상품 정보를 함께 담는 데이터 클래스
     */
    data class OrderWithItems(
        val order: Order,
        val items: List<OrderItem>
    )
}