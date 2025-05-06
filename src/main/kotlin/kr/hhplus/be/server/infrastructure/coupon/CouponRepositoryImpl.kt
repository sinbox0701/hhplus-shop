package kr.hhplus.be.server.infrastructure.coupon

import kr.hhplus.be.server.domain.coupon.model.Coupon
import kr.hhplus.be.server.domain.coupon.model.CouponType
import kr.hhplus.be.server.domain.coupon.repository.CouponRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class CouponRepositoryImpl(
    private val jpaCouponRepository: JpaCouponRepository
) : CouponRepository {
    
    override fun save(coupon: Coupon): Coupon {
        val couponEntity = CouponEntity.fromCoupon(coupon)
        val savedEntity = jpaCouponRepository.save(couponEntity)
        return savedEntity.toCoupon()
    }
    
    override fun findAll(): List<Coupon> {
        return jpaCouponRepository.findAll().map { it.toCoupon() }
    }
    
    override fun findById(id: Long): Coupon? {
        return jpaCouponRepository.findByIdOrNull(id)?.toCoupon()
    }
    
    override fun findByCode(code: String): Coupon? {
        return jpaCouponRepository.findByCode(code)?.toCoupon()
    }
    
    override fun findByType(type: CouponType): List<Coupon> {
        return jpaCouponRepository.findByCouponType(type).map { it.toCoupon() }
    }
    
    override fun update(coupon: Coupon): Coupon {
        val couponEntity = CouponEntity.fromCoupon(coupon)
        val savedEntity = jpaCouponRepository.save(couponEntity)
        return savedEntity.toCoupon()
    }
    
    override fun delete(id: Long) {
        jpaCouponRepository.deleteById(id)
    }
    
    @Transactional
    override fun findByIdWithPessimisticLock(id: Long): Coupon? {
        return jpaCouponRepository.findByIdWithPessimisticLock(id)?.toCoupon()
    }
    
    @Transactional
    override fun findByCodeWithPessimisticLock(code: String): Coupon? {
        return jpaCouponRepository.findByCodeWithPessimisticLock(code)?.toCoupon()
    }
} 