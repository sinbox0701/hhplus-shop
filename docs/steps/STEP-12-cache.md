### **`STEP-12 - Cache`**

- 조회 성능 개선을 위한 캐싱 전략 설계 및 적용
- 트래픽 증가에 따른 시스템 확장성 확보

> 이 단계에서는 높은 트래픽 API와 DB 부하가 큰 조회 로직에 캐싱을 적용하여 시스템 성능을 최적화하는 방법을 다룹니다.

---

## 캐싱 전략 계획서

### 1. 캐시 도입 배경 및 필요성

#### 현재 시스템의 성능 병목 분석

현재 시스템은 다음과 같은 성능 제한 요소가 존재합니다:

1. **반복적인 데이터베이스 조회**:

   - 자주 접근하지만 거의 변경되지 않는 데이터(상품 정보, 카테고리 등)에 대한 반복 조회
   - 동일 쿼리의 빈번한 실행으로 인한 데이터베이스 부하 증가

2. **복잡한 집계 쿼리**:

   - 상품 랭킹, 베스트셀러, 추천 상품 등 계산 비용이 높은 집계 작업
   - 실시간 계산에 따른 응답 시간 지연 및 시스템 리소스 소모

3. **트래픽 집중**:

   - 특정 시간대(출퇴근 시간, 프로모션 기간 등)에 사용자 요청 집중
   - 피크 시간대 서버 및 데이터베이스 과부하 발생 가능성

4. **서비스 확장성 제한**:
   - 데이터베이스 의존도가 높아 수평적 확장의 효율성 저하
   - 사용자 증가에 따른 인프라 비용 증가율 높음

#### 캐시 도입의 기대 효과

캐시 도입을 통해 기대할 수 있는 주요 효과:

1. **응답 시간 단축**:

   - 평균 API 응답 시간 50% 이상 감소 목표
   - 사용자 경험 향상 및 만족도 증가

2. **시스템 처리량 증가**:

   - 동일 인프라로 2-3배 이상의 요청 처리 가능
   - 트래픽 피크 시간대 안정적인 서비스 제공

3. **데이터베이스 부하 감소**:

   - 데이터베이스 연결 수 및 쿼리 실행 횟수 감소
   - 데이터베이스 리소스 여유 확보로 다른 중요 작업에 집중

4. **비용 효율성**:

   - 인프라 증설 필요성 감소 및 운영 비용 절감
   - 클라우드 환경에서의 자원 최적화

5. **서비스 안정성 향상**:
   - 장애 발생 시에도 캐시된 데이터로 서비스 일부 기능 유지 가능
   - 데이터베이스 일시적 장애에 대한 복원력 증가

### 2. 주요 캐싱 대상 분석

효과적인 캐싱을 위해 다음과 같은 데이터 유형을 우선적으로 고려합니다:

#### 캐싱 적합성 평가 기준

| 기준                 | 설명                                           | 가중치 |
| -------------------- | ---------------------------------------------- | ------ |
| **읽기/쓰기 비율**   | 읽기 작업이 쓰기 작업보다 월등히 많을수록 적합 | 높음   |
| **데이터 변경 빈도** | 변경이 적을수록 캐싱 효과 높음                 | 높음   |
| **데이터 크기**      | 적절한 크기(너무 크지 않은)의 데이터가 적합    | 중간   |
| **요청 빈도**        | 자주 요청되는 데이터일수록 캐싱 효과 높음      | 높음   |
| **계산 복잡성**      | 계산/조회 비용이 높을수록 캐싱 가치 증가       | 중간   |
| **일관성 요구사항**  | 강한 일관성이 요구되지 않는 데이터가 적합      | 중간   |

#### 우선 캐싱 대상 목록

아래 데이터 유형은 캐싱 적합성이 높다고 판단됩니다:

1. **상품 정보**:

   - 상품 상세 정보 (이미지 URL, 설명, 가격 등)
   - 상품 카테고리 및 속성 정보
   - 적합 이유: 읽기 비율 높음, 변경 빈도 낮음, 높은 조회 빈도

2. **카테고리 및 메뉴 구조**:

   - 전체 카테고리 트리, 네비게이션 메뉴
   - 적합 이유: 변경이 매우 드묾, 모든 페이지에서 사용됨

3. **인기 상품 및 추천 상품**:

   - 베스트셀러, 신상품, 추천 상품 목록
   - 적합 이유: 계산 비용 높음, 실시간성 요구 낮음

4. **검색 결과**:

   - 인기 검색어에 대한 결과
   - 적합 이유: 반복적인 동일 검색어, 계산 비용 높음

5. **사용자 비개인화 설정**:
   - 배송비 정책, 프로모션 정보 등
   - 적합 이유: 모든 사용자에게 공통적으로 제공, 변경 빈도 낮음

#### 캐싱 부적합 데이터

다음 데이터 유형은 캐싱에 적합하지 않다고 판단됩니다:

1. **개인화된 사용자 데이터**:

   - 장바구니, 위시리스트
   - 부적합 이유: 사용자별 고유 데이터, 변경 빈도 높음

2. **재고 정보**:

   - 실시간 재고 수량
   - 부적합 이유: 강한 일관성 필요, 변경 빈도 높음

3. **결제 정보**:

   - 거래 내역, 결제 상태
   - 부적합 이유: 보안 문제, 강한 일관성 필요

4. **사용자 인증 데이터**:
   - 비밀번호, 개인 식별 정보
   - 부적합 이유: 보안 문제, 민감 정보 처리 필요

### 3. 캐싱 전략 선택

#### 캐시 계층 구조 설계

본 프로젝트에서는 다층 캐싱 전략을 채택하여 최적의 성능을 확보합니다:

1. **로컬 인메모리 캐시 (L1)**:

   - 위치: 각 애플리케이션 서버 내부
   - 기술: Caffeine, Guava Cache 또는 Spring Cache
   - 용도: 초고속 접근이 필요한 작은 크기의 데이터, 자주 요청되는 데이터
   - 특징: 가장 빠른 접근 속도, 서버 간 동기화 없음

2. **분산 캐시 (L2)**:

   - 위치: 별도의 캐시 서버 클러스터
   - 기술: Redis
   - 용도: 서버 간 공유가 필요한 데이터, 세션 데이터, 중간 규모 데이터
   - 특징: 높은 확장성, 영속성 지원, 다양한 데이터 구조

3. **애플리케이션 프록시 캐시 (선택적)**:
   - 위치: API 게이트웨이 또는 CDN
   - 기술: Nginx, CloudFront, Varnish
   - 용도: 완전히 정적인 콘텐츠, 이미지, CSS/JS
   - 특징: 애플리케이션 서버 부하 완전 제거

#### 캐싱 패턴 선택

각 데이터 유형과 접근 패턴에 따라 적절한 캐싱 패턴을 선택합니다:

1. **Cache-Aside (Lazy Loading) 패턴**:

   - 적용 대상: 상품 상세 정보, 사용자 설정 등 요청 시점에 필요한 데이터
   - 동작 방식:
     1. 데이터 요청 시 먼저 캐시 확인
     2. 캐시에 없으면(Cache Miss) DB에서 조회 후 캐시에 저장
     3. 이후 동일 요청은 캐시에서 제공(Cache Hit)
   - 장점: 필요한 데이터만 캐싱, 구현 간단
   - 단점: 최초 요청 시 지연, Cache Stampede 발생 가능

2. **Read-Through 패턴**:

   - 적용 대상: 카테고리 데이터, 공통 코드 등 전체 데이터셋
   - 동작 방식:
     1. 캐시 계층이 데이터 소스 접근을 관리
     2. 애플리케이션은 캐시 계층만 접근
     3. 캐시 계층이 DB 조회 로직 처리
   - 장점: 캐싱 로직 분리, 일관된 접근 패턴
   - 단점: 캐시 계층 복잡성, 전용 라이브러리 필요

3. **Write-Through/Behind 패턴**:

   - 적용 대상: 재고 수량, 인기도 점수 등 쓰기 작업이 빈번한 데이터
   - 동작 방식:
     - Write-Through: 데이터 변경 시 캐시와 DB 동시 업데이트
     - Write-Behind: 캐시 먼저 업데이트 후 비동기적으로 DB 반영
   - 장점: 캐시-DB 일관성 유지, 쓰기 작업 성능 향상
   - 단점: 구현 복잡성, 일시적 불일치 가능성

4. **Refresh-Ahead 패턴**:
   - 적용 대상: 베스트셀러, 추천 상품 등 주기적 업데이트가 필요한 데이터
   - 동작 방식:
     1. 만료 시간 직전에 미리 데이터 갱신
     2. 백그라운드에서 비동기적으로 캐시 갱신
   - 장점: 캐시 미스 최소화, 일관된 응답 시간
   - 단점: 캐시 갱신 로직 복잡성, 불필요한 리소스 사용 가능성

#### 캐시 유형별 적용 패턴

| 데이터 유형    | 캐시 계층 | 캐싱 패턴     | TTL 전략         |
| -------------- | --------- | ------------- | ---------------- |
| 상품 기본 정보 | L1+L2     | Cache-Aside   | 중간(30분-1시간) |
| 상품 상세 정보 | L2        | Cache-Aside   | 중간(1시간)      |
| 카테고리 트리  | L1        | Read-Through  | 긴(12시간)       |
| 베스트셀러     | L2        | Refresh-Ahead | 짧은(5-15분)     |
| 검색 결과      | L2        | Cache-Aside   | 짧은(10-30분)    |
| 프로모션 정보  | L1+L2     | Read-Through  | 중간(1-2시간)    |
| 사용자 설정    | L2        | Cache-Aside   | 긴(6-12시간)     |

### 4. 캐시 관리 전략

#### TTL(Time-to-Live) 전략

효과적인 캐시 관리를 위한 TTL 설계 지침:

1. **데이터 유형별 TTL 설정**:

   - **실시간성 높은 데이터**: 짧은 TTL (1-5분)
   - **일반 콘텐츠**: 중간 TTL (10-60분)
   - **거의 변경되지 않는 데이터**: 긴 TTL (1시간-24시간)

2. **동적 TTL 적용**:

   - 트래픽 패턴에 따라 TTL 동적 조정
   - 피크 시간대는 TTL 연장, 한가한 시간대는 TTL 축소

3. **데이터 변경 연동**:
   - 데이터 변경 시 관련 캐시 항목 무효화
   - 이벤트 기반 캐시 갱신 메커니즘 구현

#### 캐시 Eviction 정책

제한된 메모리 상황에서 효율적인 캐시 관리를 위한 Eviction 정책:

1. **LRU (Least Recently Used)**:

   - 가장 최근에 사용되지 않은 항목부터 제거
   - 일반적으로 가장 적합한 기본 정책

2. **LFU (Least Frequently Used)**:

   - 가장 적게 사용된 항목부터 제거
   - 인기 있는 항목의 재접근 빈도가 높은 경우 유용

3. **TTL 기반**:

   - 만료 시간이 가까운 항목부터 제거
   - 시간 기반 일관성이 중요한 경우 적합

4. **크기 기반**:
   - 큰 항목부터 제거하여 공간 확보
   - 메모리 제약이 심한 환경에서 유용

Redis의 경우 다음과 같은 Eviction 정책을 권장합니다:

- `volatile-lru`: 만료 시간이 설정된 키 중 LRU 방식으로 제거
- `allkeys-lru`: 모든 키를 대상으로 LRU 방식 적용 (일반적 권장)

#### Cache Stampede 방지 전략

동일 시점에 다수의 요청으로 인한 Cache Stampede 문제 해결 방안:

1. **Probabilistic Early Expiration (확률적 조기 만료)**:

   - TTL의 마지막 일정 기간(예: 남은 시간의 10%)에 확률적으로 캐시 갱신
   - 모든 요청이 동시에 DB를 조회하는 상황 방지

   ```kotlin
   fun getValue(key: String): Value {
       val cachedValue = cache.get(key)

       // 캐시 히트인 경우
       if (cachedValue != null) {
           val remainingTtl = cache.getRemainingTtl(key)
           val totalTtl = cache.getTotalTtl(key)

           // 만료 시간의 90% 이상 경과 & 10% 확률로 갱신
           if (remainingTtl < totalTtl * 0.1 && Math.random() < 0.1) {
               // 비동기로 캐시 갱신
               CompletableFuture.runAsync {
                   val freshValue = fetchFromDatabase(key)
                   cache.put(key, freshValue, totalTtl)
               }
           }

           return cachedValue
       }

       // 캐시 미스인 경우
       return fetchAndCacheValue(key)
   }
   ```

2. **Mutex/Lock 활용**:

   - 캐시 미스 시 첫 번째 요청만 DB에서 조회하도록 락 사용
   - 나머지 요청은 짧은 시간 대기 후 캐시 재확인

   ```kotlin
   fun getValue(key: String): Value {
       // 캐시 확인
       val cachedValue = cache.get(key)
       if (cachedValue != null) {
           return cachedValue
       }

       // 캐시 미스: 락 획득 시도
       val lockKey = "lock:$key"
       val lockAcquired = cache.setIfNotExists(lockKey, "1", "EX", 5)

       if (lockAcquired) {
           try {
               // 락 획득 성공: DB에서 값 조회
               val value = fetchFromDatabase(key)
               cache.put(key, value, TTL)
               return value
           } finally {
               // 락 해제
               cache.delete(lockKey)
           }
       } else {
           // 락 획득 실패: 짧게 대기 후 재시도
           Thread.sleep(50)
           return getValue(key) // 재귀 호출 (최대 재시도 횟수 제한 필요)
       }
   }
   ```

3. **Bulk Loading**:

   - 캐시 초기화 시 대량의 데이터를 미리 로드
   - 시스템 시작 시 또는 정기적으로 전체 캐시 워밍업 수행

4. **Negative Caching**:
   - 존재하지 않는 데이터에 대해서도 짧은 시간 캐싱
   - 존재하지 않는 키에 대한 반복 조회 방지

#### 캐시 모니터링 및 관리

효과적인 캐시 운영을 위한 모니터링 전략:

1. **핵심 메트릭 모니터링**:

   - **Hit Ratio**: 캐시 히트율 (목표: 80% 이상)
   - **Latency**: 응답 시간 (캐시 히트/미스 구분)
   - **Memory Usage**: 메모리 사용량 및 증가 추세
   - **Eviction Rate**: 제거 비율 및 패턴

2. **이상 탐지 및 알림**:

   - 캐시 히트율 급감 감지
   - 메모리 사용량 임계치 초과 알림
   - 캐시 연결 실패 또는 지연 알림

3. **캐시 워밍업 전략**:

   - 시스템 재시작 또는 캐시 초기화 후 주요 데이터 미리 로드
   - 사용 패턴 분석을 통한 선제적 캐싱

4. **정기적 분석 및 최적화**:
   - 캐시 효율성 정기 검토 (주간/월간)
   - 사용 패턴 변화에 따른 캐싱 전략 조정
   - 불필요한 캐시 항목 식별 및 제거

### 5. 캐시 구현 계획

#### 단계별 구현 로드맵

1. **기초 인프라 구축 (1주차)**

   - Redis 서버 설정 및 클러스터 구성
   - 스프링 캐시 추상화 계층 구현
   - 로컬 캐시 설정 (Caffeine)

2. **핵심 기능 캐싱 (2주차)**

   - 상품 정보 조회 캐싱 구현
   - 카테고리 및 메뉴 캐싱 구현
   - Cache-Aside 패턴 적용

3. **고급 기능 구현 (3주차)**

   - 캐시 무효화 메커니즘 구현
   - Refresh-Ahead 패턴 적용
   - Cache Stampede 방지 로직 구현

4. **최적화 및 모니터링 (4주차)**
   - 캐시 성능 측정 및 튜닝
   - 모니터링 대시보드 구축
   - 문서화 및 지침 작성

#### Redis 캐시 구현 예시

Spring Cache 추상화와 Redis를 활용한 기본 캐싱 구현:

```kotlin
// 1. Redis 캐시 설정
@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): RedisCacheManager {
        val defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )

        // 캐시별 TTL 설정
        val cacheConfigurations = mapOf(
            "products" to defaultCacheConfig.entryTtl(Duration.ofMinutes(60)),
            "categories" to defaultCacheConfig.entryTtl(Duration.ofHours(12)),
            "bestSellers" to defaultCacheConfig.entryTtl(Duration.ofMinutes(15))
        )

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultCacheConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }
}

// 2. 서비스 계층 캐싱 적용
@Service
class ProductService(private val productRepository: ProductRepository) {

    @Cacheable(value = ["products"], key = "#productId")
    fun getProductById(productId: String): Product {
        // 로깅 또는 메트릭 수집 (캐시 미스 추적)
        log.info("Cache miss for product: $productId")
        return productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }
    }

    @CacheEvict(value = ["products"], key = "#product.id")
    fun updateProduct(product: Product): Product {
        return productRepository.save(product)
    }

    @CachePut(value = ["products"], key = "#result.id")
    fun createProduct(product: Product): Product {
        return productRepository.save(product)
    }

    @Cacheable(value = ["bestSellers"], key = "'top' + #limit")
    fun getBestSellers(limit: Int): List<Product> {
        log.info("Generating best sellers list, limit: $limit")
        return productRepository.findTopByOrderBySalesCountDesc(limit)
    }
}
```

#### Cache Stampede 방지를 위한 고급 구현

Caffeine 캐시와 비동기 로딩을 활용한 Cache Stampede 방지:

```kotlin
@Configuration
class CaffeineConfig {
    @Bean
    fun productCacheLoader(productRepository: ProductRepository): AsyncCacheLoader<String, Product> {
        return AsyncCacheLoader<String, Product> { key, executor ->
            CompletableFuture.supplyAsync({
                log.info("Async loading product: $key")
                productRepository.findById(key).orElseThrow()
            }, executor)
        }
    }

    @Bean
    fun productCache(productCacheLoader: AsyncCacheLoader<String, Product>): AsyncLoadingCache<String, Product> {
        return Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .refreshAfterWrite(25, TimeUnit.MINUTES) // 만료 직전에 리프레시
            .maximumSize(10_000)
            .recordStats()
            .buildAsync(productCacheLoader)
    }
}

@Service
class ProductCacheService(
    private val productCache: AsyncLoadingCache<String, Product>,
    private val metricsService: MetricsService
) {
    suspend fun getProduct(id: String): Product {
        val startTime = System.nanoTime()

        try {
            return productCache.get(id).await()
        } finally {
            val duration = System.nanoTime() - startTime
            metricsService.recordCacheAccess("product", duration)
        }
    }

    fun invalidateProduct(id: String) {
        productCache.synchronous().invalidate(id)
    }

    fun getCacheStats(): CacheStats {
        return productCache.synchronous().stats()
    }
}
```

### 6. 결론 및 기대 효과

#### 주요 기대 효과

1. **성능 개선**:

   - API 응답 시간 50-80% 감소
   - 시스템 처리량 2-3배 향상
   - 데이터베이스 부하 60-70% 감소

2. **비용 효율성**:

   - 인프라 확장 필요성 감소
   - 클라우드 리소스 사용 최적화
   - 운영 비용 절감 (대략 30-40%)

3. **사용자 경험 향상**:
   - 페이지 로드 시간 단축
   - 일관된 응답 시간 제공
   - 서비스 안정성 증가

#### 향후 발전 방향

1. **머신러닝 기반 캐싱**:
   - 사용 패턴 분석을 통한 예측적 캐싱
   - 사용자별 맞춤형 캐시 관리
