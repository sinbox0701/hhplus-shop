package kr.hhplus.be.server.domain.ranking.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 상품 랭킹 관리 서비스
 * - Redis Sorted Set을 활용한 실시간 랭킹 관리
 */
@Service
class ProductRankingService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE
        private val WEEK_FORMATTER = DateTimeFormatter.ofPattern("YYYY-'W'w")
    }
    
    /**
     * 일간 랭킹 키 생성
     */
    fun getDailyRankingKey(date: LocalDate = LocalDate.now()): String {
        return "ranking:daily:${date.format(DATE_FORMATTER)}"
    }
    
    /**
     * 주간 랭킹 키 생성
     */
    fun getWeeklyRankingKey(date: LocalDate = LocalDate.now()): String {
        return "ranking:weekly:${date.format(WEEK_FORMATTER)}"
    }
    
    /**
     * 상품 랭킹 점수 증가 (일간, 주간 동시 적용)
     * 
     * @param productId 상품 ID
     * @param increment 증가값 (기본 1)
     */
    fun incrementProductScore(productId: Long, increment: Int = 1) {
        val dailyKey = getDailyRankingKey()
        val weeklyKey = getWeeklyRankingKey()
        
        // 일간 랭킹 증가
        redisTemplate.opsForZSet().incrementScore(dailyKey, productId.toString(), increment.toDouble())
        
        // 일간 랭킹 TTL 설정 (존재하지 않는 경우)
        if (redisTemplate.getExpire(dailyKey) == -1L) {
            redisTemplate.expire(dailyKey, Duration.ofDays(1))
        }
        
        // 주간 랭킹 증가
        redisTemplate.opsForZSet().incrementScore(weeklyKey, productId.toString(), increment.toDouble())
        
        // 주간 랭킹 TTL 설정 (존재하지 않는 경우)
        if (redisTemplate.getExpire(weeklyKey) == -1L) {
            redisTemplate.expire(weeklyKey, Duration.ofDays(7))
        }
    }
    
    /**
     * 상위 랭킹 상품 ID 조회 (일간)
     * 
     * @param date 조회 날짜 (기본 오늘)
     * @param limit 조회 개수 (기본 10개)
     * @return 상위 상품 ID 목록
     */
    fun getTopDailyProducts(date: LocalDate = LocalDate.now(), limit: Int = 10): List<Long> {
        val key = getDailyRankingKey(date)
        
        return redisTemplate.opsForZSet()
            .reverseRange(key, 0, limit - 1.toLong())
            ?.mapNotNull { it.toString().toLongOrNull() }
            ?: emptyList()
    }
    
    /**
     * 상위 랭킹 상품 ID 조회 (주간)
     * 
     * @param date 기준 날짜 (기본 오늘)
     * @param limit 조회 개수 (기본 10개)
     * @return 상위 상품 ID 목록
     */
    fun getTopWeeklyProducts(date: LocalDate = LocalDate.now(), limit: Int = 10): List<Long> {
        val key = getWeeklyRankingKey(date)
        
        return redisTemplate.opsForZSet()
            .reverseRange(key, 0, limit - 1.toLong())
            ?.mapNotNull { it.toString().toLongOrNull() }
            ?: emptyList()
    }
    
    /**
     * 특정 상품의 일간 랭킹 조회
     * 
     * @param productId 상품 ID
     * @param date 조회 날짜 (기본 오늘)
     * @return 랭킹 (1부터 시작, null은 랭킹 없음)
     */
    fun getProductDailyRank(productId: Long, date: LocalDate = LocalDate.now()): Long? {
        val key = getDailyRankingKey(date)
        val rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString())
        return rank?.plus(1) // 0-based 인덱스이므로 +1
    }
    
    /**
     * 특정 상품의 주간 랭킹 조회
     * 
     * @param productId 상품 ID
     * @param date 기준 날짜 (기본 오늘)
     * @return 랭킹 (1부터 시작, null은 랭킹 없음)
     */
    fun getProductWeeklyRank(productId: Long, date: LocalDate = LocalDate.now()): Long? {
        val key = getWeeklyRankingKey(date)
        val rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString())
        return rank?.plus(1) // 0-based 인덱스이므로 +1
    }
    
    /**
     * 랭킹 시스템 초기화 (매일 자정에 실행)
     * - 일간 랭킹 TTL 설정/갱신
     */
    fun initializeRankings() {
        val today = LocalDate.now()
        val dailyKey = getDailyRankingKey(today)
        
        // 일간 랭킹 TTL 설정/갱신
        redisTemplate.expire(dailyKey, Duration.ofDays(1))
    }
} 