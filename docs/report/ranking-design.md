# Redis를 활용한 실시간 랭킹 시스템 설계

## 1. 개요

본 문서는 Redis의 Sorted Set 자료구조를 활용하여 효율적이고 확장성 있는 실시간 랭킹 시스템을 설계하는 방법에 대해 설명합니다. 특히 상품 인기도, 판매량 등의 데이터를 기반으로 일간/주간 랭킹을 효과적으로 구현하는 방안을 제시합니다.

## 2. 설계 목표

- 실시간 데이터 반영이 가능한 랭킹 시스템 구축
- 높은 읽기/쓰기 성능 확보
- 일간/주간 등 기간별 랭킹 자동 관리
- 시스템 부하 최소화 및 확장성 확보
- 동시성 제어 용이성 확보

## 3. 기술 스택

- **Redis**: 고성능 인메모리 데이터 저장소, Sorted Set 자료구조 활용
- **Spring Boot**: 애플리케이션 프레임워크
- **Spring Data Redis**: Redis 연동 및 추상화 라이브러리

## 4. 핵심 설계 방안

### 4.1 데이터 모델링

#### 키 설계 전략

```
ranking:daily:{date}      // 예: ranking:daily:20240430
ranking:weekly:{week}     // 예: ranking:weekly:2024-W18
ranking:monthly:{month}   // 예: ranking:monthly:2024-04
```

#### 데이터 구조

- **Sorted Set**: 상품 ID를 멤버로, 판매량/조회수를 점수로 저장
- **TTL(Time To Live)**: 자동 만료 기능 활용

### 4.2 핵심 구현 방법

#### 일간 랭킹 업데이트

```redis
// 상품 123의 판매/조회수 증가
ZINCRBY ranking:daily:20240430 1 product123

// 일간 랭킹 TTL 설정 (1일 후 만료)
EXPIRE ranking:daily:20240430 86400
```

#### 주간 랭킹 업데이트

```redis
// 상품 123의 판매/조회수 증가
ZINCRBY ranking:weekly:2024-W18 1 product123

// 주간 랭킹 TTL 설정 (7일 후 만료)
EXPIRE ranking:weekly:2024-W18 604800
```

#### 랭킹 조회

```redis
// 상위 10개 상품 ID 조회
ZREVRANGE ranking:daily:20240430 0 9

// 특정 상품의 순위 조회
ZREVRANK ranking:daily:20240430 product123
```

### 4.3 시스템 통합 설계

1. **주문 프로세스 연동**:

   - 주문 완료 시 Redis 랭킹 데이터 업데이트
   - 배치 프로세스가 아닌 실시간 반영

2. **데이터 일관성 관리**:

   - 자정 배치 작업으로 새 키 생성 및 TTL 설정
   - DB와 Redis 간 동기화 메커니즘 구현

3. **캐싱 전략**:
   - 빈번한 랭킹 조회 결과 캐싱
   - 주기적 갱신으로 데이터 신선도 유지

## 5. 구현 세부 사항

### 5.1 랭킹 데이터 업데이트 로직

```kotlin
// 상품 조회/구매 시 랭킹 업데이트
fun updateProductRanking(productId: Long, increment: Int = 1) {
    val today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
    val redisKey = "ranking:daily:$today"

    // 랭킹 점수 증가
    redisTemplate.opsForZSet().incrementScore(redisKey, productId.toString(), increment.toDouble())

    // TTL 설정 (키가 없을 경우에만)
    if (redisTemplate.getExpire(redisKey) == -1L) {
        redisTemplate.expire(redisKey, Duration.ofDays(1))
    }
}
```

### 5.2 랭킹 조회 API

```kotlin
// 일간 상위 상품 조회
fun getTopSellingProducts(days: Int = 1, limit: Int = 10): List<Product> {
    val date = LocalDate.now().minusDays(days - 1L).format(DateTimeFormatter.BASIC_ISO_DATE)
    val redisKey = "ranking:daily:$date"

    // Redis에서 상위 상품 ID 조회
    val topProductIds = redisTemplate.opsForZSet()
        .reverseRange(redisKey, 0, limit - 1)
        ?.mapNotNull { it.toString().toLongOrNull() }
        ?: emptyList()

    // 상품 정보 조회
    return if (topProductIds.isNotEmpty()) {
        productRepository.findAllById(topProductIds)
    } else {
        productRepository.findTop(limit)
    }
}
```

### 5.3 자동화 배치 프로세스

```kotlin
@Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
fun initializeDailyRanking() {
    val today = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
    val redisKey = "ranking:daily:$today"

    // 새 키 생성 (필요 시 초기화)
    redisTemplate.expire(redisKey, Duration.ofDays(1))

    // 캐시 갱신을 위한 인기 상품 미리 로드
    refreshBestSellersCache()
}
```

## 6. 예상 기대효과

### 6.1 성능 향상

- **실시간 처리**: 주문/조회 데이터가 즉시 랭킹에 반영
- **응답 시간 개선**: 평균 응답 시간 10ms 이내 목표
- **DB 부하 감소**: 랭킹 쿼리의 DB 부하 90% 이상 감소

### 6.2 확장성

- **트래픽 대응**: 초당 수천 건의 업데이트 처리 가능
- **데이터 증가**: 상품 수 증가에도 성능 저하 없음
- **기능 확장**: 카테고리별, 지역별 랭킹 등 확장 용이

### 6.3 비즈니스 가치

- **사용자 경험**: 실시간 인기 상품 추천으로 전환율 향상
- **데이터 인사이트**: 실시간 트렌드 파악 가능
- **마케팅 활용**: 인기 상품 프로모션 전략 수립 기반

## 7. 고려사항 및 제한점

### 7.1 메모리 관리

- Redis 인스턴스 메모리 한계 고려
- 데이터 크기에 따른 샤딩 전략 필요

### 7.2 동일 점수 처리

- 동일 판매량/조회수에 대한 정렬 기준 정의
- 타임스탬프 조합을 통한 고유 점수 생성 고려

### 7.3 장애 대응

- Redis 장애 시 대체 조회 메커니즘 구현
- 데이터 지속성 보장을 위한 RDB/AOF 설정

## 8. 결론

Redis Sorted Set을 활용한 실시간 랭킹 시스템은 높은 성능과 확장성을 제공하며, 사용자 경험 향상과 비즈니스 인사이트 확보에 큰 기여를 할 것으로 기대됩니다. 적절한 키 설계와 TTL 관리를 통해 시스템 부하를 최소화하면서도 최신 데이터를 빠르게 제공할 수 있는 아키텍처를 구현할 수 있습니다.

## 9. 구현 과정

실제 설계 문서를 바탕으로 다음과 같은 단계로 랭킹 시스템을 구현했습니다.

### 9.1 Redis 환경 설정

먼저 Redis 연동을 위한 기본 설정을 추가했습니다.

1. **Redis 의존성 확인**

   ```kotlin
   // build.gradle.kts
   dependencies {
       // Redis & Distributed Lock
       implementation("org.springframework.boot:spring-boot-starter-data-redis")
       implementation("org.redisson:redisson-spring-boot-starter:3.24.3")
   }
   ```

2. **Redis 설정 클래스 구현**

   ```kotlin
   // RedisConfig.kt
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
   }
   ```

3. **application.yml 설정**
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
         timeout: 3000
     cache:
       type: redis
       redis:
         time-to-live: 3600000 # 1시간(ms)
         cache-null-values: false
   ```

### 9.2 랭킹 서비스 구현

Redis Sorted Set을 활용한 핵심 랭킹 서비스를 구현했습니다.

```kotlin
// ProductRankingService.kt
@Service
class ProductRankingService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    // 일간/주간 랭킹 키 생성 로직
    fun getDailyRankingKey(date: LocalDate = LocalDate.now()): String {
        return "ranking:daily:${date.format(DATE_FORMATTER)}"
    }

    fun getWeeklyRankingKey(date: LocalDate = LocalDate.now()): String {
        return "ranking:weekly:${date.format(WEEK_FORMATTER)}"
    }

    // 상품 랭킹 점수 증가 로직
    fun incrementProductScore(productId: Long, increment: Int = 1) {
        val dailyKey = getDailyRankingKey()
        val weeklyKey = getWeeklyRankingKey()

        // 일간 랭킹 증가 및 TTL 설정
        redisTemplate.opsForZSet().incrementScore(dailyKey, productId.toString(), increment.toDouble())
        if (redisTemplate.getExpire(dailyKey) == -1L) {
            redisTemplate.expire(dailyKey, Duration.ofDays(1))
        }

        // 주간 랭킹 증가 및 TTL 설정
        redisTemplate.opsForZSet().incrementScore(weeklyKey, productId.toString(), increment.toDouble())
        if (redisTemplate.getExpire(weeklyKey) == -1L) {
            redisTemplate.expire(weeklyKey, Duration.ofDays(7))
        }
    }

    // 상위 랭킹 상품 조회 로직
    fun getTopDailyProducts(date: LocalDate = LocalDate.now(), limit: Int = 10): List<Long> {
        val key = getDailyRankingKey(date)
        return redisTemplate.opsForZSet()
            .reverseRange(key, 0, limit - 1.toLong())
            ?.mapNotNull { it.toString().toLongOrNull() }
            ?: emptyList()
    }

    // 기타 랭킹 조회 메서드들...
}
```

### 9.3 주문 처리와 연동

주문 완료 시 랭킹 데이터를 업데이트하도록 OrderFacade에 로직을 추가했습니다.

```kotlin
// OrderFacade.kt
@Transactional
fun completeOrder(orderId: Long) {
    // 주문 상태 확인 및 업데이트
    val order = orderService.getOrder(orderId)
    if (order.status != OrderStatus.PENDING) {
        throw IllegalStateException("완료할 수 없는 주문 상태입니다: ${order.status}")
    }

    // 주문 상태 완료로 변경
    orderService.updateOrderStatus(OrderCommand.UpdateOrderStatusCommand(
        id = orderId,
        status = OrderStatus.COMPLETED
    ))

    // 상품 랭킹 업데이트
    val orderItems = orderItemService.getByOrderId(orderId)
    orderItems.forEach { orderItem ->
        productRankingService.incrementProductScore(
            productId = orderItem.productId,
            increment = orderItem.quantity.toInt()
        )
    }
}
```

### 9.4 스케줄러 구현

매일 자정에 랭킹 데이터를 초기화하고, 정기적으로 캐시를 갱신하는 스케줄러를 구현했습니다.

```kotlin
// RankingScheduler.kt
@Component
class RankingScheduler(
    private val productRankingService: ProductRankingService
) {
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    fun initializeDailyRankings() {
        productRankingService.initializeRankings()
    }

    @Scheduled(fixedRate = 900000) // 15분마다
    fun refreshPopularProductsCache() {
        // 캐시 갱신 로직
    }
}

// 스케줄러 활성화
@Configuration
@EnableScheduling
class SchedulerConfig
```

### 9.5 API 엔드포인트 구현

랭킹 데이터를 외부에 제공하는 API 컨트롤러를 구현했습니다.

```kotlin
// RankingController.kt
@RestController
@RequestMapping("/api/rankings")
class RankingController(
    private val productRankingService: ProductRankingService,
    private val productFacade: ProductFacade
) {
    @GetMapping("/daily")
    fun getDailyTopProducts(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(defaultValue = "10") limit: Int
    ): List<ProductResult.ProductWithOptions> {
        val targetDate = date ?: LocalDate.now()
        val topProductIds = productRankingService.getTopDailyProducts(targetDate, limit)

        return if (topProductIds.isNotEmpty()) {
            productFacade.getProductsWithOptionsByIds(topProductIds)
        } else {
            productFacade.getAllProductsWithOptions().take(limit)
        }
    }

    // 주간 랭킹 및 특정 상품 랭킹 조회 API...
}
```

## 10. 개발 후 회고

### 10.1 성과

1. **Redis 통합**

   - Sorted Set을 활용하여 효율적인 랭킹 시스템 구축
   - TTL 기능을 활용한 자동 데이터 관리 구현

2. **실시간성 확보**

   - 주문 완료 즉시 랭킹 데이터에 반영
   - DB 조회 없이 빠른 랭킹 정보 제공

3. **확장성 있는 설계**
   - 일간/주간 분리로 다양한 기간별 랭킹 제공
   - 손쉽게 카테고리별, 지역별 확장 가능한 구조

### 10.2 향후 개선 방향

1. **캐싱 전략 고도화**

   - 캐시 히트율 모니터링 및 최적화
   - 캐시 프리워밍(Pre-warming) 전략 도입

2. **장애 대응 강화**

   - Redis 클러스터 구성으로 가용성 확보
   - 장애 발생 시 대체 조회 로직 구현

3. **비즈니스 인사이트 확장**
   - 시간대별, 카테고리별 세분화된 랭킹 제공
   - 사용자 프로필과 연계한 개인화된 추천 기능 확장

이번 Redis 기반 랭킹 시스템 구현을 통해 높은 성능과 확장성을 갖춘 실시간 데이터 처리 시스템을 성공적으로 구축했습니다. 특히 Redis의 Sorted Set 자료구조가 랭킹 시스템 구현에 매우 적합함을 실무적으로 확인할 수 있었으며, 이를 통해 DB 부하를 최소화하면서도 사용자에게 빠른 응답 시간을 제공할 수 있게 되었습니다. 앞으로 더 많은 비즈니스 요구사항을 수용하면서 시스템을 지속적으로 발전시켜 나갈 계획입니다.
