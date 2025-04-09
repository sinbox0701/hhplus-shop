package kr.hhplus.be.server.interfaces.order

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.order.model.OrderStatus

class OrderRequest {
    
    data class OrderCreateRequest(
        @field:NotNull(message = "계정 ID는 필수입니다.")
        val accountId: Long,
        
        val accountCouponId: Long? = null,
        
        val couponDiscountRate: Double? = null,
        
        @field:NotEmpty(message = "주문 상품 목록은 비어있을 수 없습니다.")
        @field:Valid
        val orderItems: List<OrderItemCreateRequest>
    )
    
    data class OrderItemCreateRequest(
        @field:NotNull(message = "상품 ID는 필수입니다.")
        val productId: Long,
        
        @field:NotNull(message = "상품 옵션 ID는 필수입니다.")
        val productOptionId: Long,
        
        @field:NotNull(message = "수량은 필수입니다.")
        @field:Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다.")
        val quantity: Int
    ) 

    data class OrderPaymentRequest(
        @field:NotNull(message = "계정 ID는 필수입니다.")
        val accountId: Long
    ) 

    data class OrderStatusUpdateRequest(
        @field:NotNull(message = "주문 상태는 필수입니다")
        @Schema(description = "주문 상태", example = "COMPLETED", allowableValues = ["PENDING", "COMPLETED", "CANCELLED"])
        val status: OrderStatus
    ) 
}