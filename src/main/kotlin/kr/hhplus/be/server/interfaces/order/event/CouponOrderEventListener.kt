package kr.hhplus.be.server.interfaces.order.event

import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.order.event.OrderEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 주문 이벤트를 구독하여 쿠폰 관련 처리를 수행하는 리스너
 */
@Component
class CouponOrderEventListener(
    private val couponService: CouponService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 주문 완료 이벤트 처리 - 쿠폰 사용 처리
     * - 트랜잭션 완료 후 이벤트 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Completed::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional
    fun handleOrderCompleted(event: OrderEvent.Completed) {
        // 쿠폰이 없으면 처리하지 않음
        val userCouponId = event.userCouponId ?: return
        
        try {
            log.info("쿠폰 사용 처리 시작: orderId={}, userCouponId={}", event.orderId, userCouponId)
            
            // 쿠폰 사용 처리
            couponService.useUserCoupon(userCouponId)
            
            log.info("쿠폰 사용 처리 완료: orderId={}, userCouponId={}", event.orderId, userCouponId)
        } catch (e: Exception) {
            log.error("쿠폰 사용 처리 실패: orderId={}, userCouponId={}, 사유={}", 
                event.orderId, userCouponId, e.message, e)
            
            // 쿠폰 사용 실패 이벤트 발행
            applicationEventPublisher.publishEvent(
                CouponEvent.UseFailed(
                    orderId = event.orderId,
                    userId = event.userId,
                    userCouponId = userCouponId,
                    reason = e.message ?: "Unknown error"
                )
            )
        }
    }
    
    /**
     * 주문 취소 이벤트 처리 - 쿠폰 사용 취소
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Cancelled::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional
    fun handleOrderCancelled(event: OrderEvent.Cancelled) {
        // 이전 상태가 COMPLETED이고 쿠폰이 있는 경우에만 쿠폰 사용 취소
        if (event.previousStatus != kr.hhplus.be.server.domain.order.model.OrderStatus.COMPLETED) {
            return
        }
        
        val userCouponId = event.userCouponId ?: return
        
        try {
            log.info("쿠폰 사용 취소 처리 시작: orderId={}, userCouponId={}", event.orderId, userCouponId)
            
            // 현재 구현에서는 쿠폰 취소 기능이 없으므로 로그만 남김
            // 실제 구현 시 아래 주석을 해제하고 구현 필요
            // couponService.cancelUserCoupon(userCouponId)
            
            log.info("쿠폰 사용 취소 처리 완료: orderId={}, userCouponId={}", event.orderId, userCouponId)
        } catch (e: Exception) {
            log.error("쿠폰 사용 취소 처리 실패: orderId={}, userCouponId={}, 사유={}", 
                event.orderId, userCouponId, e.message, e)
        }
    }
}

/**
 * 쿠폰 관련 이벤트
 */
sealed class CouponEvent {
    /**
     * 쿠폰 사용 실패 이벤트
     */
    data class UseFailed(
        val orderId: Long,
        val userId: Long,
        val userCouponId: Long,
        val reason: String
    ): CouponEvent()
} 