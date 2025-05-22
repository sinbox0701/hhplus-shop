# MSA 기반 도메인 분리 및 트랜잭션 처리 설계 방안

## 1. 현재 시스템 구조 분석

현재 시스템은 단일 애플리케이션(Monolithic)에서 이벤트 기반 아키텍처를 활용하여 각 도메인 간 결합도를 낮춘 구조로 설계되어 있습니다. 다음과 같은 주요 도메인으로 구성되어 있습니다:

- **주문(Order)**: 주문 생성, 취소, 상태 관리 등의 주문 도메인 핵심 로직
- **상품(Product)**: 상품 정보 관리, 재고 관리 등의 상품 도메인 로직
- **사용자(User)**: 사용자 정보 관리, 인증/인가 관련 로직
- **계좌(Account)**: 사용자 계좌 관리, 입출금 처리 등의 금융 관련 로직
- **쿠폰(Coupon)**: 쿠폰 발급, 사용, 만료 등의 쿠폰 관련 로직
- **랭킹(Ranking)**: 상품 인기도, 판매 순위 등의 집계 및 통계 로직

현재는 이벤트 발행자(OrderEventPublisher)와 이벤트 구독자(각종 EventListener)를 통해 도메인 간 간접적인 통신을 구현하고 있으며, 이를 통해 주요 비즈니스 로직과 부가 기능을 분리하였습니다.

## 2. MSA 전환 시 도메인 분리 방안

서비스의 규모가 확장됨에 따라 MSA로 전환할 경우, 다음과 같이 도메인별 마이크로서비스를 구성할 수 있습니다:

### 2.1 마이크로서비스 구성

| 마이크로서비스           | 포함 도메인   | 주요 기능                              | 주요 API                          |
| ------------------------ | ------------- | -------------------------------------- | --------------------------------- |
| **주문 서비스**          | Order         | 주문 생성, 조회, 취소, 상태 관리       | `/api/orders/*`                   |
| **상품 서비스**          | Product       | 상품 정보 관리, 재고 관리              | `/api/products/*`                 |
| **사용자 서비스**        | User, Account | 사용자 정보 관리, 계좌 관리, 인증/인가 | `/api/users/*`, `/api/accounts/*` |
| **쿠폰 서비스**          | Coupon        | 쿠폰 관리, 발급, 사용                  | `/api/coupons/*`                  |
| **랭킹 서비스**          | Ranking       | 상품 랭킹, 통계 데이터 처리            | `/api/rankings/*`                 |
| **데이터 플랫폼 서비스** | -             | 데이터 수집, 분석, 처리                | -                                 |

### 2.2 서비스 책임 분리

각 마이크로서비스는 자신의 도메인에 관련된 데이터와 로직만 담당하며, 다른 서비스의 데이터에 직접 접근하지 않습니다:

- **주문 서비스**: 주문 데이터의 CRUD 및 주문 상태 관리 담당
- **상품 서비스**: 상품 데이터 관리 및 재고 관리 담당
- **사용자 서비스**: 사용자 정보 및 계좌 데이터 관리, 인증/인가 담당
- **쿠폰 서비스**: 쿠폰 발급, 사용, 만료 등의 처리 담당
- **랭킹 서비스**: 상품 인기도, 판매 순위 등의 집계/통계 담당
- **데이터 플랫폼 서비스**: 주문, 상품 등의 데이터 수집 및 분석 담당

## 3. 도메인 분리에 따른 트랜잭션 처리의 한계

MSA 환경에서는 각 서비스가 독립적인 데이터베이스를 가지므로, 여러 서비스에 걸친 트랜잭션의 원자성(Atomicity)을 보장하기 어렵습니다. 주문 처리 프로세스의 경우 다음과 같은 문제가 발생할 수 있습니다:

### 3.1 분산 트랜잭션 문제

주문 생성 시 다음과 같은 여러 서비스 간 트랜잭션 처리가 필요합니다:

1. 주문 서비스: 주문 생성
2. 상품 서비스: 재고 차감
3. 사용자 서비스: 계좌에서 금액 차감
4. 쿠폰 서비스: 쿠폰 사용 처리
5. 랭킹 서비스: 상품 인기도 점수 증가
6. 데이터 플랫폼 서비스: 주문 데이터 전송

위 과정에서 어느 한 단계라도 실패할 경우, 모든 서비스의 데이터를 일관성 있게 롤백하기 어렵습니다. 특히 3번 계좌 차감 과정이 실패했을 때, 이미 처리된 1, 2번 과정을 완벽하게 롤백해야 합니다.

### 3.2 서비스 간 의존성 문제

서비스 간 동기적 호출은 각 서비스의 독립성을 저해하고, 한 서비스의 장애가 전체 시스템에 영향을 미칠 수 있습니다. 예를 들어, 주문 생성 시 상품 서비스에 재고를 확인하는 동기 호출이 있으면, 상품 서비스의 장애가 주문 서비스의 장애로 전파됩니다.

## 4. 트랜잭션 일관성 유지를 위한 해결 방안

### 4.1 SAGA 패턴 적용

SAGA 패턴은 분산 트랜잭션을 일련의 로컬 트랜잭션으로 분해하고, 각 트랜잭션의 실패 시 보상 트랜잭션을 실행하여 일관성을 유지하는 패턴입니다.

#### 4.1.1 Choreography SAGA

우리 시스템에서는 Choreography 방식의 SAGA 패턴을 적용합니다. 각 서비스는 자신의 작업을 완료한 후 이벤트를 발행하고, 다른 서비스는 해당 이벤트를 구독하여 자신의 작업을 수행합니다.

**주문 생성 시나리오**:

1. 주문 서비스: 주문 생성 후 `OrderCreated` 이벤트 발행
2. 상품 서비스: `OrderCreated` 이벤트 수신 → 재고 차감 → 성공 시 `ProductStockDecreased` 이벤트 발행, 실패 시 `ProductStockDecreaseFailed` 이벤트 발행
3. 사용자 서비스: `ProductStockDecreased` 이벤트 수신 → 계좌 금액 차감 → 성공 시 `AccountCharged` 이벤트 발행, 실패 시 `AccountChargeFailed` 이벤트 발행
4. 쿠폰 서비스: `AccountCharged` 이벤트 수신 → 쿠폰 사용 처리 → 성공 시 `CouponUsed` 이벤트 발행, 실패 시 `CouponUseFailed` 이벤트 발행
5. 주문 서비스: 모든 과정 완료 시 `OrderCompleted` 이벤트 발행

#### 4.1.2 보상 트랜잭션 처리

각 단계에서 실패 이벤트가 발생할 경우, 이미 완료된 단계를 역순으로 보상 트랜잭션을 실행합니다:

1. `ProductStockDecreaseFailed` 발생 시: 주문 서비스에서 주문 취소(`OrderCancelled` 이벤트 발행)
2. `AccountChargeFailed` 발생 시: 상품 서비스에서 재고 복구, 주문 서비스에서 주문 취소
3. `CouponUseFailed` 발생 시: 사용자 서비스에서 계좌 금액 환불, 상품 서비스에서 재고 복구, 주문 서비스에서 주문 취소

### 4.2 이벤트 발행 및 구독 메커니즘 구현

#### 4.2.1 이벤트 메시지 큐 도입

서비스 간 이벤트 통신에는 Kafka 또는 RabbitMQ와 같은 메시지 큐를 활용합니다:

```
                                   ┌─────────────────┐
                                   │                 │
                                   │  Event Message  │
                                   │      Queue      │
                                   │   (Kafka/RMQ)   │
                                   │                 │
                                   └─────────────────┘
                                         ▲     │
                                         │     │
             ┌─────────────┐      ┌─────┴─────▼────┐       ┌──────────────┐
             │             │      │                │        │              │
  ┌──────────►   Order     ├─────►│   Product      ├────────►   Account    │
  │          │   Service   │      │   Service      │        │   Service    │
  │          │             │      │                │        │              │
  │          └─────────────┘      └────────────────┘       └──────────────┘
  │                 ▲                                             │
  │                 │                                             │
  │                 │             ┌────────────────┐             │
  │                 │             │                │             │
  └─────────────────┼─────────────┤    Coupon      │◄────────────┘
                    │             │    Service     │
                    │             │                │
                    │             └────────────────┘
                    │                     ▲
                    │                     │
                    │             ┌───────┴────────┐
                    │             │                │
                    └─────────────┤    Ranking     │
                                  │    Service     │
                                  │                │
                                  └────────────────┘
```

#### 4.2.2 멱등성 보장

이벤트 처리 시 멱등성(Idempotency)을 보장하여 동일한 이벤트가 중복 처리되지 않도록 합니다:

- 각 이벤트에 고유 ID 부여
- 이벤트 처리 로직에서 이미 처리된 이벤트는 무시
- 각 서비스에서 처리된 이벤트 ID를 기록

### 4.3 최종 일관성(Eventual Consistency) 모델 채택

MSA 환경에서는 즉시적인 데이터 일관성보다 최종 일관성(Eventual Consistency)을 보장하는 방향으로 설계합니다:

- 각 서비스는 자신의 도메인 내에서는 강한 일관성 보장
- 서비스 간에는 이벤트를 통한 최종 일관성 보장
- 사용자에게는 적절한 UI/UX로 처리 상태 표시 (주문 접수 → 결제 중 → 결제 완료 → 배송 준비 등)

## 5. 서비스 간 통신 방식

### 5.1 이벤트 기반 비동기 통신

기본적으로 서비스 간 통신은 이벤트 기반 비동기 방식을 사용합니다:

```kotlin
// 주문 서비스에서 이벤트 발행
@Service
class OrderService(private val eventPublisher: EventPublisher) {
    @Transactional
    fun createOrder(command: CreateOrderCommand): Order {
        val order = Order.create(command)
        orderRepository.save(order)

        // 이벤트 발행
        eventPublisher.publish(
            OrderCreatedEvent(
                orderId = order.id,
                userId = order.userId,
                items = order.items.map { OrderItemDto(it) },
                totalPrice = order.totalPrice
            )
        )

        return order
    }
}

// 상품 서비스에서 이벤트 구독
@Service
class ProductStockEventHandler(private val productService: ProductService) {
    @EventListener
    @Transactional
    fun handleOrderCreated(event: OrderCreatedEvent) {
        try {
            event.items.forEach { item ->
                productService.decreaseStock(item.productId, item.quantity)
            }
            // 성공 이벤트 발행
        } catch (e: Exception) {
            // 실패 이벤트 발행
        }
    }
}
```

### 5.2 필요한 경우 동기 통신 사용

일부 즉시 응답이 필요한 케이스(예: 상품 재고 확인)에서는 REST API 또는 gRPC를 사용한 동기 통신을 사용할 수 있습니다:

```kotlin
// API 클라이언트 (FeignClient 또는 WebClient 사용)
@Component
class ProductServiceClient(private val webClient: WebClient) {
    fun checkStock(productId: Long, quantity: Int): Boolean {
        return webClient.get()
            .uri("/api/products/$productId/stock/check?quantity=$quantity")
            .retrieve()
            .bodyToMono(Boolean::class.java)
            .block() ?: false
    }
}
```

동기 호출은 Circuit Breaker, Retry, Timeout 등의 패턴을 적용하여 장애 전파를 방지합니다.

## 6. MSA 환경에서의 데이터 조회 패턴

### 6.1 API Composition 패턴

여러 서비스의 데이터를 조합해야 하는 조회 기능(예: 주문 상세 조회)은 API Composition 패턴을 사용합니다:

```kotlin
@RestController
class OrderQueryController(
    private val orderService: OrderService,
    private val productServiceClient: ProductServiceClient,
    private val userServiceClient: UserServiceClient
) {
    @GetMapping("/api/orders/{orderId}")
    fun getOrderDetails(@PathVariable orderId: Long): OrderDetailResponse {
        // 1. 주문 정보 조회
        val order = orderService.getOrder(orderId)

        // 2. 상품 정보 조회
        val products = productServiceClient.getProductsByIds(order.items.map { it.productId })

        // 3. 사용자 정보 조회
        val user = userServiceClient.getUserById(order.userId)

        // 4. 결과 조합
        return OrderDetailResponse.from(order, products, user)
    }
}
```

### 6.2 CQRS 패턴

복잡한 조회 기능이 많을 경우, CQRS(Command Query Responsibility Segregation) 패턴을 적용하여 조회 전용 데이터 모델을 구축합니다:

- 각 서비스에서 발생한 이벤트를 수집하여 조회 전용 데이터베이스 구축
- 복잡한 조회 기능은 조회 전용 서비스에서 처리
- 주문 내역, 상품 정보, 사용자 정보 등을 조합한 뷰 제공

## 7. 배포 및 운영 전략

### 7.1 독립적인 CI/CD 파이프라인

각 마이크로서비스마다 독립적인 CI/CD 파이프라인을 구축하여 개발 및 배포 주기를 독립적으로 가져갑니다:

```
서비스별 Repository → 빌드 → 테스트 → 이미지 생성 → 배포
```

### 7.2 서비스 디스커버리 및 API 게이트웨이

MSA 환경에서는 서비스 디스커버리(예: Netflix Eureka, Consul)와 API 게이트웨이(예: Spring Cloud Gateway)를 활용하여 서비스 간 통신을 관리합니다:

- API 게이트웨이: 라우팅, 인증/인가, 로드밸런싱, 속도 제한 등 처리
- 서비스 디스커버리: 서비스 인스턴스 등록 및 발견

### 7.3 모니터링 및 로깅

분산 시스템의 모니터링 및 로깅을 위한 도구 도입:

- 분산 추적: Zipkin, Jaeger
- 모니터링: Prometheus, Grafana
- 로깅: ELK 스택(Elasticsearch, Logstash, Kibana)
- 알림: AlertManager, Slack 연동

## 8. 결론 및 고려사항

### 8.1 MSA 전환의 이점

- **확장성**: 도메인별 서비스 독립 확장 가능
- **유연성**: 기술 스택 자유롭게 선택 가능
- **배포 독립성**: 서비스별 독립적인 개발/배포 주기
- **팀 자율성**: 도메인별 팀 구성 및 독립적 운영

### 8.2 MSA 전환 시 주의점

- **복잡성 증가**: 분산 시스템의 복잡성 관리 필요
- **트랜잭션 일관성**: SAGA 패턴 등을 통한 일관성 보장 필요
- **네트워크 비용**: 서비스 간 통신 오버헤드 발생
- **운영 복잡성**: 여러 서비스 모니터링 및 관리 부담

### 8.3 단계적 MSA 전환 전략

기존 모놀리식 시스템에서 MSA로의 전환은 단계적으로 진행합니다:

1. **이벤트 기반 아키텍처 도입**: 모놀리식 내에서 이벤트 발행/구독 구조 적용 (현재 단계)
2. **도메인 분리**: 도메인별 모듈화 강화
3. **서비스 추출**: 핵심 도메인부터 순차적으로 독립 서비스로 추출
4. **통신 패턴 개선**: 동기 → 비동기 통신으로 전환
5. **인프라 고도화**: 서비스 디스커버리, API 게이트웨이 등 인프라 구축

MSA로의 전환은 시스템의 복잡성과 비즈니스 요구사항에 맞게 신중하게 진행해야 하며, 이벤트 기반 아키텍처는 이러한 전환을 위한 중요한 첫 단계가 됩니다.
