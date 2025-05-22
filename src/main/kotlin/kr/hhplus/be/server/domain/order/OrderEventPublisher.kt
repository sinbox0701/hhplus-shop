package kr.hhplus.be.server.domain.order

import kr.hhplus.be.server.domain.order.event.OrderEvent

/**
 * 주문 이벤트 발행자 인터페이스
 */
interface OrderEventPublisher {
    /**
     * 주문 이벤트 발행
     */
    fun publish(event: OrderEvent)
} 