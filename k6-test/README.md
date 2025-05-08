# Redis 분산락 성능 테스트

이 폴더에는 Redis 기반 분산락 시스템의 성능과 안정성을 테스트하기 위한 k6 부하 테스트 환경이 포함되어 있습니다.

## 테스트 목적

- 동시성 제어 메커니즘의 동작 확인
- 분산락 획득/해제 성능 측정
- 락 경합 상황에서의 시스템 안정성 검증
- 분산락 획득 실패 시 적절한 오류 처리 확인

## 테스트 시나리오

다음 두 개의 테스트 스크립트를 통해 서로 다른 시나리오를 테스트합니다:

### 1. distributed-lock-test.js

주문 관련 기능의 분산락 테스트:

- **동일한 사용자의 동시 주문 생성**: 분산락 경합 시나리오
- **서로 다른 사용자의 주문 생성**: 분산락 경합이 적은 시나리오
- **주문 결제 처리**: 결제 과정에서의 분산락 동작 확인

### 2. coupon-lock-test.js

쿠폰 관련 기능의 분산락 테스트:

- **동일한 사용자의 동시 쿠폰 생성**: 분산락 경합 시나리오
- **여러 사용자의 쿠폰 발급**: 분산락 경합이 적은 시나리오
- **쿠폰 사용 테스트**: 쿠폰 사용 과정에서의 분산락 동작 확인

## 테스트 환경 구성

테스트 환경은 Docker Compose로 구성되어 있으며 다음 컨테이너를 포함합니다:

- **app**: 테스트 대상 애플리케이션 서버
- **redis**: 분산락을 위한 Redis 서버
- **k6**: 부하 테스트 도구
- **influxdb**: 테스트 결과 저장소
- **grafana**: 결과 시각화 대시보드

## 테스트 실행 방법

### 전체 환경 실행

Docker Compose 파일은 프로젝트 루트에 위치하고 있습니다:

```bash
cd 프로젝트_루트
docker-compose -f docker-compose.lock-test.yml up
```

### 특정 테스트만 실행

1. 환경 실행:

```bash
docker-compose -f docker-compose.lock-test.yml up -d redis app influxdb grafana
```

2. 주문 테스트 실행:

```bash
docker-compose -f docker-compose.lock-test.yml run k6 run /scripts/distributed-lock-test.js
```

3. 쿠폰 테스트 실행:

```bash
docker-compose -f docker-compose.lock-test.yml run k6 run /scripts/coupon-lock-test.js
```

### 결과 확인

Grafana 대시보드를 통해 테스트 결과를 확인할 수 있습니다:

- URL: http://localhost:3000
- 기본 대시보드: "k6 Performance Test"

## 메트릭 설명

### 주문 테스트 메트릭

- **successful_orders**: 성공적으로 생성된 주문 수
- **failed_orders**: 실패한 주문 생성 요청 수
- **lock_failures**: 락 획득 실패로 인한 오류 수
- **success_rate**: 주문 생성 성공률
- **order_creation_time**: 주문 생성 소요 시간

### 쿠폰 테스트 메트릭

- **successful_coupon_creations**: 성공적으로 생성된 쿠폰 수
- **failed_coupon_creations**: 실패한 쿠폰 생성 요청 수
- **lock_failures**: 락 획득 실패로 인한 오류 수
- **coupon_creation_success_rate**: 쿠폰 생성 성공률

## 테스트 성공 조건

- HTTP 요청 응답 시간: 95% 이상의 요청이 2초 미만
- 요청 실패율: 30% 미만
- 주문/쿠폰 생성 성공률: 70% 이상

## 문제 해결

- **컨테이너 시작 실패**: `docker-compose -f docker-compose.lock-test.yml logs [서비스명]`으로 로그 확인
- **애플리케이션 접속 불가**: `docker-compose -f docker-compose.lock-test.yml ps`로 컨테이너 상태 확인
- **테스트 중 오류 발생**: k6 로그 확인
