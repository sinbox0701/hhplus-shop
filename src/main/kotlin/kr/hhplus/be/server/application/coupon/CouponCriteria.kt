package kr.hhplus.be.server.application.coupon

import java.time.LocalDateTime
import kr.hhplus.be.server.domain.coupon.service.CouponCommand
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.user.model.User
import kr.hhplus.be.server.domain.coupon.model.Coupon


class CouponCriteria{
    data class CreateUserCouponCommand(
        val userId: Long,
        val code: String,
        val couponType: CouponType,
        val discountRate: Double,
        val description: String,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
        val quantity: Int,
        val userCouponQuantity: Int,
    ){
        fun toCreateCouponCommand(): CouponCommand.CreateCouponCommand{
            return CouponCommand.CreateCouponCommand(
                code = code,
                couponType = couponType,
                discountRate = discountRate,
                description = description,
                startDate = startDate,
                endDate = endDate,
                quantity = quantity,
            )
        }

        fun toUpdateCouponRemainingQuantityCommand(id: Long, quantity: Int): CouponCommand.UpdateCouponRemainingQuantityCommand{
            return CouponCommand.UpdateCouponRemainingQuantityCommand(
                id = id,
                quantity = quantity,
            )
        }

        fun toCreateUserCouponCommand(userId: Long, couponId: Long): CouponCommand.CreateUserCouponCommand{
            return CouponCommand.CreateUserCouponCommand(
                userId = userId,
                couponId = couponId,
                quantity = userCouponQuantity,
            )
        }
    }

    data class UpdateCouponCommand(
        val userId: Long,
        val couponId: Long,
    ){
        fun toIssueCouponCommand(id: Long, couponStartDate: LocalDateTime, couponEndDate: LocalDateTime): CouponCommand.IssueCouponCommand{
            return CouponCommand.IssueCouponCommand(
                id = id,
                couponStartDate = couponStartDate,
                couponEndDate = couponEndDate,
            )
        }
    }
}