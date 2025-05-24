package kr.hhplus.be.server.infrastructure.event

import kr.hhplus.be.server.domain.order.OrderEventPublisher
import kr.hhplus.be.server.domain.order.event.OrderEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

/**
 * Spring의 ApplicationEventPublisher를 사용한 이벤트 발행자 구현체
 */
@Component
class SpringOrderEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : OrderEventPublisher {
    
    override fun publish(event: OrderEvent) {
        applicationEventPublisher.publishEvent(event)
    }
} 