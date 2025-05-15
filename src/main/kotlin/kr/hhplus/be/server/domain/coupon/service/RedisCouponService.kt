package kr.hhplus.be.server.domain.coupon.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.CompletableFuture
import org.slf4j.LoggerFactory

@Service
class RedisCouponService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val stringRedisTemplate: StringRedisTemplate,
    private val couponService: CouponService,
    private val couponIssueScript: RedisScript<Any>
) {
    private val log = LoggerFactory.getLogger(javaClass)
    
    /**
     * 쿠폰 초기화 (관리자용)
     * 
     * @param couponCode 쿠폰 코드
     * @param quantity 발급 가능 수량
     * @param info 쿠폰 부가 정보
     */
    fun initializeCoupon(couponCode: String, quantity: Int, info: Map<String, Any>) {
        val stockKey = "coupon:stock:$couponCode"
        val infoKey = "coupon:info:$couponCode"
        val issuedKey = "coupon:issued:$couponCode"
        
        // 기존 데이터 삭제
        redisTemplate.delete(listOf(stockKey, infoKey, issuedKey))
        
        // 재고 초기화 - UUID 목록을 List에 추가
        val couponIds = (1..quantity).map { UUID.randomUUID().toString() }
        stringRedisTemplate.opsForList().rightPushAll(stockKey, couponIds)
        
        // 정보 저장
        val couponInfo = HashMap(info)
        couponInfo["total_quantity"] = quantity.toString()
        couponInfo["remaining_quantity"] = quantity.toString()
        couponInfo["created_at"] = LocalDateTime.now().toString()
        
        stringRedisTemplate.opsForHash<String, String>().putAll(infoKey, couponInfo as Map<String, String>)
        
        // TTL 설정 (예: 7일)
        stringRedisTemplate.expire(stockKey, Duration.ofDays(7))
        stringRedisTemplate.expire(infoKey, Duration.ofDays(7))
        stringRedisTemplate.expire(issuedKey, Duration.ofDays(7))
        
        log.info("쿠폰 초기화 완료: code=$couponCode, quantity=$quantity")
    }
    
    /**
     * 선착순 쿠폰 발급 시도
     * 
     * @param userId 사용자 ID
     * @param couponCode 쿠폰 코드
     * @return 발급 결과
     */
    fun tryIssueCoupon(userId: Long, couponCode: String): CouponIssueResult {
        try {
            // Lua 스크립트 실행
            val result = stringRedisTemplate.execute(
                couponIssueScript,
                listOf("coupon:$couponCode"),
                userId.toString()
            )
            
            // 결과 처리
            return when {
                result is Map<*, *> && result.containsKey("err") -> {
                    val error = result["err"] as String
                    log.info("쿠폰 발급 실패: userId=$userId, couponCode=$couponCode, reason=$error")
                    CouponIssueResult.Failure(error)
                }
                result is Map<*, *> && result.containsKey("ok") -> {
                    val couponId = result["ok"] as String
                    // DB에 영구 저장 (비동기 처리)
                    saveToDatabaseAsync(userId, couponCode, couponId)
                    log.info("쿠폰 발급 성공: userId=$userId, couponCode=$couponCode, couponId=$couponId")
                    CouponIssueResult.Success(couponId)
                }
                else -> {
                    log.error("쿠폰 발급 중 알 수 없는 오류: userId=$userId, couponCode=$couponCode")
                    CouponIssueResult.Failure("UNKNOWN_ERROR")
                }
            }
        } catch (e: Exception) {
            log.error("쿠폰 발급 처리 중 예외 발생", e)
            return CouponIssueResult.Failure("SYSTEM_ERROR")
        }
    }
    
    /**
     * 대기열에 사용자 추가
     * 
     * @param userId 사용자 ID
     * @param couponCode 쿠폰 코드
     * @return 대기 순번
     */
    fun addToWaitingQueue(userId: Long, couponCode: String): Int {
        val key = "coupon:waiting:$couponCode"
        val timestamp = System.currentTimeMillis()
        
        // 대기열에 추가 (Sorted Set)
        stringRedisTemplate.opsForZSet().add(key, userId.toString(), timestamp.toDouble())
        
        // 30분 후 만료 설정
        stringRedisTemplate.expire(key, Duration.ofMinutes(30))
        
        // 대기 순번 반환 (0-based index를 1-based로 변환)
        val rank = stringRedisTemplate.opsForZSet().rank(key, userId.toString())?.toInt()?.plus(1) ?: -1
        log.info("대기열 추가: userId=$userId, couponCode=$couponCode, rank=$rank")
        return rank
    }
    
    /**
     * 활성화된 모든 대기열 키 조회
     */
    fun findActiveWaitingQueues(): Set<String> {
        return stringRedisTemplate.keys("coupon:waiting:*") ?: emptySet()
    }
    
    /**
     * 대기열에서 상위 사용자 조회
     * 
     * @param waitingKey 대기열 키
     * @param count 조회할 사용자 수
     * @return 상위 사용자 ID 목록
     */
    fun getTopUsers(waitingKey: String, count: Int): List<Long> {
        val users = stringRedisTemplate.opsForZSet().range(waitingKey, 0, count.toLong() - 1)
        return users?.map { it.toLong() } ?: emptyList()
    }
    
    /**
     * 대기열에서 사용자 제거
     * 
     * @param waitingKey 대기열 키
     * @param userId 사용자 ID
     */
    fun removeFromWaitingQueue(waitingKey: String, userId: Long) {
        stringRedisTemplate.opsForZSet().remove(waitingKey, userId.toString())
        log.info("대기열에서 제거: userId=$userId, key=$waitingKey")
    }
    
    /**
     * 비동기로 DB에 저장
     */
    private fun saveToDatabaseAsync(userId: Long, couponCode: String, couponId: String) {
        CompletableFuture.runAsync {
            try {
                // 실제 쿠폰 정보 조회 및 유저 쿠폰 발급
                val coupon = couponService.findByCode(couponCode)
                
                // 유저 쿠폰 생성 및 저장
                couponService.createUserCoupon(
                    CouponCommand.CreateUserCouponCommand(
                        userId = userId,
                        couponId = coupon.id!!,
                        quantity = 1
                    )
                )
                
                log.info("DB 저장 완료: userId=$userId, couponCode=$couponCode")
            } catch (e: Exception) {
                log.error("쿠폰 DB 저장 실패: userId=$userId, couponCode=$couponCode", e)
                // 실패 시 Redis에서 롤백하는 보상 트랜잭션 구현 (필요 시)
                // rollbackRedisTransaction(userId, couponCode)
            }
        }
    }
    
    /**
     * 쿠폰 발급 가능 여부 확인
     */
    fun isCouponAvailable(couponCode: String): Boolean {
        val stockKey = "coupon:stock:$couponCode"
        val count = stringRedisTemplate.opsForList().size(stockKey) ?: 0
        return count > 0
    }
    
    /**
     * 사용자가 이미 발급받았는지 확인
     */
    fun hasUserIssuedCoupon(userId: Long, couponCode: String): Boolean {
        val issuedKey = "coupon:issued:$couponCode"
        return stringRedisTemplate.opsForSet().isMember(issuedKey, userId.toString()) ?: false
    }
    
    /**
     * 쿠폰 발급 현황 조회
     */
    fun getCouponStatus(couponCode: String): Map<String, Any> {
        val infoKey = "coupon:info:$couponCode"
        val stockKey = "coupon:stock:$couponCode"
        val issuedKey = "coupon:issued:$couponCode"
        
        val info = stringRedisTemplate.opsForHash<String, String>().entries(infoKey) ?: emptyMap()
        val remainingStock = stringRedisTemplate.opsForList().size(stockKey) ?: 0
        val issuedCount = stringRedisTemplate.opsForSet().size(issuedKey) ?: 0
        
        val result = HashMap<String, Any>(info)
        result["current_stock"] = remainingStock.toString()
        result["issued_count"] = issuedCount.toString()
        
        return result
    }
} 