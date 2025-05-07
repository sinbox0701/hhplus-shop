package kr.hhplus.be.server.application.coupon

import kr.hhplus.be.server.application.coupon.CouponCriteria
import kr.hhplus.be.server.application.coupon.CouponResult
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.service.CouponService
import kr.hhplus.be.server.domain.user.service.UserService
import kr.hhplus.be.server.shared.lock.DistributedLock
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponFacade(
    private val couponService: CouponService,
    private val userService: UserService,
    private val transactionHelper: TransactionHelper
) {
    @DistributedLock(
        domain = LockKeyConstants.COUPON_PREFIX,
        resourceType = LockKeyConstants.RESOURCE_USER,
        resourceIdExpression = "criteria.userId",
        timeout = LockKeyConstants.EXTENDED_TIMEOUT
    )
    fun create(criteria: CouponCriteria.CreateUserCouponCommand): UserCoupon {
        return transactionHelper.executeInTransaction {
            val user = userService.findById(criteria.userId)
            val coupon = couponService.create(criteria.toCreateCouponCommand())
            val userCoupon = couponService.createUserCoupon(criteria.toCreateUserCouponCommand(user.id!!, coupon.id!!))
            couponService.updateRemainingQuantity(criteria.toUpdateCouponRemainingQuantityCommand(coupon.id, 1))
            userCoupon
        }
    }

    @Transactional(readOnly = true)
    fun findByUserId(userId: Long): List<CouponResult.UserCouponResult>{
        val user = userService.findById(userId)
        val userCoupons = couponService.findUserCouponsByUserId(userId)
        val couponIds = userCoupons.mapNotNull { it.couponId }
        val coupons = couponService.findAllByIds(couponIds)
        return userCoupons.mapNotNull { userCoupon ->
            val coupon = coupons.find { coupon -> coupon.id == userCoupon.couponId }
            coupon?.let { CouponResult.UserCouponResult.from(userCoupon, it) }
        }
    }

    @Transactional(readOnly = true)
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): CouponResult.UserCouponResult{
        val coupon = couponService.findById(couponId)
        val userCoupon = couponService.findUserCouponByUserIdAndCouponId(userId, couponId)
        return CouponResult.UserCouponResult.from(userCoupon, coupon)
    }

    @Transactional()
    fun issue(criteria: CouponCriteria.UpdateCouponCommand) {
        val coupon = couponService.findById(criteria.couponId)
        val userCoupon = couponService.findUserCouponByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.issueUserCoupon(criteria.toIssueCouponCommand(userCoupon.id!!, coupon.startDate, coupon.endDate))
    }

    @Transactional()
    fun use(criteria: CouponCriteria.UpdateCouponCommand) {
        userService.findById(criteria.userId)
        couponService.findById(criteria.couponId)
        val userCoupon = couponService.findUserCouponByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.useUserCoupon(userCoupon.id!!)
    }

    @Transactional()
    fun deleteByUserIdAndCouponId(criteria: CouponCriteria.UpdateCouponCommand) {
        userService.findById(criteria.userId)
        couponService.findById(criteria.couponId)
        couponService.deleteUserCouponByUserIdAndCouponId(criteria.userId, criteria.couponId)
        couponService.delete(criteria.couponId)
    }

    @Transactional()
    fun deleteAllByUserId(userId: Long) {
        userService.findById(userId)
        val couponIds = couponService.findUserCouponsByUserId(userId).mapNotNull { it.couponId }
        couponService.deleteAll(couponIds)
        couponService.deleteAllUserCouponByUserId(userId)
    }
}
