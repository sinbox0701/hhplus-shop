package kr.hhplus.be.server.interfaces.coupon

import java.time.LocalDateTime
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponResult

class CouponResponse{
    data class Response(
        val id: Long,
        val discountRate: Double,
        val description: String,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val quantity: Int,
        val remainingQuantity: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime
    ) {
        companion object {
            fun from(coupon: Coupon): Response {
                return Response(
                    id = coupon.id ?: 0,
                    discountRate = coupon.discountRate,
                    description = coupon.description,
                    startDate = coupon.startDate,
                    endDate = coupon.endDate,
                    quantity = coupon.quantity,
                    remainingQuantity = coupon.remainingQuantity,
                    createdAt = coupon.createdAt,
                    updatedAt = coupon.updatedAt
                )
            }
        }
    }

    data class AccountCouponResponse(
        val id: Long,
        val accountId: Long,
        val couponId: Long,
        val issueDate: LocalDateTime,
        val issued: Boolean,
        val used: Boolean,
        val coupon: Response? = null
    ) {
        companion object {
            fun from(userCouponResult: CouponResult.UserCouponResult): AccountCouponResponse {
                return AccountCouponResponse(
                    id = userCouponResult.id ?: 0,
                    accountId = userCouponResult.userId ?: 0,
                    couponId = userCouponResult.couponId ?: 0,
                    issueDate = userCouponResult.issuedDate,
                    issued = userCouponResult.isIssued,
                    used = userCouponResult.isUsed,
                    coupon = Response(
                        id = userCouponResult.couponId ?: 0,
                        discountRate = userCouponResult.discountRate,
                        description = userCouponResult.description,
                        startDate = userCouponResult.startDate,
                        endDate = userCouponResult.endDate,
                        quantity = 1, // 유저에게 발급되는 쿠폰은 1개
                        remainingQuantity = if (userCouponResult.isUsed) 0 else 1,
                        createdAt = userCouponResult.issuedDate,
                        updatedAt = userCouponResult.issuedDate
                    )
                )
            }

            fun from(userCoupon: UserCoupon, coupon: Coupon): AccountCouponResponse {
                return AccountCouponResponse(
                    id = userCoupon.id ?: 0,
                    accountId = userCoupon.user.id ?: 0,
                    couponId = coupon.id ?: 0,
                    issueDate = userCoupon.issueDate,
                    issued = userCoupon.issued,
                    used = userCoupon.used,
                    coupon = Response.from(coupon)
                )
            }
        }
    }
}