package kr.hhplus.be.server.controller.coupon.dto.request

import java.time.LocalDateTime
import jakarta.validation.constraints.*

data class CouponUpdateRequest(
    @field:Size(min = 2, max = 30, message = "쿠폰 설명은 2자 이상 30자 이하여야 합니다")
    val description: String? = null,
    
    @field:Min(value = 1, message = "할인율은 최소 1% 이상이어야 합니다")
    @field:Max(value = 100, message = "할인율은 최대 100%까지 가능합니다")
    val discountRate: Double? = null,
    
    val startDate: LocalDateTime? = null,
    
    val endDate: LocalDateTime? = null,
    
    @field:Min(value = 1, message = "수량은 최소 1개 이상이어야 합니다")
    @field:Max(value = 100, message = "수량은 최대 100개까지 가능합니다")
    val quantity: Int? = null
) 