package kr.hhplus.be.server.interfaces.order.event

import kr.hhplus.be.server.domain.order.event.OrderEvent
import kr.hhplus.be.server.domain.user.service.AccountCommand
import kr.hhplus.be.server.domain.user.service.AccountService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 주문 이벤트를 구독하여 계좌 관련 처리를 수행하는 리스너
 */
@Component
class AccountOrderEventListener(
    private val accountService: AccountService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 주문 완료 이벤트 처리 - 계좌 금액 차감
     * - 트랜잭션 완료 후 이벤트 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Completed::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional
    fun handleOrderCompleted(event: OrderEvent.Completed) {
        try {
            log.info("계좌 금액 차감 시작: orderId={}, userId={}, amount={}", 
                event.orderId, event.userId, event.totalPrice)
            
            // 계좌 조회
            val account = accountService.findByUserId(event.userId)
            
            // 잔액 확인
            if (account.amount < event.totalPrice) {
                throw IllegalStateException("계좌 잔액이 부족합니다: 현재 잔액=${account.amount}, 주문 금액=${event.totalPrice}")
            }
            
            // 계좌에서 금액 차감
            accountService.withdraw(AccountCommand.UpdateAccountCommand(
                id = account.id!!,
                amount = event.totalPrice
            ))
            
            log.info("계좌 금액 차감 완료: orderId={}, userId={}, amount={}", 
                event.orderId, event.userId, event.totalPrice)
        } catch (e: Exception) {
            log.error("계좌 금액 차감 실패: orderId={}, userId={}, 사유={}", 
                event.orderId, event.userId, e.message, e)
            
            // 계좌 차감 실패 이벤트 발행 - 주문 취소를 위한 보상 트랜잭션 유도
            applicationEventPublisher.publishEvent(
                AccountEvent.WithdrawFailed(
                    orderId = event.orderId,
                    userId = event.userId,
                    reason = e.message ?: "Unknown error"
                )
            )
        }
    }
    
    /**
     * 주문 취소 이벤트 처리 - 계좌 금액 환불
     * - 이전 상태가 COMPLETED인 경우에만 환불 처리
     */
    @TransactionalEventListener(
        classes = [OrderEvent.Cancelled::class],
        phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional
    fun handleOrderCancelled(event: OrderEvent.Cancelled) {
        // 이전 상태가 COMPLETED인 경우에만 환불 처리
        if (event.previousStatus != kr.hhplus.be.server.domain.order.model.OrderStatus.COMPLETED) {
            return
        }
        
        try {
            log.info("계좌 금액 환불 시작: orderId={}, userId={}, amount={}", 
                event.orderId, event.userId, event.totalPrice)
            
            // 계좌 조회
            val account = accountService.findByUserId(event.userId)
            
            // 환불 처리 (계좌에 금액 환불)
            accountService.charge(AccountCommand.UpdateAccountCommand(
                id = account.id!!,
                amount = event.totalPrice
            ))
            
            log.info("계좌 금액 환불 완료: orderId={}, userId={}, amount={}", 
                event.orderId, event.userId, event.totalPrice)
        } catch (e: Exception) {
            log.error("계좌 금액 환불 실패: orderId={}, userId={}, 사유={}", 
                event.orderId, event.userId, e.message, e)
        }
    }
}

/**
 * 계좌 관련 이벤트
 */
sealed class AccountEvent {
    /**
     * 계좌 금액 차감 실패 이벤트
     */
    data class WithdrawFailed(
        val orderId: Long,
        val userId: Long,
        val reason: String
    ): AccountEvent()
} 