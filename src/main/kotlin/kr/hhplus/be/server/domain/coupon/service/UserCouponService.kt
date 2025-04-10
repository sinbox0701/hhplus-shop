package kr.hhplus.be.server.domain.coupon.service

import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import kr.hhplus.be.server.domain.coupon.service.UserCouponCommand
import org.springframework.stereotype.Service

@Service
class UserCouponService(private val userCouponRepository: UserCouponRepository) {
    fun findById(id: Long): UserCoupon {
        return userCouponRepository.findById(id)
            ?: throw IllegalArgumentException("UserCoupon not found with id: $id")
    }

    fun findByUserId(userId: Long): List<UserCoupon> {
        return userCouponRepository.findByUserId(userId)
    }

    fun findByCouponId(couponId: Long): List<UserCoupon> {
        return userCouponRepository.findByCouponId(couponId)
    }

    fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon? {
        return userCouponRepository.findByUserIdAndCouponId(userId, couponId)
    }

    fun issue(command: UserCouponCommand.IssueCouponCommand) {
        val userCoupon = findById(command.id)
        userCoupon.issue(command.couponStartDate, command.couponEndDate)
        userCouponRepository.save(userCoupon)
    }

    fun use(id: Long) {
        val userCoupon = findById(id)
        userCoupon.use()
        userCouponRepository.save(userCoupon)
    }

    fun delete(id: Long) {
        userCouponRepository.delete(id)
    }

    fun deleteAllByUserId(userId: Long) {
        val userCoupons = userCouponRepository.findByUserId(userId)
        userCoupons.forEach { userCoupon ->
            userCouponRepository.delete(userCoupon.id!!)
        }
    }

    fun deleteAllByCouponId(couponId: Long) {
        val userCoupons = userCouponRepository.findByCouponId(couponId)
        userCoupons.forEach { userCoupon ->
            userCouponRepository.delete(userCoupon.id!!)
        }
    }
}