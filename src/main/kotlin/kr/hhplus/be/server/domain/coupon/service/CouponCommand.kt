package kr.hhplus.be.server.domain.coupon.service

import kr.hhplus.be.server.domain.coupon.model.CouponType
import java.time.LocalDateTime
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.user.model.User

class CouponCommand {
    data class CreateCouponCommand(
        val code: String,
        val couponType: CouponType,
        val discountRate: Double,
        val description: String,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val quantity: Int,
    )

    data class UpdateCouponCommand(
        val id: Long,
        val discountRate: Double? = null,
        val description: String? = null,
        val startDate: LocalDateTime? = null,
        val endDate: LocalDateTime? = null,
        val quantity: Int? = null,
    )

    data class UpdateCouponRemainingQuantityCommand(
        val id: Long,
        val quantity: Int,
    )

    data class CreateUserCouponCommand(
        val user: User,
        val coupon: Coupon,
        val quantity: Int
    )
    
    data class IssueCouponCommand(
        val id: Long,
        val couponStartDate: LocalDateTime,
        val couponEndDate: LocalDateTime
    )
}