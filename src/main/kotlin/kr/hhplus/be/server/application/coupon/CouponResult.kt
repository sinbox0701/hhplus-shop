package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import java.time.LocalDateTime

class CouponResult {
    data class UserCouponResult(
        val id: Long?,
        val userId: Long?,
        val couponId: Long?,
        val code: String,
        val type: CouponType,
        val discountRate: Double,
        val description: String,
        val issuedDate: LocalDateTime,
        val isIssued: Boolean,
        val isUsed: Boolean,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
       
    ) {
        companion object {
            fun from(userCoupon: UserCoupon, coupon: Coupon): UserCouponResult {
                return UserCouponResult(
                    id = userCoupon.id,
                    userId = userCoupon.userId,
                    couponId = coupon.id,
                    code = coupon.code,
                    type = coupon.couponType,
                    discountRate = coupon.discountRate,
                    description = coupon.description,
                    issuedDate = userCoupon.issueDate,
                    isIssued = userCoupon.issued,
                    isUsed = userCoupon.used,
                    startDate = coupon.startDate,
                    endDate = coupon.endDate,
                )
            }
        }
    }
}