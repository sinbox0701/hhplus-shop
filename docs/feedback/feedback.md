# [리뷰 포인트]

## 1. OrderFacadeIntegrationTest 관련 피드백

- Fixture 는 별도 파일로 분리해주세요. 그리고 Fixture도 조립하여 중복을 제거할 수 있어요. 그리고 테스트 의도에 따라 별도의 값이 세팅되는게 아닌 경우는 BeforeEach에서 초기화해도 좋겠네요.
- 만료된 쿠폰으로 인한 주문 실패나 권한없는 사용자의 주문 조회 요청, 혹은 주문 완료 상태나 최대 주문 수량 초과 등의 정책상 필요해보는 케이스들은 있으나, 전반적으로 충분하다고 생각됩니다. 다만, 단위 테스트로 옮겨져야 할 테스트들은 보이네요. 파사드 역할과 책임의 범주인지 고민해보시면 좋겠네요.

# [그외 피드백]

## 1. 전반적으로 리팩토링이 필요해보입니다.

- 메서드 내에서 추상화 수준이 다릅니다.
- 값을 꺼내서 연산하거나 if 조건 분기를 탄다면, 메시지를 요청하도록 리팩토링하세요.

```kotlin
@Transactional
fun createOrder(criteria: OrderCriteria.OrderCreateCriteria): OrderResult.OrderWithItems {
val orderContext = orderService.prepareOrder(
userId = criteria.userId,
items = criteria.orderItems,
userCouponId = criteria.userCouponId
)

    val order = orderService.createOrder(orderContext)

    val orderItems = orderItemService.create(
        orderId = order.id!!,
        items = orderContext.validatedItems
    )

    return OrderResult.OrderWithItems(order, orderItems)

}
```

- 테스트하기 어려운 부분은 의존성을 주입해보세요. 가령, 시간에 의존적인 코드는 테스트하기 어려우므로 아래와 같이 구성해볼 수도 있을거에요.

```kotlin
interface TimeProvider {
    fun now(): LocalDateTime
    fun today(): LocalDate
}

@Component
class DefaultTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun today(): LocalDate = LocalDate.now()
}

class FixedTimeProvider(
    private val fixedDateTime: LocalDateTime
) : TimeProvider {
    override fun now(): LocalDateTime = fixedDateTime
    override fun today(): LocalDate = fixedDateTime.toLocalDate()
}
```

- 리플렉션을 사용한 매핑보다는 명시적으로 변환하면 되지 않을까요..?

```kotlin
// OrderEntity.kt
fun toOrder(): Order {
    return Order.withId(
        id = this.id,
        userId = this.userId,
        userCouponId = this.userCouponId,
        totalPrice = this.totalPrice,
        status = this.status,
        orderDate = this.orderDate,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

// Order.kt의 companion object에 추가
fun withId(id: Long?, userId: Long, ...): Order {
    return Order(id = id, userId = userId, ...)
}
```

## 2. 인덱스도 고려해보세요.

```sql
-- 인기 판매 상품 조회를 위한 인덱스
CREATE INDEX idx_product_daily_sales_date_quantity
ON product_daily_sales (sales_date, quantity_sold DESC);

-- 사용자별 주문 조회를 위한 인덱스
CREATE INDEX idx_orders_user_id_order_date
ON orders (user_id, order_date DESC);

-- 상품 옵션 검색을 위한 인덱스
CREATE INDEX idx_product_options_product_id
ON product_options (product_id);

...
```
