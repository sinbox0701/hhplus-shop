package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.UserCouponService
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponFacade(
    private val userCouponService: UserCouponService,
    private val couponService: CouponService,
    private val userService: UserService
) {
    @Transactional()
    fun create(criteria: CouponCriteria.CreateUserCouponCommand): UserCoupon{
        val user = userService.findById(criteria.userId)
        val coupon = couponService.create(criteria.toCreateCouponCommand())
        val userCoupon = userCouponService.create(criteria.toCreateUserCouponCommand(user, coupon))
        couponService.updateRemainingQuantity(criteria.toUpdateCouponRemainingQuantityCommand(coupon.id!!, 1))
        return userCoupon
    }

    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): List<CouponResult.UserCouponResult>{
        val user = userService.findById(userId)
        val userCoupons = userCouponService.findByUserId(userId)
        val couponIds = userCoupons.mapNotNull { it.coupon.id }
        val coupons = couponService.findAllByIds(couponIds)
        return userCoupons.map { CouponResult.UserCouponResult.from(it, user, coupons.find { coupon -> coupon.id == it.coupon.id } ?: it.coupon) }
    }

    @Transactional(readOnly = true)
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): CouponResult.UserCouponResult{
        val user = userService.findById(userId)
        val coupon = couponService.findById(couponId)
        val userCoupon = userCouponService.findByUserIdAndCouponId(userId, couponId)
        return CouponResult.UserCouponResult.from(userCoupon, user, coupon)
    }

    @Transactional()
    fun issue(criteria: CouponCriteria.UpdateCouponCommand) {
        val coupon = couponService.findById(criteria.couponId)
        val userCoupon = userCouponService.findByUserIdAndCouponId(criteria.userId, criteria.couponId)
        userCouponService.issue(criteria.toIssueCouponCommand(userCoupon.id!!, coupon.startDate, coupon.endDate))
    }

    @Transactional()
    fun use(criteria: CouponCriteria.UpdateCouponCommand) {
        userService.findById(criteria.userId)
        couponService.findById(criteria.couponId)
        val userCoupon = userCouponService.findByUserIdAndCouponId(criteria.userId, criteria.couponId)
        userCouponService.use(userCoupon.id!!)
    }

    @Transactional()
    fun deleteByUserIdAndCouponId(criteria: CouponCriteria.UpdateCouponCommand) {
        userService.findById(criteria.userId)
        couponService.findById(criteria.couponId)
        userCouponService.deleteByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.delete(criteria.couponId)
    }

    @Transactional()
    fun deleteAllByUserId(userId: Long) {
        userService.findById(userId)
        val couponIds = userCouponService.findByUserId(userId).mapNotNull { it.coupon.id }
        couponService.deleteAll(couponIds)
        userCouponService.deleteAllByUserId(userId)
    }
}
