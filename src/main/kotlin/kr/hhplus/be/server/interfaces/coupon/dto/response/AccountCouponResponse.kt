package kr.hhplus.be.server.controller.coupon.dto.response

import java.time.LocalDateTime

data class AccountCouponResponse(
    val id: Long,
    val accountId: Long,
    val couponId: Long,
    val issueDate: LocalDateTime,
    val issued: Boolean,
    val used: Boolean,
    val coupon: CouponResponse? = null
) 