package kr.hhplus.be.server.domain.coupon.service

import kr.hhplus.be.server.domain.coupon.service.CouponCommand
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import kr.hhplus.be.server.domain.coupon.service.UserCouponCommand
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) {
    fun create(command: CouponCommand.CreateCouponCommand): Coupon {
        val coupon = Coupon.create(
            code = command.code,
            couponType = command.couponType,
            discountRate = command.discountRate,
            description = command.description,
            startDate = command.startDate,
            endDate = command.endDate,
            quantity = command.quantity,
        )
        return couponRepository.save(coupon)
    }

    fun findAll(): List<Coupon> {
        return couponRepository.findAll()
    }

    fun findAllByIds(ids: List<Long>): List<Coupon> {
        return ids.map { id -> findById(id) }
    }

    fun findById(id: Long): Coupon {
        return couponRepository.findById(id)
            ?: throw IllegalArgumentException("Coupon not found with id: $id")
    }

    fun findByIds(ids: List<Long>): List<Coupon> {
        return ids.map { id -> findById(id) }
    }

    fun findByCode(code: String): Coupon {
        return couponRepository.findByCode(code)
            ?: throw IllegalArgumentException("Coupon not found with code: $code")
    }

    fun findByType(type: CouponType): List<Coupon> {
        return couponRepository.findByType(type)
    }

    fun update(command: CouponCommand.UpdateCouponCommand): Coupon {
        val coupon = findById(command.id)
        coupon.update(command.discountRate, command.description, command.startDate, command.endDate, command.quantity)
        return couponRepository.save(coupon)
    }

    fun updateRemainingQuantity(command: CouponCommand.UpdateCouponRemainingQuantityCommand): Coupon {
        val coupon = findById(command.id)
        coupon.decreaseQuantity(command.quantity)
        return couponRepository.save(coupon)
    }

    fun delete(id: Long) {
        couponRepository.delete(id)
    }

    fun deleteAll(ids: List<Long>) {
        ids.forEach { id ->
            delete(id)
        }
    }

    fun createUserCoupon(command: UserCouponCommand.CreateUserCouponCommand): UserCoupon {
        val userCoupon = UserCoupon.create(command.user, command.coupon, command.quantity)
        return userCouponRepository.save(userCoupon)
    }

    fun findUserCouponById(id: Long): UserCoupon {
        return userCouponRepository.findById(id)
            ?: throw IllegalArgumentException("UserCoupon not found with id: $id")
    }

    fun findUserCouponUserCouponByUserId(userId: Long): List<UserCoupon> {
        return userCouponRepository.findByUserId(userId)
    }

    fun findUserCouponByCouponId(couponId: Long): List<UserCoupon> {
        return userCouponRepository.findByCouponId(couponId)
    }

    fun findUserCouponByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon {
        return userCouponRepository.findByUserIdAndCouponId(userId, couponId)
            ?: throw IllegalArgumentException("UserCoupon not found with userId: $userId and couponId: $couponId")
    }

    fun issueUserCoupon(command: UserCouponCommand.IssueCouponCommand) {
        val userCoupon = findUserCouponById(command.id)
        userCoupon.issue(command.couponStartDate, command.couponEndDate)
        userCouponRepository.save(userCoupon)
    }

    fun useUserCoupon(id: Long) {
        val userCoupon = findUserCouponById(id)
        userCoupon.use()
        userCouponRepository.save(userCoupon)
    }

    fun deleteUserCoupon(id: Long) {
        userCouponRepository.delete(id)
    }

    fun deleteUserCouponByUserIdAndCouponId(userId: Long, couponId: Long) {
        userCouponRepository.deleteByUserIdAndCouponId(userId, couponId)
    }

    fun deleteAllUserCouponByUserId(userId: Long) {
        val userCoupons = userCouponRepository.findByUserId(userId)
        userCoupons.forEach { userCoupon ->
            userCouponRepository.delete(userCoupon.id!!)
        }
    }

    fun deleteAllUserCouponByCouponId(couponId: Long) {
        val userCoupons = userCouponRepository.findByCouponId(couponId)
        userCoupons.forEach { userCoupon ->
            userCouponRepository.delete(userCoupon.id!!)
        }
    }
}