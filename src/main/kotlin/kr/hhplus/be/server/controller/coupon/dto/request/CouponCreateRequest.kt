package kr.hhplus.be.server.controller.coupon.dto.request

import java.time.LocalDateTime
import jakarta.validation.constraints.*

data class CouponCreateRequest(
    @field:NotBlank(message = "쿠폰 설명은 필수입니다")
    @field:Size(min = 2, max = 100, message = "쿠폰 설명은 2자 이상 100자 이하여야 합니다")
    val description: String,
    
    @field:NotNull(message = "할인율은 필수입니다")
    @field:Min(value = 1, message = "할인율은 최소 1% 이상이어야 합니다")
    @field:Max(value = 100, message = "할인율은 최대 100%까지 가능합니다")
    val discountRate: Double,
    
    @field:NotNull(message = "시작일은 필수입니다")
    val startDate: LocalDateTime,
    
    @field:NotNull(message = "종료일은 필수입니다")
    val endDate: LocalDateTime,
    
    @field:NotNull(message = "수량은 필수입니다")
    @field:Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
    val quantity: Int
) 