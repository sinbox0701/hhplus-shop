# 1. 상품 조회 성능 개선 사례 분석

## 문제 상황

### 1. ProductFacade.getTopSellingProducts 메소드의 성능 이슈

- **N+1 쿼리 문제**: 각 상품 ID마다 `productService.get(productId)`를 호출하여 개별 조회 수행
- **대량 데이터 처리**: 모든 주문 데이터를 조회하여 실시간으로 판매량 계산
- **비효율적인 대체 로직**: 판매 데이터가 없을 경우 모든 상품을 메모리에 로드한 후 5개만 반환
- **시간 복잡도**: 상품 정렬 과정에서 O(n²) 시간 복잡도 발생

```kotlin
// 기존 코드의 문제
return topSellingProductIds.mapNotNull { productId ->
    try {
        productService.get(productId)  // N+1 문제 발생
    } catch (e: Exception) {
        null
    }
}
```

## 해결 방안

### 1. 집계 테이블 도입

- **집계 테이블 생성**: `ProductDailySales` 엔티티와 테이블 추가
- **배치 작업 구현**: `ProductSalesAggregationService`에서 매일 자정에 전날 판매 데이터 집계
- **효과**: 대량의 주문 데이터를 매번 조회하지 않고 미리 계산된 결과 활용

### 2. 일괄 조회 패턴 적용

- **ID 리스트 조회**: `findByIds()` 메소드로 한 번에 모든 상품 조회
- **맵 기반 조회**: Map을 활용하여 조회 시간 복잡도 개선

```kotlin
// 개선된 코드
val productsMap = products.associateBy { it.id }
return topSellingProductIds.mapNotNull { productsMap[it] }
```

### 3. 레이어 분리 및 책임 명확화

- **도메인 레이어**:
  - `ProductSalesService` - 단일 레포지토리만 의존하는 순수한 도메인 서비스
  - `ProductService` - ProductRepository만 의존
- **애플리케이션 레이어**:
  - `ProductFacade` - 여러 도메인 서비스를 조합하여 비즈니스 기능 구현
  - `ProductSalesAggregationService` - 여러 도메인 서비스 간 데이터 집계 담당

## 개선 결과

### 1. 성능 향상

- **쿼리 감소**: N+1 문제 해결로 데이터베이스 부하 감소
- **시간 복잡도 개선**: O(n²) → O(n)으로 개선
- **캐싱 효과**: 집계 테이블이 일종의 캐시 역할을 수행하여 반복 계산 방지

### 2. 코드 구조 개선

- **단일 책임 원칙**: 각 서비스가 명확한 책임을 가짐
- **테스트 용이성**: 의존성이 명확하게 분리되어 단위 테스트 작성 용이
- **확장성**: 새로운 기능 추가나 변경이 특정 레이어에만 영향을 미침

## 결론

해당 리팩토링을 통해 조회 성능 문제를 해결하는 동시에 코드의 구조적 개선도 이루었습니다. 특히 배치 처리와 일괄 조회 패턴의 조합은 대규모 데이터 처리에 효과적인 접근 방식임을 확인할 수 있었습니다. 이러한 패턴은 다른 성능 이슈가 있는 부분에도 적용할 수 있는 좋은 사례가 될 것입니다.

# 2. 상품 및 옵션 조회 성능 개선 사례 분석

## 문제 상황

### 1. ProductFacade.getAllProductsWithOptions 메소드의 성능 이슈

- **N+1 쿼리 문제**: 모든 상품을 조회한 후 각 상품마다 개별적으로 옵션 조회 쿼리 실행
- **쿼리 폭발**: 상품 수가 많을수록 쿼리 수가 선형적으로 증가 (1,000개 상품 → 1,001개 쿼리)
- **서버-DB 부하 증가**: 다수의 통신으로 인한 네트워크 병목 현상
- **응답 시간 지연**: 상품 수에 비례하여 응답 시간이 늘어나는 문제

```kotlin
// 기존 코드의 문제
@Transactional(readOnly = true)
fun getAllProductsWithOptions(): List<ProductResult.ProductWithOptions> {
    return productService.getAll().map { product ->
        // 각 상품마다 별도 쿼리로 옵션 조회 (N+1 문제)
        ProductResult.ProductWithOptions(product, productOptionService.getAllByProductId(product.id!!))
    }
}
```

## 해결 방안

### 1. 인덱스 추가

- **어노테이션 기반 인덱스 정의**: ProductOptionEntity에 product_id 컬럼 인덱스 추가

```kotlin
@Entity
@Table(
    name = "product_options",
    indexes = [
        Index(name = "idx_product_option_product_id", columnList = "product_id")
    ]
)
class ProductOptionEntity(...)
```

### 2. 일괄 조회 패턴 적용

- **IN 쿼리 활용**: 모든 제품 ID에 대한 옵션을 한 번의 쿼리로 조회
- **메모리 내 결합**: 조회된 옵션을 메모리에서 상품과 매핑

```kotlin
// 개선된 코드
val products = productService.getAll()
val productIds = products.mapNotNull { it.id }
val allOptions = productOptionService.getAllByProductIds(productIds)
val optionsByProductId = allOptions.groupBy { it.productId }
```

### 3. 레이어 수정

- **도메인 서비스 확장**: ProductOptionService에 일괄 조회 메소드 추가
- **레포지토리 확장**: ProductOptionRepository에 ID 리스트 조회 기능 추가
- **JPA 인터페이스 확장**: JpaProductOptionRepository에 findByProductIdIn 메소드 추가

## 개선 결과

### 1. 성능 향상

- **쿼리 감소**: N+1개 쿼리 → 2개 쿼리로 감소 (98% 이상 쿼리 감소)
- **응답 시간 개선**: 상품 수에 관계없이 일정한 성능 보장
- **DB 부하 감소**: 커넥션 풀 사용량 감소 및 DB 자원 효율화
- **확장성 개선**: 더 많은 상품이 추가되어도 성능 저하 없음

### 2. 코드 구조 개선

- **FK 관계 없이 구현**: 엔티티 간 관계를 정의하지 않고도 성능 최적화 구현
- **테스트 작성 용이성**: 명확한 메소드 호출 패턴으로 테스트 작성 용이
- **지연 로딩 활용**: JPA의 지연 로딩 전략을 그대로 유지하면서 성능 개선

## 결론

일괄 조회 패턴과 인덱스 최적화를 통해 FK 관계 정의 없이도 N+1 문제를 효과적으로 해결할 수 있었습니다. 이 패턴은 데이터 모델 변경 없이 애플리케이션 레이어에서 성능을 최적화할 수 있어, 기존 시스템에 쉽게 적용할 수 있는 장점이 있습니다. 특히 상품 카탈로그와 같이 조회가 빈번한 기능에서 사용자 경험을 크게 향상시킬 수 있었으며, 다른 유사한 관계 조회에도 동일한 패턴을 적용할 수 있습니다.
