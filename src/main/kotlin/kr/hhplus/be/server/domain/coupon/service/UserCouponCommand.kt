package kr.hhplus.be.server.domain.coupon.service

import java.time.LocalDateTime
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.user.model.User

class UserCouponCommand {
    
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