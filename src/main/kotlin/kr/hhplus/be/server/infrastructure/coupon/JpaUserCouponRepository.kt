package kr.hhplus.be.server.infrastructure.coupon

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserCouponRepository : JpaRepository<UserCouponEntity, Long> {
    fun findByUserId(userId: Long): List<UserCouponEntity>
    fun findByCouponId(couponId: Long): List<UserCouponEntity>
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCouponEntity?
    fun deleteByUserIdAndCouponId(userId: Long, couponId: Long)
} 