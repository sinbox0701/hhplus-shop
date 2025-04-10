package kr.hhplus.be.server.domain.coupon.repository

import kr.hhplus.be.server.domain.coupon.model.UserCoupon

interface UserCouponRepository {
    fun save(userCoupon: UserCoupon): UserCoupon
    fun findById(id: Long): UserCoupon?
    fun findByUserId(userId: Long): List<UserCoupon>
    fun findByCouponId(couponId: Long): List<UserCoupon>
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon?
    fun update(userCoupon: UserCoupon): UserCoupon
    fun delete(id: Long)
}
