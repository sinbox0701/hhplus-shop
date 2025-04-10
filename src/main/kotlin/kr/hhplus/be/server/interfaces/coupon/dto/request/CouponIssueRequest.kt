package kr.hhplus.be.server.controller.coupon.dto.request

import jakarta.validation.constraints.*

data class CouponIssueRequest(
    @field:NotNull(message = "계정 ID는 필수입니다")
    @field:Min(value = 1L, message = "계정 ID는 1 이상이어야 합니다")
    val accountId: Long
) 