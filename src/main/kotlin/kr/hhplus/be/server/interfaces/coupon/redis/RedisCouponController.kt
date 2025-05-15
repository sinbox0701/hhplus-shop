package kr.hhplus.be.server.interfaces.coupon.redis

import kr.hhplus.be.server.domain.coupon.service.CouponIssueResult
import kr.hhplus.be.server.domain.coupon.service.RedisCouponService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/redis-coupons")
class RedisCouponController(
    private val redisCouponService: RedisCouponService
) {
    /**
     * 쿠폰 초기화
     */
    @PostMapping("/initialize")
    fun initializeCoupon(
        @RequestParam couponCode: String,
        @RequestParam quantity: Int,
        @RequestBody info: Map<String, Any>
    ): ResponseEntity<Any> {
        redisCouponService.initializeCoupon(couponCode, quantity, info)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "couponCode" to couponCode,
                "quantity" to quantity,
                "message" to "쿠폰이 초기화되었습니다."
            )
        )
    }
    
    /**
     * 쿠폰 발급 시도
     */
    @PostMapping("/{couponCode}/issue")
    fun issueCoupon(
        @PathVariable couponCode: String,
        @RequestParam userId: Long
    ): ResponseEntity<Any> {
        val result = redisCouponService.tryIssueCoupon(userId, couponCode)
        
        return when (result) {
            is CouponIssueResult.Success -> {
                ResponseEntity.ok(
                    mapOf(
                        "success" to true,
                        "couponId" to result.couponId,
                        "message" to "쿠폰이 성공적으로 발급되었습니다."
                    )
                )
            }
            is CouponIssueResult.Failure -> {
                val statusCode = when (result.reason) {
                    "ALREADY_ISSUED" -> 409 // Conflict
                    "OUT_OF_STOCK" -> 410 // Gone
                    else -> 400 // Bad Request
                }
                
                val message = when (result.reason) {
                    "ALREADY_ISSUED" -> "이미 발급받은 쿠폰입니다."
                    "OUT_OF_STOCK" -> "쿠폰 수량이 모두 소진되었습니다."
                    else -> "쿠폰 발급에 실패했습니다: ${result.reason}"
                }
                
                ResponseEntity.status(statusCode).body(
                    mapOf(
                        "success" to false,
                        "error" to result.reason,
                        "message" to message
                    )
                )
            }
        }
    }
    
    /**
     * 대기열 추가
     */
    @PostMapping("/{couponCode}/wait")
    fun addToWaitingQueue(
        @PathVariable couponCode: String,
        @RequestParam userId: Long
    ): ResponseEntity<Any> {
        val rank = redisCouponService.addToWaitingQueue(userId, couponCode)
        
        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "rank" to rank,
                "message" to "대기열에 추가되었습니다. 현재 대기 순번: $rank"
            )
        )
    }
    
    /**
     * 쿠폰 발급 가능 여부 확인
     */
    @GetMapping("/{couponCode}/available")
    fun checkCouponAvailability(
        @PathVariable couponCode: String,
        @RequestParam userId: Long
    ): ResponseEntity<Any> {
        val isAvailable = redisCouponService.isCouponAvailable(couponCode)
        val hasIssued = redisCouponService.hasUserIssuedCoupon(userId, couponCode)
        
        return ResponseEntity.ok(
            mapOf(
                "couponCode" to couponCode,
                "available" to isAvailable,
                "alreadyIssued" to hasIssued,
                "message" to when {
                    hasIssued -> "이미 발급받은 쿠폰입니다."
                    !isAvailable -> "쿠폰 수량이 모두 소진되었습니다."
                    else -> "발급 가능한 쿠폰입니다."
                }
            )
        )
    }
    
    /**
     * 쿠폰 상태 조회
     */
    @GetMapping("/{couponCode}/status")
    fun getCouponStatus(
        @PathVariable couponCode: String
    ): ResponseEntity<Any> {
        val status = redisCouponService.getCouponStatus(couponCode)
        return ResponseEntity.ok(status)
    }
    
    /**
     * 대기열 조회
     */
    @GetMapping("/waiting-queues")
    fun getActiveWaitingQueues(): ResponseEntity<Any> {
        val queues = redisCouponService.findActiveWaitingQueues()
        return ResponseEntity.ok(
            mapOf(
                "queues" to queues,
                "count" to queues.size
            )
        )
    }
} 