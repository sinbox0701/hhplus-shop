package kr.hhplus.be.server.domain.coupon.service

import kr.hhplus.be.server.domain.common.TimeProvider
import kr.hhplus.be.server.domain.coupon.service.CouponCommand
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import kr.hhplus.be.server.domain.coupon.model.UserCoupon
import kr.hhplus.be.server.domain.coupon.repository.UserCouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val timeProvider: TimeProvider
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
            timeProvider = timeProvider
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
        val updatedCoupon = coupon.update(
            command.discountRate, 
            command.description, 
            command.startDate, 
            command.endDate, 
            command.quantity,
            timeProvider
        )
        return couponRepository.save(updatedCoupon)
    }

    fun updateRemainingQuantity(command: CouponCommand.UpdateCouponRemainingQuantityCommand): Coupon {
        val coupon = findById(command.id)
        val updatedCoupon = coupon.decreaseQuantity(command.quantity, timeProvider)
        return couponRepository.save(updatedCoupon)
    }

    fun delete(id: Long) {
        couponRepository.delete(id)
    }

    fun deleteAll(ids: List<Long>) {
        ids.forEach { id ->
            delete(id)
        }
    }

    fun createUserCoupon(command: CouponCommand.CreateUserCouponCommand): UserCoupon {
        val userCoupon = UserCoupon.create(command.userId, command.couponId, command.quantity)
        return userCouponRepository.save(userCoupon)
    }

    fun findUserCouponById(id: Long): UserCoupon {
        return userCouponRepository.findById(id)
            ?: throw IllegalArgumentException("UserCoupon not found with id: $id")
    }

    fun findUserCouponsByUserId(userId: Long): List<UserCoupon> {
        return userCouponRepository.findByUserId(userId)
    }

    fun findUserCouponByCouponId(couponId: Long): List<UserCoupon> {
        return userCouponRepository.findByCouponId(couponId)
    }

    fun findUserCouponByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon {
        return userCouponRepository.findByUserIdAndCouponId(userId, couponId)
            ?: throw IllegalArgumentException("UserCoupon not found with userId: $userId and couponId: $couponId")
    }

    fun issueUserCoupon(command: CouponCommand.IssueCouponCommand) {
        val userCoupon = findUserCouponById(command.id)
        val updatedUserCoupon = userCoupon.issue(command.couponStartDate, command.couponEndDate, timeProvider)
        userCouponRepository.save(updatedUserCoupon)
    }

    fun useUserCoupon(id: Long) {
        val userCoupon = findUserCouponById(id)
        val updatedUserCoupon = userCoupon.use()
        userCouponRepository.save(updatedUserCoupon)
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
    
    fun isValidCoupon(couponId: Long): Boolean {
        val coupon = findById(couponId)
        return coupon.isValid(timeProvider)
    }

    /**
     * 비관적 락을 사용하여 선착순 쿠폰 발급
     * 쿠폰 발급 시 남은 수량을 확인하고 동시에 감소시키는 작업을 안전하게 수행
     */
    @Transactional
    fun issueFirstComeFirstServedCoupon(userId: Long, couponCode: String): UserCoupon {
        // 비관적 락을 사용하여 쿠폰 조회
        val coupon = couponRepository.findByCodeWithPessimisticLock(couponCode)
            ?: throw IllegalArgumentException("쿠폰을 찾을 수 없습니다: $couponCode")
        
        // 유효한 쿠폰인지 확인
        if (!coupon.isValid(timeProvider)) {
            throw IllegalStateException("유효하지 않은 쿠폰입니다: $couponCode")
        }
        
        // 남은 수량이 있는지 확인
        if (!coupon.hasRemainingQuantity()) {
            throw IllegalStateException("쿠폰 수량이 모두 소진되었습니다: $couponCode")
        }
        
        // 이미 발급받은 쿠폰인지 확인
        val existingUserCoupon = userCouponRepository.findByUserIdAndCouponId(userId, coupon.id!!)
        if (existingUserCoupon != null) {
            throw IllegalStateException("이미 발급받은 쿠폰입니다: $couponCode")
        }
        
        // 쿠폰 수량 감소
        val updatedCoupon = coupon.decreaseQuantity(1, timeProvider)
        couponRepository.update(updatedCoupon)
        
        // 유저 쿠폰 생성
        val userCoupon = UserCoupon.create(userId, coupon.id!!, 1)
        return userCouponRepository.save(userCoupon)
    }
}