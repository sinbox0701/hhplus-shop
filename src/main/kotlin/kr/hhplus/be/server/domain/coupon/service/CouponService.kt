package kr.hhplus.be.server.domain.coupon.service

import kr.hhplus.be.server.domain.coupon.service.CouponCommand
import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import org.springframework.stereotype.Service

@Service
class CouponService(
    private val couponRepository: CouponRepository
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

    fun findById(id: Long): Coupon {
        return couponRepository.findById(id)
            ?: throw IllegalArgumentException("Coupon not found with id: $id")
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
}