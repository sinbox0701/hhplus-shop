package kr.hhplus.be.server.scheduler

import kr.hhplus.be.server.domain.coupon.service.CouponIssueResult
import kr.hhplus.be.server.domain.coupon.service.RedisCouponService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CouponScheduler(
    private val redisCouponService: RedisCouponService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 대기열 처리 스케줄러
     * 1초마다 대기 중인 사용자를 처리합니다.
     */
    @Scheduled(fixedRate = 1000) // 1초마다 실행
    fun processWaitingQueue() {
        try {
            // 모든 활성 대기열 키 조회
            val activeQueues = redisCouponService.findActiveWaitingQueues()
            if (activeQueues.isEmpty()) {
                return
            }
            
            log.info("활성 대기열 처리 시작 - 대기열 수: {}", activeQueues.size)
            
            for (queueKey in activeQueues) {
                // 쿠폰 코드 추출 (예: "coupon:waiting:CODE123" -> "CODE123")
                val couponCode = queueKey.split(":").getOrNull(2) ?: continue
                
                // 대기열에서 최대 10명 처리
                val processCount = processQueueForCoupon(couponCode)
                
                if (processCount > 0) {
                    log.info("쿠폰 대기열 처리 완료 - 코드: {}, 처리 인원: {}", couponCode, processCount)
                }
            }
        } catch (e: Exception) {
            log.error("대기열 처리 중 오류 발생", e)
        }
    }
    
    /**
     * 특정 쿠폰 코드에 대한 대기열 처리
     */
    private fun processQueueForCoupon(couponCode: String): Int {
        var processedCount = 0
        
        // 쿠폰 재고 확인
        if (!redisCouponService.isCouponAvailable(couponCode)) {
            return 0
        }
        
        // 대기열에서 상위 10명 조회
        val waitingUsersKey = "coupon:waiting:$couponCode"
        val waitingUsers = redisCouponService.getTopUsers(waitingUsersKey, 10)
        
        for (userId in waitingUsers) {
            // 이미 발급받은 사용자 건너뛰기
            if (redisCouponService.hasUserIssuedCoupon(userId, couponCode)) {
                redisCouponService.removeFromWaitingQueue(waitingUsersKey, userId)
                continue
            }
            
            // 쿠폰 발급 시도
            when (val result = redisCouponService.tryIssueCoupon(userId, couponCode)) {
                is CouponIssueResult.Success -> {
                    // 발급 성공 시 대기열에서 제거
                    redisCouponService.removeFromWaitingQueue(waitingUsersKey, userId)
                    processedCount++
                    
                    // 알림 전송 (실제로는 이벤트 발행 등으로 구현)
                    notifyUser(userId, couponCode, result.couponId)
                }
                is CouponIssueResult.Failure -> {
                    when (result.reason) {
                        "OUT_OF_STOCK" -> {
                            // 재고 소진 시 처리 중단
                            return processedCount
                        }
                        "ALREADY_ISSUED" -> {
                            // 이미 발급 받은 경우 대기열에서 제거
                            redisCouponService.removeFromWaitingQueue(waitingUsersKey, userId)
                        }
                        else -> {
                            // 기타 오류는 다음 스케줄에서 재시도
                            log.warn("쿠폰 발급 중 오류 발생 - 코드: {}, 사용자: {}, 이유: {}", 
                                couponCode, userId, result.reason)
                        }
                    }
                }
            }
            
            // 쿠폰 재고 확인, 소진 시 중단
            if (!redisCouponService.isCouponAvailable(couponCode)) {
                break
            }
        }
        
        return processedCount
    }
    
    /**
     * 사용자에게 알림 전송 (Mock 구현)
     */
    private fun notifyUser(userId: Long, couponCode: String, couponId: String) {
        // 실제 구현에서는 이벤트 발행, 푸시 알림, 이메일 등으로 사용자에게 알림
        log.info("사용자 알림 전송 - 사용자: {}, 쿠폰 코드: {}, 쿠폰 ID: {}", userId, couponCode, couponId)
    }
} 