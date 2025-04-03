package kr.hhplus.be.server.controller.order.dto.request

import jakarta.validation.constraints.*
import jakarta.validation.Valid

data class OrderCreateRequest(
    @field:NotNull(message = "사용자 ID는 필수입니다")
    @field:Min(value = 1L, message = "사용자 ID는 1 이상이어야 합니다")
    val accountId: Long,
    
    @field:NotNull(message = "사용자 쿠폰 ID는 필수입니다")
    @field:Min(value = 1L, message = "사용자 쿠폰 ID는 1 이상이어야 합니다")
    val accountCouponId: Long,
    
    @field:NotEmpty(message = "주문 상품 목록은 필수입니다")
    @field:Valid
    val orderItems: List<OrderItemRequest>
)

data class OrderItemRequest(
    @field:NotNull(message = "상품 ID는 필수입니다")
    @field:Min(value = 1L, message = "상품 ID는 1 이상이어야 합니다")
    val productId: Long,
    
    @field:NotNull(message = "상품 수량은 필수입니다")
    @field:Min(value = 1, message = "상품 수량은 최소 1개 이상이어야 합니다")
    @field:Max(value = 100, message = "상품 수량은 최대 100개까지 가능합니다")
    val quantity: Int,
    
    @field:NotNull(message = "상품 가격은 필수입니다")
    @field:DecimalMin(value = "100.0", message = "상품 가격은 최소 100원 이상이어야 합니다")
    val price: Double,
    
    @field:Min(value = 1L, message = "상품 옵션 ID는 1 이상이어야 합니다")
    val productOptionId: Long?
) 