package kr.hhplus.be.server.controller.coupon.dto.response

import java.time.LocalDateTime

data class CouponResponse(
    val couponId: Int,
    val discountRate: Double,
    val description: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val quantity: Int,
    val remainingQuantity: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) 