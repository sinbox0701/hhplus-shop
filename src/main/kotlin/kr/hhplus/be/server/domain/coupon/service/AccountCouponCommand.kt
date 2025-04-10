package kr.hhplus.be.server.domain.coupon.service

import java.time.LocalDateTime

class AccountCouponCommand {
    data class IssueCouponCommand(
        val id: Long,
        val couponStartDate: LocalDateTime,
        val couponEndDate: LocalDateTime
    )
    
}