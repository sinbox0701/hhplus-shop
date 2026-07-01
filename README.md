## 프로젝트

Kotlin + Spring Boot + JPA로 구현한 이커머스 백엔드로, 항해플러스 백엔드 과정에서 진행한
개인 프로젝트입니다. 상품/주문/결제, 포인트 잔액, 선착순 쿠폰 발급 등 실제 커머스 도메인을
다루며, 특히 **동시성 제어**를 심화 주제로 삼아 재고 오버셀·잔액 불일치·쿠폰 초과 발급을
막는 락 전략(비관적 락 / 낙관적 락 / Redisson 분산 락)을 흐름별 권위 계층으로 분리해
설계했습니다. 금액은 부동소수점 오차를 피하기 위해 `BigDecimal`(DECIMAL(19,2))로 관리합니다.

> 참고: 컨트롤러 계층 패키지가 `interfaces/`, `presentation/`, `web/`로 나뉘어 있어
> 책임이 중복됩니다. 향후 `interfaces/` 하나로 통합할 여지가 있습니다(리팩터링 후보).

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```

## DOCS

### sequence diagram
- [point](./docs/sequence-diagram/point-scenario.md)
- [product](./docs/sequence-diagram/product-scenario.md)
- [coupon](./docs/sequence-diagram/coupon-scenario.md)
- [order-pay](./docs/sequence-diagram/order-pay-scenario.md)
- [popular-product](./docs/sequence-diagram/popular-product-scenario.md)

### erd
- [erd](./docs/erd/erd.md)

### class diagram
- [class-diagram](./docs/class-diagram/class-diagram.md)

### state diagram
- [state-diagram](./docs/state-diagram/state-diagram.md)


