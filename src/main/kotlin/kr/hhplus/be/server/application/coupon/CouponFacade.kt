package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.UserCouponService
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.user.service.UserService
import org.springframework.stereotype.Service

@Service
class CouponFacade(
    private val userCouponService: UserCouponService,
    private val couponService: CouponService,
    private val userService: UserService
) {
    
}
