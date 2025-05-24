# 이벤트 기반 아키텍처를 통한 주문 처리 시스템 재구성 방안

## 핵심 주제

1. 실시간 주문정보(이커머스)를 데이터 플랫폼에 전송(mock API 호출)하는 요구사항을 이벤트를 활용하여 트랜잭션과 관심사를 분리하여 서비스를 개선합니다.
2. 메인 로직과 부가 로직을 "이벤트" 라는 매개로 분리하여 시스템의 안정성과 확장성을 높입니다.
3. 핵심 원칙:
   - 메인 로직은 부가 로직을 몰라야 한다.
   - 부가 로직의 성공/실패 여부는 메인 로직의 결과에 영향을 끼치면 안된다.
4. Saga 패턴 + CHOREOGRAPHY를 활용하여 프로젝트를 고도화합니다.
5. Order Facade를 분리해서 어플리케이션 이벤트를 활용하여 각 서비스 간 의존성을 제거합니다.

## 1. 현재 시스템 분석

현재 시스템은 Facade 패턴을 사용하여 구현되어 있습니다. 주문 처리 로직이 하나의 트랜잭션 내에서 동기적으로 처리되고 있으며, 주문 서비스가 다양한 도메인(상품, 결제, 쿠폰 등)에 직접 의존하는 구조입니다.

### 문제점:

- 트랜잭션이 길고 여러 외부 API 호출을 포함하여 장애 발생 가능성이 높음
- 핵심 로직과 부가 로직이 혼재되어 있어 책임 분리가 명확하지 않음
- 서비스 간 강한 결합도로 인해 확장성과 유지보수성이 저하됨

## 2. 이벤트 기반 아키텍처로의 전환 방안

### 2.1 디렉토리 구조 설계

```
server/
├── interfaces/
│   └── order/
│       ├── OrderController.kt
│       └── event/
│           ├── OrderKafkaEventListener.kt
│           ├── ProductOrderEventListener.kt
│           ├── CouponOrderEventListener.kt
│           └── OrderCompensationListener.kt
├── application/
│   └── order/
│       └── OrderFacade.kt
├── domain/
│   └── order/
│       ├── OrderService.kt
│       ├── Order.kt
│       ├── OrderItem.kt
│       ├── OrderEventPublisher.kt
│       ├── OrderRepository.kt
│       └── event/
│           └── OrderEvent.kt
└── infrastructure/
    └── event/
        └── SpringOrderEventPublisher.kt
```

### 2.2 도메인 이벤트 설계

```kotlin
// domain/order/event/OrderEvent.kt
sealed class OrderEvent {
    data class Created(
        val orderId: Long,
        val userId: Long,
        val orderItems: List<OrderItem>,
        val totalAmount: Long,
        val createdAt: LocalDateTime
    ) : OrderEvent()

    data class Completed(
        val orderId: Long,
        val userId: Long,
        val completedAt: LocalDateTime
    ) : OrderEvent()

    data class Failed(
        val orderId: Long,
        val userId: Long,
        val reason: String,
        val failedAt: LocalDateTime
    ) : OrderEvent()
}
```

### 2.3 이벤트 발행자 인터페이스

```kotlin
// domain/order/OrderEventPublisher.kt
interface OrderEventPublisher {
    fun publish(event: OrderEvent)
}

// infrastructure/event/SpringOrderEventPublisher.kt
@Component
class SpringOrderEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : OrderEventPublisher {
    override fun publish(event: OrderEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
```

### 2.4 Facade 및 도메인 서비스 분리

```kotlin
// application/order/OrderFacade.kt
@Service
class OrderFacade(
    private val orderService: OrderService
) {
    fun createOrder(userId: Long, orderRequest: OrderRequest): Order {
        return orderService.createOrder(userId, orderRequest)
    }

    fun cancelOrder(orderId: Long, reason: String): Order {
        return orderService.cancelOrder(orderId, reason)
    }
}

// domain/order/OrderService.kt
@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderEventPublisher: OrderEventPublisher
) {
    @Transactional
    fun createOrder(userId: Long, orderRequest: OrderRequest): Order {
        try {
            // 주문 객체 생성 (핵심 로직)
            val order = Order.create(userId, orderRequest)
            val savedOrder = orderRepository.save(order)

            // 주문 생성 이벤트 발행 (부가 로직을 위한 이벤트)
            orderEventPublisher.publish(
                OrderEvent.Created(
                    orderId = savedOrder.id,
                    userId = savedOrder.userId,
                    orderItems = savedOrder.items,
                    totalAmount = savedOrder.totalAmount,
                    createdAt = savedOrder.createdAt
                )
            )

            return savedOrder
        } catch (e: Exception) {
            // 주문 생성 실패 이벤트 발행
            orderEventPublisher.publish(
                OrderEvent.Failed(
                    orderId = -1L,
                    userId = userId,
                    reason = e.message ?: "Unknown error",
                    failedAt = LocalDateTime.now()
                )
            )
            throw e
        }
    }
}
```

### 2.5 이벤트 리스너 구현

```kotlin
// interfaces/order/event/ProductOrderEventListener.kt
@Component
class ProductOrderEventListener(
    private val productService: ProductService
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCreated(event: OrderEvent.Created) {
        try {
            // 상품 재고 차감 처리 (부가 로직)
            event.orderItems.forEach { item ->
                productService.decreaseStock(item.productId, item.quantity)
            }
        } catch (e: Exception) {
            // 재고 차감 실패 이벤트 발행
            applicationEventPublisher.publishEvent(
                ProductEvent.StockDecreaseFailure(
                    orderId = event.orderId,
                    reason = e.message ?: "Unknown error"
                )
            )
            // 핵심 로직에 영향을 주지 않기 위해 예외를 로그만 남기고 다시 던지지 않음
            log.error("Failed to decrease product stock: ${e.message}", e)
        }
    }
}

// interfaces/order/event/CouponOrderEventListener.kt
@Component
class CouponOrderEventListener(
    private val couponService: CouponService,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCreated(event: OrderEvent.Created) {
        try {
            // 쿠폰 사용 처리 (부가 로직)
            couponService.useCoupon(event.userId, event.orderId)
        } catch (e: Exception) {
            // 쿠폰 사용 실패 이벤트 발행
            applicationEventPublisher.publishEvent(
                CouponEvent.UseFailure(
                    orderId = event.orderId,
                    userId = event.userId,
                    reason = e.message ?: "Unknown error"
                )
            )
            log.error("Failed to use coupon: ${e.message}", e)
        }
    }

    @EventListener
    fun handleOrderFailed(event: OrderEvent.Failed) {
        try {
            // 쿠폰 사용 취소 처리
            couponService.cancelCouponUse(event.userId, event.orderId)
        } catch (e: Exception) {
            log.error("Failed to cancel coupon use: ${e.message}", e)
        }
    }
}

// interfaces/order/event/OrderCompensationListener.kt
@Component
class OrderCompensationListener(
    private val orderFacade: OrderFacade
) {
    @EventListener
    fun handleProductStockFailed(event: ProductEvent.StockDecreaseFailure) {
        // 주문 취소 처리 (보상 트랜잭션)
        orderFacade.cancelOrder(event.orderId, "상품 재고 부족")
    }

    @EventListener
    fun handleCouponUseFailed(event: CouponEvent.UseFailure) {
        // 주문 취소 처리 (보상 트랜잭션)
        orderFacade.cancelOrder(event.orderId, "쿠폰 사용 실패")
    }
}

// interfaces/order/event/OrderKafkaEventListener.kt
@Component
class OrderKafkaEventListener(
    private val dataAnalyticsClient: DataAnalyticsClient
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCreated(event: OrderEvent.Created) {
        try {
            // 데이터 플랫폼에 주문 정보 전송 (외부 시스템 연동)
            dataAnalyticsClient.sendOrderData(
                OrderDataDto.from(event)
            )
        } catch (e: Exception) {
            // 실패해도 핵심 주문 처리에는 영향 없음
            log.error("Failed to send order data to analytics platform: ${e.message}", e)
        }
    }
}
```

## 3. 핵심 세션별 구현 가이드

### 3.1 트랜잭션 구조 진단과 부가 로직 분리

**핵심 고려사항**

- 트랜잭션은 짧고 핵심적인 로직만 포함해야 함
  - 주문 생성, 업데이트와 같은 DB 작업만 트랜잭션 내에서 처리
  - 외부 API 호출, 알림 전송 등은 트랜잭션 외부로 분리
- 부가 로직 실패가 핵심 로직에 영향을 주지 않도록 설계
  - 이벤트 처리 중 발생한 예외는 핵심 로직으로 전파되지 않아야 함
  - 실패 로그 및 모니터링 체계 구축

### 3.2 Application Event의 구조와 흐름 이해

**구현 방식**

- Spring의 `ApplicationEventPublisher`를 통한 이벤트 발행
- `@TransactionalEventListener`의 `phase = TransactionPhase.AFTER_COMMIT` 설정으로 주문 트랜잭션 완료 후 이벤트 처리
- 이벤트 객체에는 처리에 필요한 모든 정보를 포함하여 추가 조회 최소화

**주요 차이점**

- `@TransactionalEventListener`: 트랜잭션 상태와 연계되어 동작
  - `phase = TransactionPhase.AFTER_COMMIT`: 트랜잭션 완료 후 이벤트 처리
  - `phase = TransactionPhase.AFTER_ROLLBACK`: 트랜잭션 롤백 후 이벤트 처리
- `@EventListener`: 트랜잭션과 무관하게 즉시 이벤트 처리

### 3.3 이벤트 기반 다중 로직 분리 및 실습 전략

**다중 리스너 구성**

- 하나의 이벤트에 여러 리스너가 독립적으로 반응하도록 설계
- 각 리스너는 단일 책임을 가지며 다른 리스너의 실패에 영향받지 않음
- 실패 처리 전략:
  - 로깅 및 알림: 실패 정보 기록 및 운영팀 알림
  - 재시도: 실패한 작업에 대한 스케줄링된 재시도
  - 보상 트랜잭션: 일관성을 위한 보상 작업 수행

### 3.4 MSA 확장 대비 설계 원칙 & 보상 트랜잭션 전략

**SAGA 패턴 적용**

- 분산 트랜잭션 문제 해결을 위한 CHOREOGRAPHY 기반 SAGA 패턴 적용
- 실패 발생 시 역순으로 보상 트랜잭션 수행하여 일관성 유지
- 각 서비스는 자신의 보상 작업을 정의하고 관련 이벤트를 리스닝

**설계 원칙**

- 모든 작업은 언제든 실패할 수 있다고 가정
- 멱등성 보장: 동일 이벤트가 여러 번 처리되어도 결과는 같아야 함
- 모니터링 및 추적성 확보: 이벤트 흐름과 처리 상태를 추적할 수 있어야 함

## 4. 이점 및 고려사항

### 이점

- **관심사 분리**: 핵심 로직과 부가 로직이 명확히 분리됨
- **도메인 분리**: 주문 서비스는 자신의 책임만 수행
- **확장성**: 새로운 기능(예: 알림, 데이터 분석)은 새 리스너로 간단히 추가 가능
- **유연성**: 각 서비스는 독립적으로 개발/배포 가능
- **성능**: 부가 작업이 비동기로 처리되어 사용자 응답 시간 개선

### 고려사항

- **디버깅 복잡성**: 비동기 처리로 인한 흐름 추적 어려움
- **일관성 보장**: 최종 일관성(Eventual Consistency) 모델로 전환
- **중복 이벤트 처리**: 멱등성(Idempotency) 보장 필요
- **모니터링**: 이벤트 처리 상태 모니터링 체계 구축 필요

## 5. 결론

Facade 패턴과 이벤트 기반 아키텍처를 결합하여 시스템의 결합도를 낮추고 확장성을 높일 수 있습니다. 주문 처리 시스템은 CHOREOGRAPHY 패턴을 활용하여 각 서비스가 자율적으로 동작하면서도 전체 비즈니스 흐름을 유지할 수 있습니다. 메인 로직과 부가 로직의 명확한 분리는 시스템의 안정성과 유지보수성을 크게 향상시키며, 서비스 간 의존성을 최소화하는 데 도움이 됩니다.

## 참고 사항

- 위 설계는 일반적인 이벤트 기반 아키텍처의 패턴을 제시한 것으로, 실제 구현 시에는 프로젝트의 특성과 요구사항에 맞게 조정이 필요합니다.
- 이벤트 발행 외에도 메시징 큐(Kafka, RabbitMQ 등)를 활용한 확장 방안도 고려할 수 있습니다.
- 모든 비즈니스 로직을 이벤트로 처리하는 것은 오버엔지니어링이 될 수 있으므로, 적절한 경계 설정이 중요합니다.
