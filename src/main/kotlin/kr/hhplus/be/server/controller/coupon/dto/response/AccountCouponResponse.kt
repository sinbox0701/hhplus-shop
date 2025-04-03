package kr.hhplus.be.server.controller.coupon.dto.response

import java.time.LocalDateTime

data class AccountCouponResponse(
    val accountCouponId: Int,
    val accountId: Int,
    val couponId: Int,
    val issueDate: LocalDateTime,
    val issued: Boolean,
    val used: Boolean,
    val coupon: CouponResponse? = null
) 