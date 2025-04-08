package kr.hhplus.be.server.controller.order.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.server.domain.order.OrderStatus
import jakarta.validation.constraints.NotNull

data class OrderStatusUpdateRequest(
    @field:NotNull(message = "주문 상태는 필수입니다")
    @Schema(description = "주문 상태", example = "COMPLETED", allowableValues = ["PENDING", "COMPLETED", "CANCELLED"])
    val status: OrderStatus
) 