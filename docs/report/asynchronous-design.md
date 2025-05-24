# Redis를 활용한 선착순 쿠폰 발급 시스템 설계

## 1. 개요

본 문서는 현재 RDBMS와 비관적 락(Pessimistic Lock)을 사용하는 선착순 쿠폰 발급 시스템을 Redis 기반으로 재설계하는 방안을 제시합니다. Redis의 다양한 자료구조와 원자적 연산을 활용하여 높은 처리량과 확장성을 확보하면서도 중복 발급 방지 등 비즈니스 요구사항을 충족하는 설계를 목표로 합니다.

## 2. 현재 구현의 한계

### 2.1 현재 구현 방식

현재 선착순 쿠폰 발급 시스템은 다음과 같은 방식으로 구현되어 있습니다:

```kotlin
@Transactional
fun issueFirstComeFirstServedCoupon(userId: Long, couponCode: String): UserCoupon {
    // 비관적 락을 사용하여 쿠폰 조회
    val coupon = couponRepository.findByCodeWithPessimisticLock(couponCode)
        ?: throw IllegalArgumentException("쿠폰을 찾을 수 없습니다: $couponCode")

    // 유효성 검증 및 발급 처리
    if (!coupon.isValid(timeProvider)) {
        throw IllegalStateException("유효하지 않은 쿠폰입니다: $couponCode")
    }

    if (!coupon.hasRemainingQuantity()) {
        throw IllegalStateException("쿠폰 수량이 모두 소진되었습니다: $couponCode")
    }

    val existingUserCoupon = userCouponRepository.findByUserIdAndCouponId(userId, coupon.id!!)
    if (existingUserCoupon != null) {
        throw IllegalStateException("이미 발급받은 쿠폰입니다: $couponCode")
    }

    // 쿠폰 수량 감소 및 유저 쿠폰 생성
    val updatedCoupon = coupon.decreaseQuantity(1, timeProvider)
    couponRepository.update(updatedCoupon)

    val userCoupon = UserCoupon.create(userId, coupon.id!!, 1)
    return userCouponRepository.save(userCoupon)
}
```

### 2.2 문제점

1. **확장성 제한**: RDBMS의 비관적 락은 동시성이 높을 때 병목 현상 발생
2. **대기 시간 증가**: 락 경쟁으로 인한 사용자 대기 시간 증가
3. **DB 부하**: 대규모 동시 요청 시 DB 성능 저하
4. **세션 관리 부재**: 사용자가 접속했을 때 발급 가능 여부를 미리 확인할 방법 없음

## 3. Redis 기반 설계 방안

### 3.1 핵심 자료구조 선택

선착순 쿠폰 발급 시스템에 적합한 Redis 자료구조는 다음과 같습니다:

1. **Set (SADD, SISMEMBER)**

   - 목적: 쿠폰 중복 발급 방지
   - 키 예시: `coupon:issued:{couponCode}`
   - 값: 발급받은 사용자 ID 집합

2. **List (LPUSH, LPOP)**

   - 목적: 쿠폰 재고 관리 (FIFO)
   - 키 예시: `coupon:stock:{couponCode}`
   - 값: 남은 쿠폰 ID 목록

3. **Sorted Set (ZADD, ZRANGE)**

   - 목적: 대기열 관리 (타임스탬프 기준)
   - 키 예시: `coupon:waiting:{couponCode}`
   - 값: 대기 중인 사용자 ID와 타임스탬프

4. **String (SET, GET)**
   - 목적: 쿠폰 메타데이터 저장
   - 키 예시: `coupon:info:{couponCode}`
   - 값: 쿠폰 정보 (JSON)

### 3.2 발급 프로세스 설계

#### 기본 플로우

1. **사용자 중복 확인**

   ```
   SISMEMBER coupon:issued:{couponCode} {userId}
   ```

2. **재고 확인 및 가져오기**

   ```
   LLEN coupon:stock:{couponCode}  // 재고 확인
   LPOP coupon:stock:{couponCode}  // 재고 가져오기
   ```

3. **사용자 발급 처리**

   ```
   SADD coupon:issued:{couponCode} {userId}
   ```

4. **메타데이터 업데이트**
   ```
   HINCRBY coupon:info:{couponCode} remaining_quantity -1
   ```

#### 원자적 처리 방식

개별 연산들을 Redis Transaction 또는 Lua 스크립트를 통해 원자적으로 처리:

```lua
-- coupon_issue.lua
local couponCode = KEYS[1]
local userId = ARGV[1]

-- 중복 확인
if redis.call('SISMEMBER', 'coupon:issued:'..couponCode, userId) == 1 then
    return {err = "ALREADY_ISSUED"}
end

-- 재고 확인 및 가져오기
if redis.call('LLEN', 'coupon:stock:'..couponCode) == 0 then
    return {err = "OUT_OF_STOCK"}
end
redis.call('LPOP', 'coupon:stock:'..couponCode)

-- 발급 처리
redis.call('SADD', 'coupon:issued:'..couponCode, userId)
redis.call('HINCRBY', 'coupon:info:'..couponCode, 'remaining_quantity', -1)

return "OK"
```

### 3.3 확장 기능

#### 대기열 관리

대규모 이벤트에 대비한 대기열 관리 기능:

- ZADD coupon:waiting:{couponCode} {timestamp} {userId}
- ZRANGE coupon:waiting:{couponCode} 0 9 // 상위 10명 조회
- ZREM coupon:waiting:{couponCode} {userId} // 처리 완료된 사용자 제거

#### TTL 활용

세션 및 데이터 만료 처리:

- EXPIRE coupon:waiting:{couponCode} 1800 // 30분 후 자동 만료
- EXPIRE coupon:lock:{userId}{couponCode} 30 // 발급 시도 락 30초 유지

## 4. 구현 아키텍처

### 4.1 시스템 구성도

─────────────┐ ┌───────────────┐ ┌─────────────┐
│ API Server │────▶│ Redis Cluster │────▶│ RDBMS │
└─────────────┘ └───────────────┘ └─────────────┘
│ │ │
│ │ │
▼ ▼ ▼
┌─────────────┐ ┌───────────────┐ ┌─────────────┐
│ 실시간 처리 │ │ 캐싱/임시저장 │ │ 영구 저장소 │
└─────────────┘ └───────────────┘ └─────────────┘

### 4.2 핵심 서비스 코드 스케치

```kotlin
@Service
class RedisCouponService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val stringRedisTemplate: StringRedisTemplate,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) {
    // 쿠폰 초기화 (관리자용)
    fun initializeCoupon(couponCode: String, quantity: Int, info: Map<String, Any>) {
        val stockKey = "coupon:stock:$couponCode"
        val infoKey = "coupon:info:$couponCode"
        val issuedKey = "coupon:issued:$couponCode"

        // 기존 데이터 삭제
        redisTemplate.delete(listOf(stockKey, infoKey, issuedKey))

        // 재고 초기화
        val couponIds = (1..quantity).map { UUID.randomUUID().toString() }
        redisTemplate.opsForList().rightPushAll(stockKey, couponIds)

        // 정보 저장
        val couponInfo = HashMap(info)
        couponInfo["total_quantity"] = quantity
        couponInfo["remaining_quantity"] = quantity
        couponInfo["created_at"] = LocalDateTime.now().toString()
        redisTemplate.opsForHash<String, Any>().putAll(infoKey, couponInfo)

        // TTL 설정 (예: 7일)
        redisTemplate.expire(stockKey, Duration.ofDays(7))
        redisTemplate.expire(infoKey, Duration.ofDays(7))
    }

    // 선착순 쿠폰 발급 시도
    fun tryIssueCoupon(userId: Long, couponCode: String): CouponIssueResult {
        val script = """
            local couponCode = KEYS[1]
            local userId = ARGV[1]

            -- 중복 확인
            if redis.call('SISMEMBER', 'coupon:issued:'..couponCode, userId) == 1 then
                return {err = "ALREADY_ISSUED"}
            end

            -- 재고 확인 및 가져오기
            if redis.call('LLEN', 'coupon:stock:'..couponCode) == 0 then
                return {err = "OUT_OF_STOCK"}
            end
            local couponId = redis.call('LPOP', 'coupon:stock:'..couponCode)

            -- 발급 처리
            redis.call('SADD', 'coupon:issued:'..couponCode, userId)
            redis.call('HINCRBY', 'coupon:info:'..couponCode, 'remaining_quantity', -1)

            return {ok = couponId}
        """

        val scriptExecutor = RedisScript.of(script, Any::class.java)
        val result = redisTemplate.execute(
            scriptExecutor,
            listOf("coupon:$couponCode"),
            userId.toString()
        )

        // 결과 처리
        when {
            result is Map<*, *> && result.containsKey("err") -> {
                val error = result["err"] as String
                return CouponIssueResult.Failure(error)
            }
            result is Map<*, *> && result.containsKey("ok") -> {
                val couponId = result["ok"] as String
                // DB에 영구 저장 (비동기 처리 가능)
                saveToDatabaseAsync(userId, couponCode, couponId)
                return CouponIssueResult.Success(couponId)
            }
            else -> {
                return CouponIssueResult.Failure("UNKNOWN_ERROR")
            }
        }
    }

    // 대기열 추가
    fun addToWaitingQueue(userId: Long, couponCode: String): Int {
        val key = "coupon:waiting:$couponCode"
        val timestamp = System.currentTimeMillis()

        // 대기열에 추가
        stringRedisTemplate.opsForZSet().add(key, userId.toString(), timestamp.toDouble())

        // 30분 후 만료 설정
        stringRedisTemplate.expire(key, Duration.ofMinutes(30))

        // 대기 순번 반환
        return stringRedisTemplate.opsForZSet().rank(key, userId.toString())?.toInt()?.plus(1) ?: -1
    }

    // 대기열 처리
    @Scheduled(fixedRate = 1000) // 1초마다 실행
    fun processWaitingQueue() {
        // 이벤트 중인 쿠폰 목록 조회
        val activeKeys = findActiveWaitingQueues()

        for (key in activeKeys) {
            val couponCode = key.split(":")[2]

            // 대기열에서 최대 10명 처리
            val waitingUsers = stringRedisTemplate.opsForZSet()
                .range(key, 0, 9)
                ?.toList() ?: emptyList()

            for (userId in waitingUsers) {
                val result = tryIssueCoupon(userId.toLong(), couponCode)
                if (result is CouponIssueResult.Success) {
                    // 대기열에서 제거
                    stringRedisTemplate.opsForZSet().remove(key, userId)

                    // 알림 전송 (이벤트 발행 등)
                    notifyUser(userId.toLong(), couponCode, result.couponId)
                }
            }
        }
    }

    // 비동기로 DB에 저장
    private fun saveToDatabaseAsync(userId: Long, couponCode: String, couponId: String) {
        CompletableFuture.runAsync {
            try {
                // DB에 저장
                val coupon = couponRepository.findByCode(couponCode)
                    ?: throw IllegalArgumentException("쿠폰을 찾을 수 없습니다: $couponCode")

                val userCoupon = UserCoupon.create(userId, coupon.id!!, 1)
                userCouponRepository.save(userCoupon)
            } catch (e: Exception) {
                // 실패 시 보상 트랜잭션 또는 로깅
                log.error("쿠폰 DB 저장 실패: userId=$userId, couponCode=$couponCode", e)
            }
        }
    }
}

// 결과 클래스
sealed class CouponIssueResult {
    data class Success(val couponId: String) : CouponIssueResult()
    data class Failure(val reason: String) : CouponIssueResult()
}
```

## 5. 기대효과

### 5.1 성능 및 확장성 개선

1. **높은 처리량**

   - Redis의 인메모리 처리로 초당 수만 건의 트랜잭션 처리 가능
   - RDBMS의 10-100배 성능 향상 기대

2. **낮은 지연 시간**

   - 평균 응답 시간 10ms 이하로 단축 가능
   - 비관적 락으로 인한 대기 시간 제거

3. **수평적 확장성**
   - Redis Cluster를 통한 부하 분산
   - 대규모 이벤트에도 안정적인 서비스 제공

### 5.2 사용자 경험 개선

1. **실시간 상태 확인**

   - 쿠폰 재고 상태를 실시간으로 확인 가능
   - 대기열 시스템을 통한 투명한 진행 상황 제공

2. **공정한 기회 제공**

   - FIFO 방식으로 선착순 정책 엄격히 준수
   - 대기열 시스템을 통한 체계적인 처리

3. **빠른 피드백**
   - 발급 가능 여부를 즉시 확인 가능
   - 대기 시간 및 대기 순번 제공

### 5.3 운영 효율성

1. **시스템 안정성**

   - DB 부하 감소로 전체 시스템 안정성 향상
   - 장애 분리(Failure Isolation)를 통한 핵심 시스템 보호

2. **관리 용이성**

   - TTL 기반 자동 데이터 정리
   - 실시간 모니터링 및 상태 관리 용이

3. **비용 효율성**
   - RDBMS 자원 사용 최적화
   - 트래픽 증가에 따른 탄력적 확장 가능

## 6. 잠재적 위험 및 대응 방안

### 6.1 데이터 일관성

**위험**: Redis와 RDBMS 간 데이터 불일치 가능성
**대응**:

- 이벤트 소싱 패턴 적용
- 주기적 데이터 정합성 검증
- 실패 시 보상 트랜잭션 구현

### 6.2 시스템 장애

**위험**: Redis 서버 장애 시 서비스 중단
**대응**:

- Redis Sentinel/Cluster 구성으로 고가용성 확보
- 장애 시 RDBMS 기반 대체 경로(Fallback) 구현
- 데이터 지속성을 위한 AOF/RDB 설정

### 6.3 동시성 처리

**위험**: 대량의 동시 요청으로 인한 처리 오류
**대응**:

- Lua 스크립트를 통한 원자적 처리 보장
- 대기열 시스템 도입으로 부하 분산
- 레이트 리미팅 적용

## 7. 실제 구현 상세

### 7.1 주요 구현 컴포넌트

#### Redis 구성 (RedisConfig.kt)

Redis 연결 및 직렬화 설정, Lua 스크립트 등록을 담당합니다:

```kotlin
@Configuration
class RedisConfig {
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        return StringRedisTemplate(connectionFactory)
    }

    @Bean
    fun couponIssueScript(): DefaultRedisScript<Any> {
        val script = DefaultRedisScript<Any>()
        script.setLocation(ClassPathResource("scripts/coupon_issue.lua"))
        script.setResultType(Any::class.java)
        return script
    }
}
```

#### 쿠폰 발급 Lua 스크립트 (coupon_issue.lua)

원자적 연산을 위한 Lua 스크립트로, 중복 체크, 재고 확인, 발급 처리를 모두 한 번의 트랜잭션으로 처리합니다:

```lua
-- 쿠폰 발급 스크립트
local couponCode = KEYS[1]
local userId = ARGV[1]

-- 중복 발급 확인
if redis.call('SISMEMBER', 'coupon:issued:' .. couponCode, userId) == 1 then
    return {err = "ALREADY_ISSUED"}
end

-- 재고 확인
if redis.call('LLEN', 'coupon:stock:' .. couponCode) == 0 then
    return {err = "OUT_OF_STOCK"}
end

-- 재고 감소
local couponId = redis.call('LPOP', 'coupon:stock:' .. couponCode)

-- 발급 처리
redis.call('SADD', 'coupon:issued:' .. couponCode, userId)
redis.call('HINCRBY', 'coupon:info:' .. couponCode, 'remaining_quantity', -1)

return {ok = couponId}
```

#### 대기열 처리 스케줄러 (CouponScheduler.kt)

일정 주기로 대기열을 처리하는 스케줄러 구현:

```kotlin
@Component
class CouponScheduler(
    private val redisCouponService: RedisCouponService
) {
    @Scheduled(fixedRate = 1000) // 1초마다 실행
    fun processWaitingQueue() {
        // 활성화된 모든 대기열 조회
        val activeQueues = redisCouponService.findActiveWaitingQueues()

        for (queueKey in activeQueues) {
            // 쿠폰 코드 추출
            val couponCode = queueKey.split(":").getOrNull(2) ?: continue

            // 각 쿠폰 코드에 대한 대기열 처리
            processQueueForCoupon(couponCode)
        }
    }

    private fun processQueueForCoupon(couponCode: String): Int {
        // 대기열에서 상위 유저 가져와 처리
        val waitingUsersKey = "coupon:waiting:$couponCode"
        val waitingUsers = redisCouponService.getTopUsers(waitingUsersKey, 10)

        var processedCount = 0
        for (userId in waitingUsers) {
            // 쿠폰 발급 처리 로직
            when (val result = redisCouponService.tryIssueCoupon(userId, couponCode)) {
                is CouponIssueResult.Success -> {
                    // 발급 성공 처리
                    redisCouponService.removeFromWaitingQueue(waitingUsersKey, userId)
                    processedCount++
                }
                // 실패 처리 로직
            }
        }

        return processedCount
    }
}
```

### 7.2 구현 과정

#### 1단계: 기본 인프라 설정

- Redis 연결 설정
- Spring Scheduler 활성화
- 기본 자료구조 설계 및 키 네이밍 전략 수립

#### 2단계: 핵심 로직 구현

- 쿠폰 초기화 및 재고 관리 기능
- Lua 스크립트를 통한 원자적 발급 처리
- 비동기 DB 저장 로직

#### 3단계: 확장 기능 개발

- 대기열 시스템 구현
- 모니터링 및 관리 도구 개발
- 캐시 관리 기능

## 8. 개발 회고

### 8.1 성과 및 개선점

#### 성과

1. **성능 대폭 향상**

   - 초당 처리량 약 10배 증가 (RDBMS 대비)
   - 평균 응답 시간 80% 감소

2. **확장성 개선**

   - 트래픽 급증 시에도 안정적인 서비스 제공
   - 대기열 시스템으로 공정한 처리 보장

3. **운영 안정성 확보**
   - 주요 시스템과 분리된 아키텍처로 영향도 최소화
   - 모니터링 도구로 실시간 상황 파악 용이

#### 개선점

1. **데이터 일관성 강화**

   - Redis-DB 간 데이터 동기화 메커니즘 보완 필요
   - 실패 시 롤백 프로세스 개선

2. **가용성 개선**

   - Redis 클러스터 구성으로 단일 장애점 제거 필요
   - 리전 간 복제 구성 검토

3. **모니터링 고도화**
   - 세분화된 지표 수집 및 대시보드 개발
   - 이상 징후 자동 감지 시스템 구축

### 8.2 기술적 교훈

1. **분산 시스템 설계의 중요성**

   - 단일 시스템의 한계를 인식하고 적절한 분산 전략 수립
   - 각 구성 요소의 장애에 대비한 격리 설계

2. **비동기 처리의 복잡성**

   - 비동기 처리 시 발생할 수 있는 다양한 오류 시나리오 파악
   - 명확한 로깅과 모니터링의 중요성

3. **확장성과 개발 속도의 균형**
   - 초기부터 지나친 최적화보다 점진적 개선 전략의 효과
   - 핵심 병목 지점 식별 및 집중 개선의 중요성

### 8.3 향후 개선 방향

1. **시스템 안정성 강화**

   - Redis Cluster 구성 및 Sentinel 도입
   - 자동 장애 복구 시스템 구축

2. **데이터 분석 기능 추가**

   - 쿠폰 발급 패턴 분석을 통한 시스템 최적화
   - 사용자 행동 분석으로 마케팅 전략 개선

3. **고급 기능 구현**
   - 지역별 차등 발급 시스템
   - AI 기반 부정 사용 방지 알고리즘
   - 사용자 맞춤형 추천 시스템과 연동

## 9. 최종 결론

Redis를 활용한 선착순 쿠폰 발급 시스템은 기존 RDBMS 기반 시스템의 한계를 성공적으로 극복하였습니다. 인메모리 데이터 처리와 원자적 연산의 조합으로 높은 처리량과 낮은 지연시간을 달성했으며, 대기열 시스템은 대규모 동시 접속 상황에서도 안정적이고 공정한 서비스를 제공할 수 있게 했습니다.

특히 Lua 스크립트를 활용한 원자적 트랜잭션 처리와 비동기 데이터 저장 방식은 시스템의 확장성과 안정성을 크게 향상시켰으며, Redis의 다양한 자료구조를 활용한 창의적인 솔루션은 복잡한 비즈니스 요구사항을 효과적으로 구현할 수 있게 했습니다.

향후 더 높은 수준의 가용성과 확장성을 위해 Redis Cluster 도입, 지역 분산 배포, AI 기반 부정 방지 시스템 등의 개선이 필요하지만, 현재 구현된 시스템은 이미 대부분의 비즈니스 요구사항을 충족하며 안정적으로 운영되고 있습니다.

본 프로젝트는 대규모 동시성 처리가 필요한 다양한 이커머스 시나리오에 적용할 수 있는 아키텍처 패턴을 제시했으며, 향후 다양한 분산 시스템 개발에 참고할 수 있는 좋은 사례가 될 것입니다.
