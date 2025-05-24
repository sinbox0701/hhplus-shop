package kr.hhplus.be.server.interfaces.order.event

import kr.hhplus.be.server.application.order.OrderFacade
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 보상 트랜잭션 처리를 위한 이벤트 리스너
 * - 각 도메인의 실패 이벤트를 수신하여 주문 취소 등 보상 트랜잭션 수행
 */
@Component
class CompensationOrderEventListener(
    private val orderFacade: OrderFacade
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 상품 재고 감소 실패 이벤트 처리 - 주문 취소
     */
    @EventListener
    @Transactional
    fun handleProductStockDecreaseFailed(event: ProductStockEvent.DecreaseFailed) {
        try {
            log.info("상품 재고 감소 실패로 인한 주문 취소 시작: orderId={}", event.orderId)
            
            // 주문 취소 처리
            orderFacade.cancelOrder(event.orderId, event.userId)
            
            log.info("상품 재고 감소 실패로 인한 주문 취소 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("상품 재고 감소 실패로 인한 주문 취소 처리 실패: orderId={}, 사유={}", 
                event.orderId, e.message, e)
        }
    }
    
    /**
     * 쿠폰 사용 실패 이벤트 처리 - 주문 취소
     */
    @EventListener
    @Transactional
    fun handleCouponUseFailed(event: CouponEvent.UseFailed) {
        try {
            log.info("쿠폰 사용 실패로 인한 주문 취소 시작: orderId={}", event.orderId)
            
            // 주문 취소 처리
            orderFacade.cancelOrder(event.orderId, event.userId)
            
            log.info("쿠폰 사용 실패로 인한 주문 취소 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("쿠폰 사용 실패로 인한 주문 취소 처리 실패: orderId={}, 사유={}", 
                event.orderId, e.message, e)
        }
    }
    
    /**
     * 계좌 금액 차감 실패 이벤트 처리 - 주문 취소
     */
    @EventListener
    @Transactional
    fun handleAccountWithdrawFailed(event: AccountEvent.WithdrawFailed) {
        try {
            log.info("계좌 금액 차감 실패로 인한 주문 취소 시작: orderId={}", event.orderId)
            
            // 주문 취소 처리
            orderFacade.cancelOrder(event.orderId, event.userId)
            
            log.info("계좌 금액 차감 실패로 인한 주문 취소 완료: orderId={}", event.orderId)
        } catch (e: Exception) {
            log.error("계좌 금액 차감 실패로 인한 주문 취소 처리 실패: orderId={}, 사유={}", 
                event.orderId, e.message, e)
        }
    }
} 